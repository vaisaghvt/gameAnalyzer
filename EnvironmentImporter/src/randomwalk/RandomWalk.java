package randomwalk;


import ec.util.MersenneTwister;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import gui.NetworkModel;
import javafx.geometry.Point3D;
import modelcomponents.ModelArea;
import modelcomponents.ModelEdge;
import modelcomponents.ModelGroup;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.awt.*;
import java.util.*;
import java.util.List;


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
    private static final double EPSILON = 0.0001;
    private Collection<DirectedGraph<ModelObject, ModelEdge>> randomWalkGraphs;
    private static RandomWalk randomWalkInstance;

    private RandomWalk() {
    }

    public static RandomWalk instance() {
        if (randomWalkInstance == null) {
            randomWalkInstance = new RandomWalk();
        }
        return randomWalkInstance;

    }

    public void generateRandomWalkCollection(Graph<ModelObject, ModelEdge> graph, ModelObject startingLocation, int numberOfGraphsToGenerate) {
        Collection<DirectedGraph<ModelObject, ModelEdge>> resultSet = new HashSet<DirectedGraph<ModelObject, ModelEdge>>();

        for (int i = 0; i < numberOfGraphsToGenerate; i++) {
            resultSet.add(generateRandomWalk(graph, startingLocation));
        }

        this.randomWalkGraphs = resultSet;

    }

    public void generateRandomWalkCollection(Graph<ModelObject, ModelEdge> graph, ModelObject startingLocation) {
        varianceList.clear();
        Collection<DirectedGraph<ModelObject, ModelEdge>> resultSet = new HashSet<DirectedGraph<ModelObject, ModelEdge>>();
        List<Double> listOfGyrationRadius = new ArrayList<Double>();

        while (true) {

            DirectedGraph<ModelObject, ModelEdge> tempGraph = generateRandomWalk(graph, startingLocation);
            resultSet.add(tempGraph);
            listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
            if (isStable(listOfGyrationRadius)) {
                break;
            }


//            System.out.println(resultSet.size());
        }

        this.randomWalkGraphs = resultSet;
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

        if (varianceEvaluator.evaluate(primitiveVariance, mean) < EPSILON) {
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


    private static DirectedGraph<ModelObject, ModelEdge> generateRandomWalk(Graph<ModelObject, ModelEdge> graph, ModelObject startingLocation) {

        DirectedGraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
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

    public HashMap<String, Double> subtractRandomWalkFromRoomToVisitMapping(HashMap<String, Double> playerMap) {
        if (this.randomWalkGraphs == null) {
            return playerMap;
        }
        HashMap<String, Number> averageOfRandomWalks = calculateAverageRoomVisitFrequency();
        return normalizedResult(playerMap, averageOfRandomWalks);
    }

    private HashMap<String, Double> normalizedResult(HashMap<String, Double> playerMap, HashMap<String, Number> averageOfRandomWalks) {

        for (String roomName : playerMap.keySet()) {
            playerMap.put(roomName, playerMap.get(roomName) - averageOfRandomWalks.get(roomName).doubleValue());
        }
        return playerMap;
    }

    public HashMap<String, Number> calculateAverageRoomVisitFrequency() {

        HashMap<String, Number> result = new HashMap<String, Number>();
//        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : this.randomWalkGraphs) {
            for (ModelObject vertex : graph.getVertices()) {
                double count = 0;
                if (result.containsKey(vertex.toString())) {
                    count = result.get(vertex.toString()).doubleValue();
                }
//                int numberOfEdges = roomEdgeCountMapping.get(vertex.toString());

//                result.put(vertex.toString(), (graph.inDegree(vertex) / numberOfEdges) + count);
                 result.put(vertex.toString(), (graph.inDegree(vertex)+count));

            }
        }

        for (String roomName : result.keySet()) {
            result.put(roomName, result.get(roomName).doubleValue() / this.randomWalkGraphs.size());
        }
        return result;

    }

    public HashMap<String, Number> calculateNormalizedAverageRoomVisitFrequency() {

        HashMap<String, Number> result = new HashMap<String, Number>();
        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (DirectedGraph<ModelObject, ModelEdge> graph : this.randomWalkGraphs) {
            for (ModelObject vertex : graph.getVertices()) {
                double count = 0;
                if (result.containsKey(vertex.toString())) {
                    count = result.get(vertex.toString()).doubleValue();
                }
                int numberOfEdges = roomEdgeCountMapping.get(vertex.toString());

                result.put(vertex.toString(), (graph.inDegree(vertex) / numberOfEdges) + count);


            }
        }

        for (String roomName : result.keySet()) {
            result.put(roomName, result.get(roomName).doubleValue() / this.randomWalkGraphs.size());
        }
        return result;

    }

}
