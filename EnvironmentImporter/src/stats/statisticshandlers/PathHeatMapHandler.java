package stats.statisticshandlers;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathHeatMapHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay> {


    public PathHeatMapHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne);
        super.actualGenerateAndDisplay(task);
    }



    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        HashMap<String,DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        private final StatsDialog.AllOrOne allOrOne;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne) {
            super(dataNames);
            this.phase = phase;
            this.allOrOne = allOrOne;
        }

        @Override
        protected void doTasks(String dataName) {
            synchronized (NetworkModel.instance()) {
                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, Collections.singleton(phase)));
            }
        }

        @Override
        protected void summarizeAndDisplay() {
            HashMap<String,HashMap<String, Double>> data;

            switch (allOrOne) {

                case ALL:
                    data = getSecondOrderMarkovData(dataNameDataMap);
                    chartDisplay.setTitle("Visit order heat map :" + phase.toString());
                    chartDisplay.display(data);
                    consoleDisplay.display(data);
                    break;
                case EACH:
                    HashMap<String,DirectedSparseMultigraph<ModelObject, ModelEdge>> tempDataNameDataMap
                            = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                    for(String name: dataNames){
                        tempDataNameDataMap.put(name, dataNameDataMap.get(name));
                        data = getSecondOrderMarkovData(dataNameDataMap);
                        chartDisplay.setTitle("Visit order heat map :"+name+":" + phase.toString());
                        chartDisplay.display(data);
                        consoleDisplay.display(data);
                        tempDataNameDataMap.clear();
                    }
                    break;
            }

        }

        private HashMap<String, HashMap<String, Double>> getSecondOrderMarkovData(
                HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap) {

            HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();

           Collection<ModelObject> rooms = NetworkModel.instance().getCompleteGraph().getVertices();
                for(ModelObject vertex : rooms){
                    DirectedSparseMultigraph<ModelObject, ModelEdge> localGraphAtVertex =
                                calculateLocalGraphAtVertex(vertex, dataNameDataMap);


                    addProbabilitiesToTable(vertex, localGraphAtVertex, result);

                }





            return result;
        }

        /**
         * This assumes that Node1ToNode2 is possible only through some other node.
         * @param localGraph
         * @param nodeToNodeProbabilities
         */
        private void addProbabilitiesToTable( ModelObject start,
                DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, HashMap<String, HashMap<String, Double>> nodeToNodeProbabilities) {
            HashMap<String, HashMap<String, Integer>> nodeToNodeTravelFrequency
                    = new HashMap<String, HashMap<String, Integer>>();

            for (ModelEdge edge : localGraph.getEdges()) {
                String source = localGraph.getSource(edge).toString();
                String destination = localGraph.getDest(edge).toString();


                if (!nodeToNodeTravelFrequency.containsKey(source)) {
                    nodeToNodeTravelFrequency.put(source,
                            new HashMap<String, Integer>());
                }
                if (!nodeToNodeTravelFrequency.get(source).containsKey(destination)) {
                    nodeToNodeTravelFrequency.get(source).put(destination, 0);
                }

                int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
                nodeToNodeTravelFrequency.get(source).put(destination, currentValue + 1);

            }


            for (String source : nodeToNodeTravelFrequency.keySet()) {
                if(!nodeToNodeProbabilities.containsKey(source)){
                    nodeToNodeProbabilities.put(source, new HashMap<String, Double>());
                }
                double totalNumberOfOutEdges = 0.0;
                for (String destination : nodeToNodeTravelFrequency.get(source).keySet()) {
                    totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source)
                            .get(destination);
                }
                for (String destination : nodeToNodeTravelFrequency.get(source).keySet()) {
                    if(source.equals(destination)){
                        System.out.println("Through "+start.toString()+ " self loop at "+ source +" prob. = "+
                                (double) nodeToNodeTravelFrequency.get(source).get(destination) / totalNumberOfOutEdges);
                        continue;
                    }

                    nodeToNodeProbabilities.get(source).put(destination,
                            (double) nodeToNodeTravelFrequency.get(source).get(destination) / totalNumberOfOutEdges);
                }


            }

        }

        private DirectedSparseMultigraph<ModelObject, ModelEdge> calculateLocalGraphAtVertex(
                ModelObject mainNode, HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {

            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();


            localGraph.addVertex(mainNode);

            for(ModelObject room: localGraph.getNeighbors(mainNode)){
                localGraph.addVertex(room);
            }
            for (String name : nameToGraphMap.keySet()) {
                DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

                if (!tempGraph.containsVertex(mainNode)) {
                    continue;
                }

                Collection<ModelEdge> edgeCollection = tempGraph.getInEdges(mainNode);

                for (ModelEdge edge : edgeCollection) {

                    localGraph.addEdge(new ModelEdge(), tempGraph.getOpposite(mainNode, edge),
                            mainNode);

                }


                edgeCollection = tempGraph.getOutEdges(mainNode);

                for (ModelEdge edge : edgeCollection) {

                    localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
                }

            }


            return localGraph;
        }
    }


}
