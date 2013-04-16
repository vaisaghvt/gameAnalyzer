package randomwalk;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/28/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RandomWalkGenerator {
    public DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(ModelObject startingLocation);


}
