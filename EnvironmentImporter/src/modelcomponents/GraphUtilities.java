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
    private static final int PATHS_TO_GENERATE = 30000;


    public static Collection<List<String>> generatePathsTillCoverage(
            RecursiveHashMap markovData, final int coverageRequired) {
        Graph<ModelObject, ModelEdge> completeGraph = CompleteGraph.instance().getGraph();
        String startingRoom = CompleteGraph.instance().getStartingNode().toString();
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


//            CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);

        Collection<List<String>> resultSet = new ArrayList<List<String>>();
//            List<Double> listOfGyrationRadius = new ArrayList<Double>();
        ProgressVisualizer progressVisualizer = new ProgressVisualizer("Generating path data", ProgressVisualizer.DeterminateType.DETERMINATE);

        for(int count=0;count<PATHS_TO_GENERATE; count++) {
            List<String> path = generatePathForCoverage(startingLocation, coverageRequired, markovData);
             resultSet.add(path);
//            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = convertPathToGraph(path);
//            resultSet.add(tempGraph);
            if(count%100==0)
                progressVisualizer.print("graphNumber"+ count+"\n");

            progressVisualizer.setProgress((count*100) /PATHS_TO_GENERATE);

        }
        progressVisualizer.finish();


        return resultSet;
    }


    //TODO: Can replace this with something in MarkovDataStore
    private static List<String> generatePathForCoverage(ModelObject startingLocation,int coverageRequired, RecursiveHashMap markovData) {
        List<String> path = new ArrayList<String>();
//        path.addVertex(startingLocation);
        String startRoom = startingLocation.toString();
        path.add(startRoom);
        int verticesRequired = (coverageRequired *CompleteGraph.instance().getTotalNumberOfVertices())/100;
        HashSet<String> vertexSet = new HashSet<String>(verticesRequired);

        vertexSet.add(startRoom);


//        String destination = getDestination(
//                markovData.obtainHeatMap().rowMap().get(startRoom)); // TODO : verify that this works correctly.
//        if (destination.equals(startRoom)) {
//            System.out.println("Source same as destination");
//        }
//        ModelObject currentNode = addNewVertexToPath(destination, path);

        List<String> startingPath = markovData.getPathFromRoom(startRoom, random);

        path.addAll(startingPath);
        vertexSet.addAll(startingPath);
        ModelObject currentNode = CompleteGraph.instance().findRoomByName(path.get(path.size()-1));
//        if (pathLength == markovData.getOrder()) {
//            return path;
//        }


       while(vertexSet.size()<verticesRequired) {
            ModelObject tempNode = currentNode;
            currentNode = addOneMoreHop(path, markovData);
            vertexSet.add(currentNode.toString());
            if (currentNode == tempNode) {
                assert false;
                System.out.println("Error!!");
            }
        }

        return path;
    }
