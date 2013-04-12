package modelcomponents;

import ec.util.MersenneTwister;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import gui.ProgressVisualizer;
import javafx.geometry.Point3D;
import markovmodeldata.RecursiveHashMap;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/21/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphUtilities {

    private final static MersenneTwister random = new MersenneTwister();
    private static final double EPSILON = 0.0000001;

//    public static HashMap<String, HashMap<String, HashMap<String, Double>>> calculateSecondOrderProbabilities(
//            final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, final Semaphore semaphore){
//        final ProgressVisualizer pv = new ProgressVisualizer("Generating second order probabilities", ProgressVisualizer.DeterminateType.DETERMINATE);
//
//        SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void> secondOrderProbabilityCalculator =
//                              new SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void>() {
//            @Override
//            protected HashMap<String, HashMap<String, HashMap<String, Double>>> doInBackground() throws Exception {
//                int numberOfRooms = CompleteGraph.instance().getSortedRooms().size();
//                int count = 0;
//
//                this.addPropertyChangeListener(pv);
//                HashMap<String, HashMap<String, HashMap<String, Double>>> result = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
//                for (String room : CompleteGraph.instance().getSortedRooms()) {
//                    pv.print("processing " + room + "...\n");
//                    result.put(room,
//                            calculateSecondOrderProbForNeighbouringRooms(graphCollection, room));
//
//                    setProgress((count++ * 100) / numberOfRooms);
//
//                }
//
//                return result;
//
//            }
//
//            @Override
//            protected void done() {
//                pv.finish();
//                semaphore.release();
//            }
//        };
//
//        secondOrderProbabilityCalculator.execute();
//        try {
//            return secondOrderProbabilityCalculator.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        } catch (ExecutionException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        }
//
//
//    }
//
//    private static HashMap<String, HashMap<String, Double>> calculateSecondOrderProbForNeighbouringRooms(
//            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, String startRoom) {
//        ModelObject mainNode = CompleteGraph.instance().findRoomByName(startRoom);
//        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
//        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
//
////        localGraph.addVertex(mainNode);
//        for (ModelObject node : neighbours) {
//            localGraph.addVertex(node);
//        }
//
//        for (DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph : graphCollection) {
//
//
//            if (!tempGraph.containsVertex(mainNode)) {
//                continue;
//            }
//
//            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
//                @Override
//                public int compare(ModelEdge o1, ModelEdge o2) {
//                    return (int) (o1.getTime() - o2.getTime());
//                }
//            });
//
//            edgeCollection.addAll(tempGraph.getIncidentEdges(mainNode));
//
//            ModelEdge lonelyEdge = null;
//            if (tempGraph.inDegree(mainNode) > tempGraph.outDegree(mainNode)) {
//                //Ending Vertex;
//                edgeCollection.pollLast();
//
////                localGraph.addVertex(mainNode);
////                localGraph.addEdge(new ModelEdge(),
////                        tempGraph.getOpposite(mainNode, lonelyEdge),
////                        mainNode
////                );
//
//            } else if (tempGraph.inDegree(mainNode) < tempGraph.outDegree(mainNode)) {
//                //First Vertex;
//                edgeCollection.pollFirst();
//
//
////                localGraph.addVertex(mainNode);
////                localGraph.addEdge(new ModelEdge(),
////                        mainNode,
////                        tempGraph.getOpposite(mainNode, lonelyEdge)
////                );
//
//            }
//            int size = edgeCollection.size();
//            for (int i = 0; i <  size/ 2; i++) {
//                ModelEdge incoming = edgeCollection.pollFirst();
//                ModelEdge outgoing = edgeCollection.pollFirst();
//                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
//                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
//                localGraph.addEdge(new ModelEdge(), from, to);
//            }
//        }
//
//
//        return convertToSecondOrderProbabilities(localGraph);
//    }
//
//    private static HashMap<String, HashMap<String, Double>> convertToSecondOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
//        HashMap<String, HashMap<String, Integer>> nodeToNodeTravelFrequency = new HashMap<String, HashMap<String, Integer>>();
//
//        for (ModelEdge edge : localGraph.getEdges()) {
//            String source = localGraph.getSource(edge).toString();
//            String destination = localGraph.getDest(edge).toString();
//
//            if (!nodeToNodeTravelFrequency.containsKey(source)) {
//                nodeToNodeTravelFrequency.put(source, new HashMap<String, Integer>());
//            }
//            if (!nodeToNodeTravelFrequency.get(source).containsKey(destination)) {
//                nodeToNodeTravelFrequency.get(source).put(destination, 0);
//            }
//
//            int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
//            nodeToNodeTravelFrequency.get(source).put(destination, currentValue + 1);
//
//        }
//
//        HashMap<String, HashMap<String, Double>> nodeToNodeProbabilities = new HashMap<String, HashMap<String, Double>>();
//
//        for (String source : nodeToNodeTravelFrequency.keySet()) {
//            nodeToNodeProbabilities.put(source, new HashMap<String, Double>());
//            double totalNumberOfOutEdges = 0.0;
//            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {
//                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source).get(dest);
//            }
//            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {
//
//                nodeToNodeProbabilities.get(source).put(dest, (double) nodeToNodeTravelFrequency.get(source).get(dest) / totalNumberOfOutEdges);
//            }
//
//
//        }
//
//
//        return nodeToNodeProbabilities;
//    }
//
//
//    public static HashMap<String, HashMap<String, Double>> calculateFirstOrderProbabilities(
//            final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, final Semaphore semaphore) {
//
//
//        final ProgressVisualizer pv = new ProgressVisualizer("Generating first order probabilities", ProgressVisualizer.DeterminateType.DETERMINATE);
//
//        SwingWorker<HashMap<String, HashMap<String, Double>>, Void> firstOrderProbabilityCalculator = new SwingWorker<HashMap<String, HashMap<String, Double>>, Void>() {
//            @Override
//            protected HashMap<String, HashMap<String, Double>> doInBackground() throws Exception {
//                int numberOfRooms = CompleteGraph.instance().getSortedRooms().size();
//                int count = 0;
//                this.addPropertyChangeListener(pv);
//                HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
//                for (String room : CompleteGraph.instance().getSortedRooms()) {
//                    pv.print("processing " + room + "...\n");
//
//                    result.put(room,
//                            calculateFirstOrderProbForNeighbouringRooms(graphCollection, room));
//
//                    setProgress((count++ * 100) / numberOfRooms);
//
//                }
//                return result;
//            }
//
//            @Override
//            protected void done() {
//                pv.finish();
//                semaphore.release();
//            }
//        };
//
//        firstOrderProbabilityCalculator.execute();
//        try {
//            return firstOrderProbabilityCalculator.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        } catch (ExecutionException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        }
//    }
//
//    private static HashMap<String, Double> calculateFirstOrderProbForNeighbouringRooms(
//            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, String room) {
//        ModelObject mainNode = CompleteGraph.instance().findRoomByName(room);
//        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
//        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
//
//        localGraph.addVertex(mainNode);
//        for (ModelObject node : neighbours) {
//            localGraph.addVertex(node);
//        }
//
//        for (DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph : graphCollection) {
//
//
//            if (!tempGraph.containsVertex(mainNode)) {
//                continue;
//            }
//
//
//            for (ModelEdge edge : tempGraph.getOutEdges(mainNode)) {
//
//                localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
//            }
//
//        }
//
//
//        return convertToFirstOrderProbabilities(localGraph, mainNode);
//    }
//
//    private static HashMap<String, Double> convertToFirstOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, ModelObject source) {
//        HashMap<String, Integer> nodeToNodeTravelFrequency = new HashMap<String, Integer>();
//
//        for (ModelEdge edge : localGraph.getEdges()) {
//
//            String destination = localGraph.getDest(edge).toString();
//
//
//            if (!nodeToNodeTravelFrequency.containsKey(destination)) {
//                nodeToNodeTravelFrequency.put(destination, 0);
//            }
//
//            int currentValue = nodeToNodeTravelFrequency.get(destination).intValue();
//            nodeToNodeTravelFrequency.put(destination, currentValue + 1);
//
//        }
//
//        HashMap<String, Double> nodeToNodeProbabilities = new HashMap<String, Double>();
//
//
//        double totalNumberOfOutEdges = 0.0;
//        for (String destination : nodeToNodeTravelFrequency.keySet()) {
//            totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(destination);
//        }
//        for (String destination : nodeToNodeTravelFrequency.keySet()) {
//
//            nodeToNodeProbabilities.put(destination,
//                    (double) nodeToNodeTravelFrequency.get(destination) / totalNumberOfOutEdges);
//        }
//
//
//        return nodeToNodeProbabilities;
//    }

    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generatePathsTillCoverage(
            RecursiveHashMap firstOrderProbs, RecursiveHashMap secondOrderProbs,
            String startingRoom, final int coverageRequired) {
        try {
            Graph<ModelObject, ModelEdge> completeGraph = CompleteGraph.instance().getGraph();
            ModelObject startingLocation;


            if (completeGraph != null) {
                startingLocation = CompleteGraph.instance().findRoomByName(startingRoom);
                if (startingLocation == null) {
                    System.out.println("No starting location");
                    throw new NullPointerException();
                }

            } else {
                System.out.println("No graph to load.");
                throw new NullPointerException();
            }


            CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);

            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                    new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
            List<Double> listOfGyrationRadius = new ArrayList<Double>();
            ProgressVisualizer progressVisualizer = new ProgressVisualizer("Generating path data", ProgressVisualizer.DeterminateType.INDETERMINATE);
            int count = 0;
            while (true) {

                DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = generatePathForCoverage(completeGraph, startingLocation, coverageRequired, firstOrderProbs, secondOrderProbs);
                resultSet.add(tempGraph);
                listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
                if (isStable(listOfGyrationRadius, varianceList, progressVisualizer)) {

                    break;
                } else {
                    count++;
                    if (count % 50 == 0) {

                        progressVisualizer.displayStability(count);

                    }
                }


//            System.out.println(resultSet.size());
            }
            progressVisualizer.finish();


            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generatePaths(
            RecursiveHashMap firstOrderProbs,
            RecursiveHashMap secondOrderProbs,
            String startingRoom, int pathLength) {

        try {
            Graph<ModelObject, ModelEdge> completeGraph = CompleteGraph.instance().getGraph();
            ModelObject startingLocation;


            if (completeGraph != null) {
                startingLocation = CompleteGraph.instance().findRoomByName(startingRoom);
                if (startingLocation == null) {
                    System.out.println("No starting location");
                    throw new NullPointerException();
                }

            } else {
                System.out.println("No graph to load.");
                throw new NullPointerException();
            }


            CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);

            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                    new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
            List<Double> listOfGyrationRadius = new ArrayList<Double>();
            ProgressVisualizer progressVisualizer = new ProgressVisualizer("Generating path data", ProgressVisualizer.DeterminateType.INDETERMINATE);
            int count = 0;
            while (true) {

                DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = generatePath(startingLocation, pathLength, firstOrderProbs, secondOrderProbs);
                resultSet.add(tempGraph);
                listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
                if (isStable(listOfGyrationRadius, varianceList, progressVisualizer)) {

                    break;
                } else {
                    count++;
                    if (count % 50 == 0) {

                        progressVisualizer.displayStability(count);

                    }
                }


//            System.out.println(resultSet.size());
            }
            progressVisualizer.finish();


            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Can replace this with something in MarkovDataStore
    private static DirectedSparseMultigraph<ModelObject, ModelEdge> generatePathForCoverage(Graph<ModelObject, ModelEdge> completeGraph, ModelObject startingLocation,
                                                                                            int coverageRequired, RecursiveHashMap firstOrderProbabilities,
                                                                                            RecursiveHashMap secondOrderProbabilities) {
        DirectedSparseMultigraph<ModelObject, ModelEdge> path = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        path.addVertex(startingLocation);
        String startRoom = startingLocation.toString();


        ModelObject currentNode = startingLocation;

        String destination = getDestination(firstOrderProbabilities.getDestinationProbabilities(startRoom));


        if (destination.equals(startRoom)) {
            System.out.println("Source same as destination");
        }

        currentNode = addNewVertexToPath(currentNode, destination, path, 0);

        int numberOfNodesRequired = coverageRequired * completeGraph.getVertexCount() / 100;


        ModelObject lastNode = startingLocation;

        int hops = 1;

        while (path.getVertexCount() < numberOfNodesRequired) {
            ModelObject tempNode = currentNode;
            currentNode = addOneMoreHop(currentNode, lastNode, path, firstOrderProbabilities, secondOrderProbabilities, hops);
            lastNode = tempNode;
            if (currentNode == lastNode) {
                System.out.println("Error!!");
            }
            hops++;
        }

        return path;
    }


    private static DirectedSparseMultigraph<ModelObject, ModelEdge> generatePath(
            ModelObject startingLocation, int pathLength,
            RecursiveHashMap firstOrderProbabilities,
            RecursiveHashMap secondOrderProbabilities) {


        DirectedSparseMultigraph<ModelObject, ModelEdge> path = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        path.addVertex(startingLocation);
        String startRoom = startingLocation.toString();


        ModelObject currentNode = startingLocation;
        String destination = getDestination(
                firstOrderProbabilities.getDestinationProbabilities(startRoom));
        if (destination.equals(startRoom)) {
            System.out.println("Source same as destination");
        }


        currentNode = addNewVertexToPath(currentNode, destination, path, 0);

        if (pathLength == 1) {
            return path;

        }

        ModelObject lastNode = startingLocation;


        for (int hops = 1; hops < pathLength; hops++) {
            ModelObject tempNode = currentNode;
            currentNode = addOneMoreHop(currentNode, lastNode, path, firstOrderProbabilities, secondOrderProbabilities, hops);
            lastNode = tempNode;
            if (currentNode == lastNode) {
                System.out.println("Error!!");
            }
        }

        return path;

    }

    private static ModelObject addOneMoreHop(ModelObject lastNode, ModelObject lastToLastNode,
                                             DirectedSparseMultigraph<ModelObject, ModelEdge> path,
                                             RecursiveHashMap firstOrderProbabilities,
                                             RecursiveHashMap secondOrderProbabilities, int hop) {


        List<String> currentSequence = new ArrayList<String>(2);
        currentSequence.add(lastToLastNode.toString());
        currentSequence.add(lastNode.toString());
        Map<String, Double> nextLocationProbabilities = secondOrderProbabilities.getDestinationProbabilities(currentSequence);
        if (nextLocationProbabilities == null) {
            nextLocationProbabilities = firstOrderProbabilities.getDestinationProbabilities(lastNode.toString());
        }
        if (nextLocationProbabilities == null || nextLocationProbabilities.isEmpty()) {
            System.out.println("Mess up!!");
        }

        String destination = getDestination(nextLocationProbabilities);
        if (destination.equals(lastNode.toString())) {
            System.out.println("Source and destination are same!!");
        }

        return addNewVertexToPath(lastNode, destination, path, hop);


    }


    private static ModelObject addNewVertexToPath(ModelObject lastNode, String destination, DirectedSparseMultigraph<ModelObject, ModelEdge> result, int currentLength) {
        ModelObject newNode = CompleteGraph.instance().findRoomByName(destination);
        result.addVertex(newNode);
        ModelEdge edge = new ModelEdge();
        edge.setTime(currentLength);
        result.addEdge(edge, lastNode, newNode);

        return newNode;

    }

    private static String getDestination(Map<String, Double> roomProbabilities) {
        double value = 0.0;

        double randomDouble = random.nextDouble();

        for (String possibleDestination : roomProbabilities.keySet()) {
            value += roomProbabilities.get(possibleDestination);

            if (randomDouble < value) {

                return possibleDestination;
            }
        }

        System.out.println("TROUBLE!!!!");
        return null;
    }

    public static boolean isStable(List<Double> listOfGyrationRadius, CircularFifoBuffer<Double> varianceList, ProgressVisualizer progressVisualizer) {

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

        double stabilityValue = varianceEvaluator.evaluate(primitiveVariance, mean);
        progressVisualizer.setStability(stabilityValue);
        return stabilityValue < EPSILON;
    }


    public static Double calculateRadiusOfGyration(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
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

    public static Point3D calcCenterOfMass(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
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

    public static Point3D getCenterOfArea(ModelObject area) {
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

            ModelArea tempArea = CompleteGraph.instance().getRoomForId(areaId);
            Point3D tempPoint = getCenterOfRoom(tempArea);
            sumX += tempPoint.getX();
            sumY += tempPoint.getY();
            sumZ += tempPoint.getZ();
            n++;
        }


        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }

    public static Point3D getCenterOfRoom(ModelArea room) {
        Point p1 = room.getCorner0();
        Point p2 = room.getCorner1();

        double x = (p1.getX() + p2.getX()) / 2;
        double y = (p1.getY() + p2.getY()) / 2;
        double z = (double) CompleteGraph.instance().getFloorForArea(room);
        return new Point3D(x, y, z);

    }

    public static int calculateCoverage(Graph<ModelObject, ModelEdge> completeGraph, DirectedGraph<ModelObject, ModelEdge> graph) {
        int count = 0;
        for (ModelObject vertex : completeGraph.getVertices()) {
            if (graph.containsVertex(vertex)) {
                count++;
            }
        }
        return (count * 100) / completeGraph.getVertexCount();
    }

    public static Double calculateAverageCoverage(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection) {
        int totalNumberOfVertices = CompleteGraph.instance().getVertexCount();
        double[] coverageValues = new double[graphCollection.size()];
        int i = 0;
        for (DirectedSparseMultigraph graph : graphCollection) {
            int verticesInGraph = graph.getVertexCount();
            coverageValues[i] = (double) verticesInGraph / (double) totalNumberOfVertices;
            i++;
        }

        Mean meanEvaluator = new Mean();
        return meanEvaluator.evaluate(coverageValues);


    }


    public static HashMap<String, Double> calculateNumberOfHops(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> pathCollections) {
        double[] edgeCountArray = new double[pathCollections.size()];
        int pathNumber = 0;
        for (DirectedSparseMultigraph<ModelObject, ModelEdge> path : pathCollections) {
            edgeCountArray[pathNumber] = path.getEdgeCount();
            pathNumber++;
        }

        Mean meanCalculator = new Mean();
        double calculatedMean = meanCalculator.evaluate(edgeCountArray);
        StandardDeviation sd = new StandardDeviation();
        double calculatedSD = sd.evaluate(edgeCountArray);

        HashMap<String, Double> result = new HashMap<String, Double>();
        result.put("mean", calculatedMean);
        result.put("sd", calculatedSD);
        return result;
    }


    public static Collection<List<String>> findActualPathsOfLength(final DirectedSparseMultigraph<ModelObject, ModelEdge> graph, final ModelObject startingNode, final int length) {
        assert length >= 1;
        final ArrayList<List<String>> listOfPaths = new ArrayList<List<String>>();
        final Collection<ModelEdge> outEdges = graph.getOutEdges(startingNode);
//        if (length < 3) {

            if (outEdges == null) {
                return listOfPaths;
            }
            for (final ModelEdge edge : outEdges) {
                List<String> path = new ArrayList<String>(length);
                path.add(startingNode.toString());
                followTillHopCount(graph, startingNode, edge, length - 1, path);
                if (path.size() - 1 == length) {
                    listOfPaths.add(path);
                }
            }
//        }
        return listOfPaths;
    }

    private static void followTillHopCount(DirectedSparseMultigraph<ModelObject, ModelEdge> graph, ModelObject startingNode, ModelEdge previousEdge, int hopsRemaining, List<String> path) {
        ModelObject currentLocation = graph.getOpposite(startingNode, previousEdge);
        path.add(currentLocation.toString());
        if (hopsRemaining != 0) {
            ModelEdge newEdge = findNextEdge(currentLocation, previousEdge, graph);
            if (newEdge != null) {
                followTillHopCount(graph, currentLocation, newEdge, hopsRemaining - 1, path);
//            } else {
//                System.out.println("No more path to follow!");
            }
        }
    }

    private static ModelEdge findNextEdge(ModelObject currentLocation, ModelEdge previousEdge, DirectedSparseMultigraph<ModelObject, ModelEdge> graph) {

        double currentTime = previousEdge.getTime();

        TreeSet<ModelEdge> setOfEdges = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge modelEdge1, ModelEdge modelEdge2) {
                return (int) (modelEdge1.getTime() - modelEdge2.getTime());
            }
        });

        setOfEdges.addAll(graph.getOutEdges(currentLocation));

        for (ModelEdge edge : setOfEdges) {
            double nextTime = edge.getTime();
            if (nextTime >= currentTime) {
                return edge;
            }
        }

//        assert false;
        return null;
    }
}
