package randomwalk;


import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import java.util.ArrayList;

import static modelcomponents.GraphUtilities.calculateCoverage;
import static randomwalk.RandomWalkOrganizer.random;


/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/13/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */
class FirstOrderBiasedRandomWalk implements RandomWalkGenerator{


    public DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(ModelObject startingLocation) {
        DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        int coverage = calculateCoverage(currentGraph);
        int time = 0;
        ModelObject currentLocation = startingLocation;
        currentGraph.addVertex(currentLocation);
        ArrayList<ModelObject> neighbours = new ArrayList<ModelObject>();
        ModelObject lastVisited = null;


        while (coverage < 100) {
            neighbours.clear();
            neighbours.addAll(CompleteGraph.instance().getNeighbors(currentLocation));
            if (neighbours.size() > 1 && lastVisited != null) {
                neighbours.remove(lastVisited);
            }



            ModelObject next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));

            currentGraph.addVertex(next);
            ModelEdge edge = new ModelEdge();
            edge.setTime(time++);
            currentGraph.addEdge(edge, currentLocation, next);
            lastVisited = currentLocation;
            currentLocation = next;
            coverage = calculateCoverage(currentGraph);

        }

        return currentGraph;

    }
}
