package randomwalk;


import ec.util.MersenneTwister;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import gui.NetworkModel;
import javafx.geometry.Point3D;
import modelcomponents.*;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/13/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomWalk {
    private final static MersenneTwister random = new MersenneTwister();
    private static CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);
    private static final double EPSILON = 0.0000001;
    private static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> randomWalkGraphs;
    private static Graph<ModelObject, ModelEdge> completeGraph = null;
    private static ModelObject startingLocation = null;
    private static JTextArea taskOutput;
    private static double stabilityValue;
    private static JFrame progressFrame;
    private static final Semaphore randomWalkSemaphore = new Semaphore(1);
    private static final int MAX_SOLVER_SIZE = 5000;

    private RandomWalk() {
    }


    public static void setRandomWalkParameters(Graph<ModelObject, ModelEdge> completeGraph, ModelObject startingLocation) {
        RandomWalk.completeGraph = completeGraph;
        RandomWalk.startingLocation = startingLocation;
    }


    private static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRandomWalkCollection() {
        if (!randomWalkSemaphore.tryAcquire()) {
            try {
                randomWalkSemaphore.tryAcquire(1, 600, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return randomWalkGraphs;
        }
        if (completeGraph == null || startingLocation == null) {
            throw new NullPointerException();
        }

        varianceList.clear();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                new HashSet<DirectedSparseMultigraph<ModelObject, modelcomponents.ModelEdge>>();
        List<Double> listOfGyrationRadius = new ArrayList<Double>();
        createProgressBar();
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
                        return generateRandomWalk(completeGraph, startingLocation);
                    }
                }));
            }


            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = null;
            for (int i = 0; i < n; i++) {
                try {
                    tempGraph = ecs.take().get();

                    if (tempGraph != null) {
                        resultSet.add(tempGraph);
                        listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
                        if (isStable(listOfGyrationRadius)) {
                            break;
                        } else {
                            count++;
                            if (count % 50 == 0) {
                                NumberFormat doubleFormat = new DecimalFormat("##.000000");
                                final String lastStabilityValue = doubleFormat.format(stabilityValue);
                                NumberFormat integerFormat = new DecimalFormat("0000");
                                final String lastCount = integerFormat.format(count);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        taskOutput.append((lastCount) + " : " + lastStabilityValue + "\n");
                                        progressFrame.revalidate();
                                    }
                                });
                            }
                        }

                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

        } finally {
            for (Future<DirectedSparseMultigraph<ModelObject, ModelEdge>> f : futures)
                f.cancel(true);
        }


