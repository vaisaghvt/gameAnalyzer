package markovmodeldata;

import com.google.common.collect.HashBasedTable;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.ProgressVisualizer;
import modelcomponents.CompleteGraph;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static modelcomponents.GraphUtilities.trimSequence;

public class MarkovDataStore {
    private final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection;
    HashMap<Integer, RecursiveHashMap> orderToDirectMarkovDataMapping =
            new HashMap<Integer, RecursiveHashMap>();
    HashBasedTable<Integer, Integer, RecursiveHashMap> nFromMData = HashBasedTable.create();

    public MarkovDataStore(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphs) {
        this.graphCollection = graphs;
    }

    public boolean containsDirectMarkovData(int order) {
        return orderToDirectMarkovDataMapping.containsKey(order);
    }


    public RecursiveHashMap getDirectMarkovData(int order) {
        if (!containsDirectMarkovData(order)) {
            putDirectMarkovData(order, generateDirectDataForOrder(order));
        }

        return orderToDirectMarkovDataMapping.get(order);
    }

    public boolean containsNFromMData(int n, int m) {
        return nFromMData.contains(n, m);
    }


    public RecursiveHashMap getNFromMData(int n, int m) {
        if (!containsNFromMData(n, m)) {
            RecursiveHashMap data = constructNFromM(n, m);
            this.nFromMData.put(n, m, data);

        }

        return nFromMData.get(n, m);
    }


