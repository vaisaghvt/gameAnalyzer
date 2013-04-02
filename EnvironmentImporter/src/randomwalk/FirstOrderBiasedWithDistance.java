package randomwalk;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import javafx.geometry.Point3D;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import java.util.ArrayList;
import java.util.HashMap;

import static modelcomponents.GraphUtilities.calculateCoverage;
import static modelcomponents.GraphUtilities.getCenterOfArea;
import static randomwalk.RandomWalkOrganizer.random;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/28/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
class FirstOrderBiasedWithDistance implements RandomWalkGenerator {

    private static final int REQUIRED_COVERAGE = 90;
    private static final double SHORTEST_DISTANCE_PREFERENCE = 0.15;

    public DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(Graph<ModelObject, ModelEdge> graph, ModelObject startingLocation) {
        DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        int coverage = calculateCoverage(graph, currentGraph);
        int time = 0;
        ModelObject currentLocation = startingLocation;
        currentGraph.addVertex(currentLocation);
        ArrayList<ModelObject> neighbours = new ArrayList<ModelObject>();
        ModelObject lastVisited = null;


        while (coverage < REQUIRED_COVERAGE) {
            neighbours.clear();
            neighbours.addAll(graph.getNeighbors(currentLocation));
            if (neighbours.size() > 1 && lastVisited != null) {
                neighbours.remove(lastVisited);
            }


            ModelObject next = null;

            if (lastVisited != null) {
                HashMap<ModelObject, Double> probabilityMap = generateProbabilities(lastVisited, neighbours);
                double randomValue = random.nextDouble();
                double sum = 0;
                for (ModelObject neighbour : probabilityMap.keySet()) {
                    sum += probabilityMap.get(neighbour);
                    if (sum > randomValue) {
                        next = neighbour;
                        break;
                    }

                }
//                next = findNearestDestination(lastVisited, neighbours);
            } else {
                next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));
            }
            currentGraph.addVertex(next);
            ModelEdge edge = new ModelEdge();
            edge.setTime(time++);
            currentGraph.addEdge(edge, currentLocation, next);
            lastVisited = currentLocation;
            currentLocation = next;
            coverage = calculateCoverage(graph, currentGraph);

        }

        return currentGraph;

    }

//    private static ModelObject findNearestDestination(ModelObject lastVisited, ArrayList<ModelObject> neighbours) {
//
//        if (neighbours.size() == 1) {
//            return neighbours.get(0);
//        }
//        Point3D currentCenter = getCenterOfArea(lastVisited);
//
//        double secondMinDistance = Double.MAX_VALUE;
//        ModelObject result = null;
//        double randomNoise = random.nextDouble();
//        if (randomNoise > SHORTEST_DISTANCE_PREFERENCE) {
//            return neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));
//        }
//
//
//        double minDistance = Double.MAX_VALUE;
//        ModelObject nearestNeighbour = null;
//        ModelObject secondNearestNeighbour = null;
//        for (ModelObject neighbour : neighbours) {
//            double distance = currentCenter.distance(getCenterOfArea(neighbour));
//            if (distance < minDistance) {
//                secondMinDistance = minDistance;
//                minDistance = distance;
//
//                nearestNeighbour = neighbour;
//            } else if (distance < secondMinDistance) {
//                secondMinDistance = distance;
//                secondNearestNeighbour = neighbour;
//            }
//        }
//
//        if (secondMinDistance - minDistance < 1) {
//            result = random.nextDouble() > 0.5 ? secondNearestNeighbour : nearestNeighbour;
//        } else {
//            result = nearestNeighbour;
//        }
//
//        if(result==)
//        return result;
//
//    }

    private static HashMap<ModelObject, Double> generateProbabilities(ModelObject currentLocation, ArrayList<ModelObject> neighbours) {
        HashMap<ModelObject, Double> destinationProbabilities = new HashMap<ModelObject, Double>();

        if (neighbours.size() == 1) {
            destinationProbabilities.put(neighbours.get(0), 1.0);
            return destinationProbabilities;
        }
        Point3D currentCenter = getCenterOfArea(currentLocation);
        HashMap<ModelObject, Double> destinationDistanceMap = new HashMap<ModelObject, Double>();
        double total = 0;
        for (ModelObject neighbour : neighbours) {
            double distance = currentCenter.distance(getCenterOfArea(neighbour));
            destinationDistanceMap.put(neighbour, distance);
            total += distance;
        }
        double newTotal = 0;
        for (ModelObject destination : destinationDistanceMap.keySet()) {
            double newValue = total - destinationDistanceMap.get(destination);
            destinationDistanceMap.put(destination, newValue);
            newTotal += newValue;
        }
        for (ModelObject destination : destinationDistanceMap.keySet()) {
            destinationProbabilities.put(destination, destinationDistanceMap.get(destination) / newTotal);
        }
//        System.out.println(destinationProbabilities);
        return destinationProbabilities;
    }
}