//            System.out.println(resultSet.size());


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressFrame.dispose();
            }
        }

        );

        randomWalkGraphs = resultSet;
        randomWalkSemaphore.release();
        return randomWalkGraphs;
    }

    private static void createProgressBar() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);


                taskOutput = new JTextArea(5, 20);
                taskOutput.setMargin(new Insets(5, 5, 5, 5));
                taskOutput.setEditable(false);
                DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                progressFrame = new JFrame("Generating random walk data");
                progressFrame.setLayout(new BorderLayout());
                progressFrame.add(progressBar, BorderLayout.NORTH);
                progressFrame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
                progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                progressFrame.setLocation(25, 25);
                progressFrame.setSize(400, 200);
                progressFrame.setVisible(true);
            }
        });
    }

    private static boolean isStable(List<Double> listOfGyrationRadius) {

        double[] primitiveNumbers = new double[listOfGyrationRadius.size()];
        int i = 0;
        for (Double number : listOfGyrationRadius) {
            primitiveNumbers[i] = number;
            i++;
        }
        Mean meanEvaluator = new Mean();
        double mean = meanEvaluator.evaluate(primitiveNumbers);


        Variance varianceEvaluator = new Variance();
        double var = varianceEvaluator.evaluate(primitiveNumbers, mean);

        varianceList.add(var);

        if (varianceList.size() < 5) {
            return false;
        }

        double[] primitiveVariance = new double[5];
        i = 0;
        for (Double number : varianceList) {
            primitiveVariance[i] = number;
            i++;
        }

        mean = meanEvaluator.evaluate(primitiveVariance);
        stabilityValue = varianceEvaluator.evaluate(primitiveVariance, mean);
        if (stabilityValue < EPSILON) {
            return true;
        } else {
            return false;
        }
    }

    private static Double calculateRadiusOfGyration(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
        Point3D centerOfMass = calcCenterOfMass(graph, start);

        TreeSet<ModelEdge> setOfEdges = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge modelEdge1, ModelEdge modelEdge2) {
                return (int) (modelEdge1.getTime() - modelEdge2.getTime());
            }
        });
        setOfEdges.addAll(graph.getEdges());
        double sum = 0;

        ModelObject current = start;
        sum += Math.pow(getCenterOfArea(current).distance(centerOfMass), 2.0);
        int n = 0;
        for (ModelEdge edge : setOfEdges) {
            current = graph.getOpposite(current, edge);
            sum += Math.pow(getCenterOfArea(current).distance(centerOfMass), 2.0);
            n++;
        }

        return Math.sqrt(sum / n);
    }

    private static Point3D getCenterOfArea(ModelObject area) {
        if (area instanceof ModelArea) {
            ModelArea room = (ModelArea) area;
            return getCenterOfRoom(room);
        }

        ModelGroup group = (ModelGroup) area;
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;
        for (int areaId : group.getAreaIds()) {

            ModelArea tempArea = NetworkModel.instance().getRoomForId(areaId);
            Point3D tempPoint = getCenterOfRoom(tempArea);
            sumX += tempPoint.getX();
            sumY += tempPoint.getY();
            sumZ += tempPoint.getZ();
            n++;
        }


        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }

    private static Point3D getCenterOfRoom(ModelArea room) {
        Point p1 = room.getCorner0();
        Point p2 = room.getCorner1();

        double x = (p1.getX() + p2.getX()) / 2;
        double y = (p1.getY() + p2.getY()) / 2;
        double z = (double) NetworkModel.instance().getFloorForArea(room);
        return new Point3D(x, y, z);

    }

    private static Point3D calcCenterOfMass(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
        ModelObject current = start;
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;

        TreeSet<ModelEdge> setOfEdges = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge modelEdge1, ModelEdge modelEdge2) {
                return (int) (modelEdge1.getTime() - modelEdge2.getTime());
            }
        });
        setOfEdges.addAll(graph.getEdges());
        for (ModelEdge edge : setOfEdges) {

            current = graph.getOpposite(current, edge);

            Point3D p = getCenterOfArea(current);
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();

            n++;
        }
        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }


    private static DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(Graph<ModelObject, ModelEdge> graph, ModelObject startingLocation) {

        DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        int coverage = calculateCoverage(graph, currentGraph);
        int time = 0;
        ModelObject currentLocation = startingLocation;
        currentGraph.addVertex(currentLocation);
        while (coverage < 100) {
            ArrayList<ModelObject> neighbours = new ArrayList<ModelObject>();
            neighbours.addAll(graph.getNeighbors(currentLocation));
            ModelObject next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));
            currentGraph.addVertex(next);
            ModelEdge edge = new ModelEdge();
            edge.setTime(time++);
            currentGraph.addEdge(edge, currentLocation, next);
            currentLocation = next;
            coverage = calculateCoverage(graph, currentGraph);

        }

        return currentGraph;
    }


    private static int calculateCoverage(Graph<ModelObject, ModelEdge> completeGraph, DirectedGraph<ModelObject, ModelEdge> graph) {
        int count = 0;
        for (ModelObject vertex : completeGraph.getVertices()) {
            if (graph.containsVertex(vertex)) {
                count++;
            }
        }
        return (count * 100) / completeGraph.getVertexCount();
    }


    public static HashMap<String, Number> calculateAverageRoomVisitFrequency() {
        if (randomWalkGraphs == null || randomWalkGraphs.isEmpty()) {
            generateRandomWalkCollection();
        }

        HashMap<String, Number> result = new HashMap<String, Number>();
//        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : randomWalkGraphs) {
            for (ModelObject vertex : graph.getVertices()) {
                double count = 0;
                if (result.containsKey(vertex.toString())) {
                    count = result.get(vertex.toString()).doubleValue();
                }
//                int numberOfEdges = roomEdgeCountMapping.get(vertex.toString());

//                result.put(vertex.toString(), (graph.inDegree(vertex) / numberOfEdges) + count);
                result.put(vertex.toString(), (graph.inDegree(vertex) + count));

            }
        }

        for (String roomName : result.keySet()) {
            result.put(roomName, result.get(roomName).doubleValue() / randomWalkGraphs.size());
        }
        return result;

    }


    public static HashMap<String, Number> calculateAverageDoorUseFrequency() {
        if (randomWalkGraphs == null || randomWalkGraphs.isEmpty()) {
            generateRandomWalkCollection();
        }

        HashMap<String, Number> result = new HashMap<String, Number>();
//        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : randomWalkGraphs) {

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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (randomWalkGraphs == null || randomWalkGraphs.isEmpty()) {
            SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> worker = new SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>() {
                @Override
                protected Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {
                    Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = generateRandomWalkCollection();
                    tempSemaphore.release();
                    return graphCollection;
                }

            };

            worker.execute();
            try {

                tempSemaphore.tryAcquire(1, 300, TimeUnit.SECONDS);

                worker.get();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println("data received");
        generatorSemaphore.release();
        return randomWalkGraphs;
    }

    public static Double getCoverageOfRandomWalks(final int pathLength, final String startingRoom, Semaphore coverageAreaSemaphore) {
        final Semaphore semaphore = new Semaphore(2);
        final Semaphore mutex = new Semaphore(1);
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        SwingWorker<Double, Void> tempWorker = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {

                if (randomWalkGraphs == null || randomWalkGraphs.isEmpty()) {
                    generateRandomWalkCollection();
                }

                semaphore.acquire(2);

                SwingWorker<HashMap<String, HashMap<String, Double>>, Void> firstOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, Double>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, Double>> doInBackground() throws Exception {

                        return GraphUtilities.calculateFirstOrderProbabilities(randomWalkGraphs, semaphore);
                    }

                };

                SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void> secondOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, HashMap<String, Double>>> doInBackground() throws Exception {


                        return GraphUtilities.calculateSecondOrderProbabilities(randomWalkGraphs, semaphore);
                    }


                };

                firstOrderCalculator.execute();
                secondOrderCalculator.execute();


                System.out.println("Waiting for random walk");
                semaphore.tryAcquire(2, 300, TimeUnit.SECONDS);
                System.out.println("Random walk acquired");
                HashMap<String, HashMap<String, Double>> firstOrderProbs = firstOrderCalculator.get();
                HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbs = secondOrderCalculator.get();
                System.out.println("Data received");

                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> pathCollections = GraphUtilities.generatePaths(firstOrderProbs, secondOrderProbs, startingRoom, pathLength);
                Double coverage = GraphUtilities.calculateAverageCoverage(pathCollections);

                mutex.release();
                return coverage;
            }


        };
        tempWorker.execute();

        try {
            mutex.tryAcquire(1, 300, TimeUnit.SECONDS);
            coverageAreaSemaphore.release();
            return tempWorker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