    private RecursiveHashMap generateDirectDataForOrder(final int order) {
        final Collection<ModelObject> vertices = CompleteGraph.instance().getVertices();
//        HashMultimap<String, List<String>> pathsFromSource = HashMultimap.create();
        RecursiveHashMap requiredResult = new RecursiveHashMap(order);
        // Find all sequences of length order from vertex;

        final ProgressVisualizer pv = new ProgressVisualizer("Generating order" + order + "data", ProgressVisualizer.DeterminateType.DETERMINATE);

        final Semaphore markovDataCalculatorSemaphore = new Semaphore(1);
        try {
            markovDataCalculatorSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingWorker<RecursiveHashMap, Void> markovDataCalculator = new SwingWorker<RecursiveHashMap, Void>() {
            @Override
            protected RecursiveHashMap doInBackground() throws Exception {
                this.addPropertyChangeListener(pv);
                RecursiveHashMap result = new RecursiveHashMap(order);
                int i = 0;
                for (ModelObject vertex : vertices) {
//            String source = vertex.toString();

                    pv.print("Processing " + vertex.toString() + "...\n");
                    pv.print("\tCalculating possible paths...\n");
                    List<List<String>> completeListOfPathsForCurrent = new ArrayList<List<String>>();
                    completeListOfPathsForCurrent.addAll(calculatePathsFromAllGraphs(graphCollection, vertex, order));

                    pv.print("\tSummarizing paths to markov data...\n");
                    for (List<String> path : completeListOfPathsForCurrent) {
                        double existingOccurrenceOfPath = result.getValue(new ArrayList<String>(path));
                        result.putValue(path, existingOccurrenceOfPath + 1.0);
                    }
                    final int currentProgress = i++;
                    setProgress((currentProgress * 100) / vertices.size());

                }
                markovDataCalculatorSemaphore.release();
                return result;
            }

            @Override
            protected void done() {
                pv.finish();

            }
        };
        markovDataCalculator.execute();

        try {
            markovDataCalculatorSemaphore.tryAcquire(1, 600, TimeUnit.SECONDS);
            requiredResult = markovDataCalculator.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        requiredResult.normalize();
        return requiredResult;

    }

    private Collection<List<String>> calculatePathsFromAllGraphs(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection,
                                                                           final ModelObject vertex, final int order) {
        Collection<List<String>> completeListOfPathsForCurrent = new ArrayList<List<String>>();


        final Semaphore graphSemaphore = new Semaphore(graphCollection.size());
        try {
            graphSemaphore.acquire(graphCollection.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<SwingWorker<Collection<List<String>>, Void>> graphCalculatorTaskList
                = new ArrayList<SwingWorker<Collection<List<String>>, Void>>(graphCollection.size());

        for (final DirectedSparseMultigraph<ModelObject, ModelEdge> graph : graphCollection) {


            graphCalculatorTaskList.add(new SwingWorker<Collection<List<String>>, Void>() {
                @Override
                protected Collection<List<String>> doInBackground() {
                    return GraphUtilities.findActualPathsOfLength(graph, vertex, order);
                }

                @Override
                protected void done() {
                    graphSemaphore.release();
                }
            });
        }
        for (SwingWorker<Collection<List<String>>, Void> task : graphCalculatorTaskList) {
            task.execute();
        }
        try {
            graphSemaphore.tryAcquire(graphCollection.size(), 600, TimeUnit.SECONDS);
            for (SwingWorker<Collection<List<String>>, Void> task : graphCalculatorTaskList) {

                completeListOfPathsForCurrent.addAll(task.get());

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        return completeListOfPathsForCurrent;
    }

    private void putDirectMarkovData(int order, RecursiveHashMap markovData) {
        orderToDirectMarkovDataMapping.put(order, markovData);
    }

    private RecursiveHashMap constructNFromM(int n, int m) {
        if (n <= m) {
            return getDirectMarkovData(n);
//        }else if (n == m + 1) {
//            RecursiveHashMap mThOrderMap = orderToDirectMarkovDataMapping.getValue(m);
//            RecursiveHashMap result = new RecursiveHashMap(n);
//            for (List<String> sequence : mThOrderMap.getSequenceCodes()) {
//                String destination = sequence.getValue(sequence.size() - 1);
//                ModelObject destinationNode = NetworkModel.instance().findRoomByName(destination);
//                Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(destinationNode);
//                double prior = mThOrderMap.getValue(sequence);
//
//                for (ModelObject neighbour: neighbours) {
//                    List<String> newSequence = new ArrayList<String>();
//                    newSequence.addAll(sequence);
//                    newSequence.remove(0);
//                    newSequence.add(neighbour.toString());
//                    double newValue = prior *
//                            mThOrderMap.getValue(newSequence);
//
//                    List<String> newSequenceForN = new ArrayList<String>();
//                    newSequenceForN.addAll(sequence);
//                    newSequenceForN.add(neighbour.toString());
//
//                    result.putValue(newSequenceForN, newValue);
//
//
//                }
//            }
//            return result;

        } else {
            RecursiveHashMap result = new RecursiveHashMap(n);
            try{

            RecursiveHashMap nMinusOneTable = constructNFromM(n - 1, m);  // equals mthOrderMap if n= m+1;
            RecursiveHashMap mThOrderMap = getDirectMarkovData(m);
            for (String sequenceCode : nMinusOneTable.getSequenceCodes()) {
                List<String> sequence = nMinusOneTable.getSequenceForCode(sequenceCode);
                String destination = sequence.get(sequence.size() - 1);
                ModelObject destinationNode = CompleteGraph.instance().findRoomByName(destination);
                Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(destinationNode);
                double prior = nMinusOneTable.getValue(new ArrayList<String>(sequence));

                List<String> trimmedSequence = trimSequence(m, sequence);

                for (ModelObject neighbour : neighbours) {
                    List<String> newSequence = new ArrayList<String>(trimmedSequence);
//                    newSequence.addAll(trimmedSequence);
//                    newSequence.remove(0);
                    newSequence.add(neighbour.toString());
                    double newValue = prior *
                            mThOrderMap.getValue(new ArrayList<String>(newSequence));

                    List<String> newSequenceForN = new ArrayList<String>(sequence);
//                    newSequenceForN.addAll();
                    newSequenceForN.add(neighbour.toString());

                    result.putValue(newSequenceForN, newValue);


                }
            }
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;

        }
    }



}
