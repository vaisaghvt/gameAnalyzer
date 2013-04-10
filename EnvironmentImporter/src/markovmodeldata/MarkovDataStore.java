package markovmodeldata;

import com.google.common.collect.HashBasedTable;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MarkovDataStore {
    private final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> humanMovementGraphCollection;
    HashMap<Integer, RecursiveHashMap> orderToHumanMarkovDataMapping =
            new HashMap<Integer, RecursiveHashMap>();
    HashBasedTable<Integer, Integer, RecursiveHashMap> nFromMData = HashBasedTable.create();

    public MarkovDataStore(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphs) {
        this.humanMovementGraphCollection = graphs;
    }

    public boolean containsHumanMarkovData(int order) {
        return orderToHumanMarkovDataMapping.containsKey(order);
    }


    public RecursiveHashMap getHumanMarkovData(int order) {
        if (!containsHumanMarkovData(order)) {
            putHumanData(order, generateHumanDataForOrder(order));
        }

        return orderToHumanMarkovDataMapping.get(order);
    }

    public boolean containsNFromMData(int n, int m) {
        return nFromMData.contains(n, m);
    }


    public RecursiveHashMap getNFromMData(int n, int m) {
        if (!containsNFromMData(n, m)) {
            RecursiveHashMap data = constructNFromM(n, m);
            this.nFromMData.put(n,m,data);

        }

        return nFromMData.get(n,m);
    }


    private RecursiveHashMap generateHumanDataForOrder(int order) {
        Collection<ModelObject> vertices = NetworkModel.instance().getCompleteGraph().getVertices();
//        HashMultimap<String, List<String>> pathsFromSource = HashMultimap.create();
        RecursiveHashMap requiredResult = new RecursiveHashMap(order);
        // Find all sequences of length order from vertex;
        for(ModelObject vertex: vertices){
//            String source = vertex.toString();
            List<List<String>> completeListOfPathsForCurrent = new ArrayList<List<String>>();
            for(DirectedSparseMultigraph<ModelObject, ModelEdge> graph: humanMovementGraphCollection){
                Collection<List<String>> pathsTaken = GraphUtilities.findActualPathsOfLength(graph, vertex, order);
                completeListOfPathsForCurrent.addAll(pathsTaken);
            }

            for(List<String> path: completeListOfPathsForCurrent){
                double existingOccurrenceOfPath = requiredResult.getValue(path);
                requiredResult.putValue(path, existingOccurrenceOfPath+1.0);
            }
        }

        requiredResult.normalize();
        return requiredResult;

    }

    private void putHumanData(int order, RecursiveHashMap markovData) {
        orderToHumanMarkovDataMapping.put(order, markovData);
    }

    private RecursiveHashMap constructNFromM(int n, int m) {
        if (n <= m) {
            return orderToHumanMarkovDataMapping.get(n);
//        }else if (n == m + 1) {
//            RecursiveHashMap mThOrderMap = orderToHumanMarkovDataMapping.getValue(m);
//            RecursiveHashMap result = new RecursiveHashMap(n);
//            for (List<String> sequence : mThOrderMap.getSequences()) {
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
            RecursiveHashMap mThOrderMap = orderToHumanMarkovDataMapping.get(m);
            RecursiveHashMap nMinusOneTable = constructNFromM(n - 1, m);  // equals mthOrderMap if n= m+1;
            RecursiveHashMap result = new RecursiveHashMap(n);
            for (List<String> sequence : nMinusOneTable.getSequences()) {
                String destination = sequence.get(sequence.size() - 1);
                ModelObject destinationNode = NetworkModel.instance().findRoomByName(destination);
                Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(destinationNode);
                double prior = nMinusOneTable.getValue(sequence);

                List<String> trimmedSequence = trimSequence(m, sequence);

                for (ModelObject neighbour : neighbours) {
                    List<String> newSequence = new ArrayList<String>();
                    newSequence.addAll(trimmedSequence);
                    newSequence.remove(0);
                    newSequence.add(neighbour.toString());
                    double newValue = prior *
                            mThOrderMap.getValue(newSequence);

                    List<String> newSequenceForN = new ArrayList<String>();
                    newSequenceForN.addAll(sequence);
                    newSequenceForN.add(neighbour.toString());

                    result.putValue(newSequenceForN, newValue);


                }
            }
            return result;

        }
    }

    private List<String> trimSequence(int m, List<String> sequence) {
        List<String> result = new ArrayList<String>();
        for (int i = sequence.size() - 1; result.size() < m; i--) {
            result.add(0, sequence.get(i));

        }
        assert result.size() == m;
        assert result.get(result.size() - 1) == sequence.get(sequence.size() - 1);
        return result;

        //To change body of created methods use File | Settings | File Templates.
    }


}
