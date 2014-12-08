package randomwalk;


import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;

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
class NthOrderBiasedRandomWalk implements RandomWalkGenerator{

    private static final int REQUIRED_COVERAGE = 100;
    private final int MEMORY_SIZE;

    public NthOrderBiasedRandomWalk(int n) {
        this.MEMORY_SIZE =n;
    }


    public DirectedSparseMultigraph<ModelObject, ModelEdge> generateRandomWalk(ModelObject startingLocation) {
        DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        int coverage = calculateCoverage(currentGraph);
        int time = 0;
        ModelObject currentLocation = startingLocation;
        currentGraph.addVertex(currentLocation);
        ArrayList<ModelObject> neighbours = new ArrayList<ModelObject>();
        CircularFifoBuffer<ModelObject> previousNVisits = new CircularFifoBuffer<ModelObject>(MEMORY_SIZE);
        CircularFifoBuffer<Boolean> isDecisionPoints = new CircularFifoBuffer<Boolean>(MEMORY_SIZE);



        while (coverage < REQUIRED_COVERAGE && currentGraph.getEdgeCount()<5000) {
            neighbours.clear();
            neighbours.addAll(CompleteGraph.instance().getNeighbors(currentLocation));
            if (!previousNVisits.isEmpty()) {
                neighbours.removeAll(previousNVisits);
            }

            if(!neighbours.isEmpty()){

                ModelObject next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));

                time =addNewVertex(currentGraph,next, time, currentLocation, previousNVisits);

                if(neighbours.size()>1)  {
                    //i.e. it had more than the one decision point.
                    isDecisionPoints.add(Boolean.TRUE);
                } else {
                    isDecisionPoints.add(Boolean.FALSE);
                }
                currentLocation = next;

            }else {
                boolean random_walk_needed = true;
                int lastUnexploredVertexIndex = -1;
                ArrayList<Boolean> decisionPointArray = new ArrayList<Boolean>(isDecisionPoints);



                for(int i=isDecisionPoints.size()-1;i>=0;i--){
                    Boolean decisionPoint = decisionPointArray.get(i);
                    if (decisionPoint){
                        lastUnexploredVertexIndex =i;
                        random_walk_needed = false;
                        break;
                    }
                }
                if (random_walk_needed){
                    neighbours.clear();
                    neighbours.addAll(CompleteGraph.instance().getNeighbors(currentLocation));
                    ModelObject next = neighbours.get((int) Math.floor(random.nextDouble() * neighbours.size()));

                    time =addNewVertex(currentGraph,next, time, currentLocation, previousNVisits);

                    isDecisionPoints.add(Boolean.FALSE);

                    currentLocation = next;
                } else {
                    ArrayList<ModelObject> locationArray = new ArrayList<ModelObject>(previousNVisits);
                    for(int j=previousNVisits.size()-1;j>=lastUnexploredVertexIndex;j-- ){
                        ModelObject next= locationArray.remove(j);

                        time =addNewVertex(currentGraph,next, time, currentLocation, previousNVisits);

                        isDecisionPoints.add(Boolean.FALSE);
                        currentLocation = next;
                    }

                }

            }
            coverage = calculateCoverage(currentGraph);
        }
//        if(coverage!=REQUIRED_COVERAGE){
//            System.out.println("Approximated");
//        }else {
//            System.out.println(currentGraph.getEdgeCount());
//        }

        return currentGraph;

    }

    private int addNewVertex(DirectedSparseMultigraph<ModelObject, ModelEdge> currentGraph, ModelObject next, int time,
                             ModelObject currentLocation, CircularFifoBuffer<ModelObject> previousNVisits) {
        currentGraph.addVertex(next);
        ModelEdge edge = new ModelEdge();
        edge.setTime(time++);
        currentGraph.addEdge(edge, currentLocation, next);
        previousNVisits.add(currentLocation);
        return time;
    }


}
