package randomwalk;

import ec.util.MersenneTwister;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.ProgressVisualizer;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

import static modelcomponents.GraphUtilities.calculateRadiusOfGyration;
import static modelcomponents.GraphUtilities.isStable;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/28/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomWalkOrganizer {


    public static RandomWalkType getRandomWalkType() {
        return (RandomWalkType)
                JOptionPane.showInputDialog(new JFrame(), "Choose random walk type", "Random walk chooser",
                        JOptionPane.QUESTION_MESSAGE, null,
                        RandomWalkType.values(), RandomWalkType.UNBIASED);

    }


    public enum RandomWalkType {
        UNBIASED(new UnbiasedRandomWalk()),
        WITH_MEMORY(new FirstOrderBiasedRandomWalk()),
        WITH_MEMORY_AND_DISTANCE(new FirstOrderBiasedWithDistance()),
        WITH_FIRST_ORDER_MEMORY(new NthOrderBiasedRandomWalk(1)),
        WITH_SECOND_ORDER_MEMORY(new NthOrderBiasedRandomWalk(2)),
        WITH_THIRD_ORDER_MEMORY(new NthOrderBiasedRandomWalk(3)),
        WITH_FOURTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(4)),
        WITH_FIFTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(5)),
        WITH_SIXTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(6)),
        WITH_SEVENTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(7)),
        WITH_EIGHTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(8)),
        WITH_NINTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(9)),
        WITH_TENTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(10)),
        WITH_ELEVENTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(11)),
        WITH_TWELFTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(12)),
        WITH_THIRTEENTH_ORDER_MEMORY(new NthOrderBiasedRandomWalk(13));


        private final RandomWalkGenerator randomWalkGenerator;
        private final Semaphore semaphore;


        RandomWalkType(RandomWalkGenerator generator) {
            this.randomWalkGenerator = generator;
            this.semaphore = new Semaphore(1);
        }


    }


    public final static MersenneTwister random = new MersenneTwister();

    private static HashMap<RandomWalkType, Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>> randomWalkGraphs
            = new HashMap<RandomWalkType, Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>>();


    private static final int MAX_SOLVER_SIZE = 5000;

    private RandomWalkOrganizer() {
    }


    private static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRandomWalkCollection(final RandomWalkType type) {
        if (CompleteGraph.instance().getGraph() == null ||CompleteGraph.instance().getStartingNode() == null) {
            throw new NullPointerException();
        }

        if (!type.semaphore.tryAcquire()) {
            try {
                type.semaphore.tryAcquire(1, 600, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return randomWalkGraphs.get(type);
        }


        CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        List<Double> listOfGyrationRadius = new ArrayList<Double>();
        final ProgressVisualizer pv = new ProgressVisualizer("generating random walks..." + type, ProgressVisualizer.DeterminateType.INDETERMINATE);
        ExecutorService threadPool = Executors.newCachedThreadPool();
//        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        CompletionService<DirectedSparseMultigraph<ModelObject, ModelEdge>> ecs
                = new ExecutorCompletionService<DirectedSparseMultigraph<ModelObject, ModelEdge>>(threadPool);
        int count = 0;
        int n = MAX_SOLVER_SIZE;


        List<Future<DirectedSparseMultigraph<ModelObject, ModelEdge>>> futures
                = new ArrayList<Future<DirectedSparseMultigraph<ModelObject, ModelEdge>>>(n);
        try {
            for (int i = 0; i < n; i++) {
                futures.add(ecs.submit(new Callable<DirectedSparseMultigraph<ModelObject, ModelEdge>>() {

                    @Override
                    public DirectedSparseMultigraph<ModelObject, ModelEdge> call() throws Exception {
                        return type.randomWalkGenerator.generateRandomWalk(CompleteGraph.instance().getStartingNode());
                    }
                }));
            }


            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = null;
            for (int i = 0; i < n; i++) {
                try {
                    tempGraph = ecs.take().get();

                    if (tempGraph != null) {
                        resultSet.add(tempGraph);
                        listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, CompleteGraph.instance().getStartingNode()));
                        if (isStable(listOfGyrationRadius, varianceList, pv)) {
                            break;
                        } else {
                            count++;
                            if (count % 50 == 0) {

                                pv.displayStability(count);

                            }

                        }

                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            for (Future<DirectedSparseMultigraph<ModelObject, ModelEdge>> f : futures)
                f.cancel(true);
        }


//            System.out.println(resultSet.size());


        pv.finish();

        synchronized (randomWalkGraphs) {
            randomWalkGraphs.put(type, resultSet);
        }
        type.semaphore.release();
        return randomWalkGraphs.get(type);
    }


    public static HashMap<String, Number> calculateAverageRoomVisitFrequency(final RandomWalkType type) {

        ensureGraphExists(type);

        HashMap<String, Number> result = new HashMap<String, Number>();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = randomWalkGraphs.get(type);
//        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : graphCollection) {
            for (ModelObject vertex : graph.getVertices()) {
                double count = 0;
                if (result.containsKey(vertex.toString())) {
                    count = result.get(vertex.toString()).doubleValue();
                }
//                int numberOfEdges = roomEdgeCountMapping.getProbabilityOfSequence(vertex.toString());

//                result.putDestinationGivenSeqProbability(vertex.toString(), (graph.inDegree(vertex) / numberOfEdges) + count);
                result.put(vertex.toString(), (graph.inDegree(vertex) + count));

            }
        }

        for (String roomName : result.keySet()) {
            result.put(roomName, result.get(roomName).doubleValue() / randomWalkGraphs.size());
        }
        return result;

    }

    private static void ensureGraphExists(final RandomWalkType type) {

        if (!randomWalkGraphs.containsKey(type)) {
            final Semaphore tempSemaphore = new Semaphore(1);
            try {
                tempSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> worker = new SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>() {
                @Override
                protected Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {

                    Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = generateRandomWalkCollection(type);
                    tempSemaphore.release();
                    return graphCollection;
                }

            };

            worker.execute();
            try {

                tempSemaphore.tryAcquire(1, 300, TimeUnit.SECONDS);

                worker.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    public static HashMap<String, Number> calculateAverageDoorUseFrequency() {
        RandomWalkType type = getRandomWalkType();
        ensureGraphExists(type);

        HashMap<String, Number> result = new HashMap<String, Number>();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> localCollection = randomWalkGraphs.get(type);
//        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : localCollection) {

            for (ModelEdge edge : graph.getEdges()) {
                String edgeStringRepresentation = NetworkModel.edgeToString(graph.getEndpoints(edge));


                if (!result.containsKey(edgeStringRepresentation)) {
                    result.put(edgeStringRepresentation, 0);
                } else {
                    result.put(edgeStringRepresentation, result.get(edgeStringRepresentation).longValue() + 1);
                }
            }
        }

        for (String edgeName : result.keySet()) {
            result.put(edgeName, result.get(edgeName).doubleValue() / randomWalkGraphs.size());
        }
        return result;
    }

    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> getAllRandomWalkGraphs(final Semaphore generatorSemaphore) {
        final Semaphore tempSemaphore = new Semaphore(1);
        try {
            tempSemaphore.acquire(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RandomWalkType type = getRandomWalkType();
        ensureGraphExists(type);
        generatorSemaphore.release();
        return randomWalkGraphs.get(type);
    }

    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> getAllRandomWalkGraphs(final Semaphore generatorSemaphore, final RandomWalkType type) {
        final Semaphore tempSemaphore = new Semaphore(1);
        try {
            tempSemaphore.acquire(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ensureGraphExists(type);
        generatorSemaphore.release();
        return randomWalkGraphs.get(type);
    }




}
