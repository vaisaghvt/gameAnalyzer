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
class UnbiasedRandomWalk implements RandomWalkGenerator{

    @Override
    public DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(ModelObject startingLocation) {

            DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
            int coverage = calculateCoverage(currentGraph);
            int time = 0;
            ModelObject currentLocation = startingLocation;
            currentGraph.addVertex(currentLocation);
            while (coverage < 100) {
                ArrayList<ModelObject> neighbours = new ArrayList<ModelObject>();
                neighbours.addAll(CompleteGraph.instance().getNeighbors(currentLocation));
                ModelObject next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));
                currentGraph.addVertex(next);
                ModelEdge edge = new ModelEdge();
                edge.setTime(time++);
                currentGraph.addEdge(edge, currentLocation, next);
                currentLocation = next;
                coverage = calculateCoverage(currentGraph);

            }

            return currentGraph;


    }
}