//
    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generatePathsForPathLength(
            RecursiveHashMap markovData,
            int pathLength) {


            Graph<ModelObject, ModelEdge> completeGraph = CompleteGraph.instance().getGraph();
            String startingRoom = CompleteGraph.instance().getStartingNode().toString();
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


//            CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);

            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                    new ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
//            List<Double> listOfGyrationRadius = new ArrayList<Double>();
            ProgressVisualizer progressVisualizer = new ProgressVisualizer("Generating path data", ProgressVisualizer.DeterminateType.DETERMINATE);

            for(int count=0;count<PATHS_TO_GENERATE; count++) {
                List<String> path = generatePathForLength(startingLocation, pathLength, markovData);

                DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = convertPathToGraph(path);
                resultSet.add(tempGraph);
                if(count%100==0)
                    progressVisualizer.print("graphNumber"+ count+"\n");

                progressVisualizer.setProgress((count*100) /PATHS_TO_GENERATE);

//                listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
//                if (isStable(listOfGyrationRadius, varianceList, progressVisualizer)) {

//                    break;
//                } else {
//                    count++;
//                    if (count % 50 == 0) {

//                        progressVisualizer.displayStability(count);

//                    }
//                }


//            System.out.println(resultSet.size());
            }
            progressVisualizer.finish();


            return resultSet;

    }

    private static DirectedSparseMultigraph<ModelObject, ModelEdge> convertPathToGraph(List<String> path) {
        DirectedSparseMultigraph<ModelObject, ModelEdge> resultGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        ModelObject currentRoom =null;
        ModelObject lastRoom=null;
        int count=0;
        for(String vertex: path){


            if(currentRoom!=null){
                lastRoom = currentRoom;

            }
            currentRoom = CompleteGraph.instance().findRoomByName(vertex);
            resultGraph.addVertex(currentRoom);
            if(lastRoom!=null){
                ModelEdge edge = new ModelEdge();
                edge.setTime(count++);
                resultGraph.addEdge(edge, lastRoom, currentRoom);
            }


        }
        return resultGraph;
    }


    private static List<String> generatePathForLength(
            ModelObject startingLocation, int pathLength,
            RecursiveHashMap markovData) {


        List<String> path = new ArrayList<String>();
//        path.addVertex(startingLocation);
        String startRoom = startingLocation.toString();
        path.add(startRoom);


//        String destination = getDestination(
//                markovData.obtainHeatMap().rowMap().get(startRoom)); // TODO : verify that this works correctly.
//        if (destination.equals(startRoom)) {
//            System.out.println("Source same as destination");
//        }
//        ModelObject currentNode = addNewVertexToPath(destination, path);

        List<String> startingPath = markovData.getPathFromRoom(startRoom, random);

        path.addAll(startingPath);
        ModelObject currentNode = CompleteGraph.instance().findRoomByName(path.get(path.size()-1));
        if (pathLength == markovData.getOrder()) {
            return path;
        }


        for (int hops = markovData.getOrder(); hops < pathLength; hops++) {
            ModelObject tempNode = currentNode;
            currentNode = addOneMoreHop(path, markovData);

            if (currentNode == tempNode) {
                assert false;
                System.out.println("Error!!");
            }
        }

        return path;

    }

    private static ModelObject addOneMoreHop(List<String> path,RecursiveHashMap markovData) {
        List<String> trimmedPath = trimSequence(markovData.getOrder(),path);

        Map<String, Double> nextLocationProbabilities = markovData.getDestinationProbabilities(trimmedPath);
        String destination = getDestination(nextLocationProbabilities);
//        if (destination.equals(lastNode.toString())) {
//            System.out.println("Source and destination are same!!");
//        }

        return addNewVertexToPath( destination, path);


    }


    /**
     * Adds new vertex to the path
     * @param destination
     * @param result
     * @return returns the room object
     */
    private static ModelObject addNewVertexToPath(String destination, List<String> result) {
        result.add(destination);

        return CompleteGraph.instance().findRoomByName(destination);

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

    public static int calculateCoverage(DirectedGraph<ModelObject, ModelEdge> graph) {

        int count = graph.getVertexCount();
        return (count * 100) / CompleteGraph.instance().getVertexCount();
    }

    private static HashMap<String, Double> calculateAverageNumberOfHops(Collection<List<String>> pathCollection) {
        double[] hopCountList = new double[pathCollection.size()];
        int i = 0;
        for (List<String> path : pathCollection) {

            hopCountList[i] = path.size();
            i++;
        }

        Mean meanEvaluator = new Mean();
        double mean = meanEvaluator.evaluate(hopCountList);

        StandardDeviation standardDeviationEvaluator = new StandardDeviation();
        double sd = standardDeviationEvaluator.evaluate(hopCountList, mean);

        HashMap<String, Double> result = new HashMap<String, Double>();
        result.put("mean", mean);
        result.put("sd", sd);

        return result;
    }

    public static HashMap<String, Double> calculateAverageCoverage(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection) {
//        int totalNumberOfVertices = CompleteGraph.instance().getVertexCount();
        double[] coverageValues = new double[graphCollection.size()];
        int i = 0;
        for (DirectedSparseMultigraph graph : graphCollection) {

            coverageValues[i] = calculateCoverage(graph);
            i++;
        }

        Mean meanEvaluator = new Mean();
        double mean = meanEvaluator.evaluate(coverageValues);

        StandardDeviation standardDeviationEvaluator = new StandardDeviation();
        double sd = standardDeviationEvaluator.evaluate(coverageValues, mean);

        HashMap<String, Double> result = new HashMap<String, Double>();
        result.put("mean", mean);
        result.put("sd", sd);

        return result;



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

    public static HashMap<String, Double> calculateHopsNeededForCoverage(RecursiveHashMap data, int coverageRequired) {


        Collection<List<String>> generatedPaths = generatePathsTillCoverage(data,
                coverageRequired);
        return calculateAverageNumberOfHops(generatedPaths);
    }




    public static HashMap<String, Double> calculateCoverageForPathLength(RecursiveHashMap data, int hopsRequired) {
        assert hopsRequired >= data.getOrder();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generatedPaths = generatePathsForPathLength(data,
                hopsRequired);
        return calculateAverageCoverage(generatedPaths);
    }


    public static List<String> trimSequence(int m, List<String> sequence) {
        List<String> result = new ArrayList<String>();
        for (int i = sequence.size() - 1; result.size() < m; i--) {
            result.add(0, sequence.get(i));

        }
        assert result.size() == m;
        assert result.get(result.size() - 1).equals(sequence.get(sequence.size() - 1));
        return result;

        //To change body of created methods use File | Settings | File Templates.
    }
}
