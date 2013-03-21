package stats.statisticshandlers;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalk;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import javax.swing.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecondOrderPathHeatMapHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay> {


    public SecondOrderPathHeatMapHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne);
        super.actualGenerateAndDisplay(task);
    }

    private HashMap<String, HashMap<String, Double>> getSecondOrderMarkovData(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap) {


        HashMap<String, HashMap<String, Integer>> nodeToNodeFrequencyTable = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();

        Collection<ModelObject> rooms = NetworkModel.instance().getCompleteGraph().getVertices();
        for (ModelObject vertex : rooms) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraphAtVertex =
                    calculateLocalGraphAtVertex(vertex, dataNameDataMap);


            addToNodeToNodeTable(localGraphAtVertex, nodeToNodeFrequencyTable);

        }

        result = summarizeToProbTable(nodeToNodeFrequencyTable);


        return result;
    }

    private HashMap<String, HashMap<String, Double>> summarizeToProbTable(HashMap<String, HashMap<String, Integer>> nodeToNodeTravelFrequency) {
        HashMap<String, HashMap<String, Double>> nodeToNodeProbabilities = new HashMap<String, HashMap<String, Double>>();
        for (String source : nodeToNodeTravelFrequency.keySet()) {
            if (!nodeToNodeProbabilities.containsKey(source)) {
                nodeToNodeProbabilities.put(source, new HashMap<String, Double>());
            }
            double totalNumberOfOutEdges = 0.0;
            for (String destination : nodeToNodeTravelFrequency.get(source).keySet()) {
                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source)
                        .get(destination);
            }
            for (String destination : nodeToNodeTravelFrequency.get(source).keySet()) {


                nodeToNodeProbabilities.get(source).put(destination,
                        (double) nodeToNodeTravelFrequency.get(source).get(destination) / totalNumberOfOutEdges);
            }


        }
        return nodeToNodeProbabilities;
    }

    private void addToNodeToNodeTable(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, HashMap<String, HashMap<String, Integer>> nodeToNodeFrequencyTable) {


        for (ModelEdge edge : localGraph.getEdges()) {
            String source = localGraph.getSource(edge).toString();
            String destination = localGraph.getDest(edge).toString();


            if (!nodeToNodeFrequencyTable.containsKey(source)) {
                nodeToNodeFrequencyTable.put(source,
                        new HashMap<String, Integer>());
            }
            if (!nodeToNodeFrequencyTable.get(source).containsKey(destination)) {
                nodeToNodeFrequencyTable.get(source).put(destination, 0);
            }

            int currentValue = nodeToNodeFrequencyTable.get(source).get(destination).intValue();
            nodeToNodeFrequencyTable.get(source).put(destination, currentValue + 1);

        }
    }


    private DirectedSparseMultigraph<ModelObject, ModelEdge> calculateLocalGraphAtVertex(
            ModelObject mainNode, HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {


        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

//        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });

            edgeCollection.addAll(tempGraph.getIncidentEdges(mainNode));

            if (tempGraph.inDegree(mainNode) > tempGraph.outDegree(mainNode)) {
//                    //Ending Vertex;
                edgeCollection.pollLast();
//
//                    localGraph.addVertex(mainNode);
//                    localGraph.addEdge(new ModelEdge(),
//                            tempGraph.getOpposite(mainNode, lonelyEdge),
//                            mainNode
//                    );

            } else if (tempGraph.inDegree(mainNode) < tempGraph.outDegree(mainNode)) {
                //First Vertex;
                edgeCollection.pollFirst();
//
//
//                    localGraph.addVertex(mainNode);
//                    localGraph.addEdge(new ModelEdge(),
//                            mainNode,
//                            tempGraph.getOpposite(mainNode, lonelyEdge)
//                    );

            }

            for (int i = 0; i < edgeCollection.size() / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);
            }

        }
        return localGraph;
    }


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        private final StatsDialog.AllOrOne allOrOne;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne) {
            super(dataNames);
            this.phase = phase;
            this.allOrOne = allOrOne;
            if (allOrOne == StatsDialog.AllOrOne.ALL) {
                dataNames.remove("random walk");

            }
        }

        @Override
        protected void doTasks(String dataName) {
            if (!dataName.equals("random walk")) {
                synchronized (NetworkModel.instance()) {
                    dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, Collections.singleton(phase)));
                }
            }
        }

        @Override
        protected void summarizeAndDisplay() {
            HashMap<String, HashMap<String, Double>> data;
            switch (allOrOne) {

                case ALL:
                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }

                    data = getSecondOrderMarkovData(dataNameDataMap);
                    chartDisplay.setTitle("Second order heat map :" + phase.toString());
                    chartDisplay.display(data);
                    consoleDisplay.display(data);
                    break;
                case EACH:
                    HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> tempDataNameDataMap
                            = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }
                    for (String name : dataNames) {

                        tempDataNameDataMap.put(name, dataNameDataMap.get(name));

                        data = getSecondOrderMarkovData(tempDataNameDataMap);
                        chartDisplay.setTitle("Second order heat map :" + name + ":" + phase.toString());
                        chartDisplay.display(data);
                        consoleDisplay.display(data);
                        tempDataNameDataMap.clear();
                    }
                    break;
            }

        }


    }

    private class RandomWalkCalculator extends SwingWorker<Void, Void> {


        public Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> collectionOfGraphs;

        @Override
        protected Void doInBackground() throws Exception {
            collectionOfGraphs
                    = RandomWalk.getAllRandomWalkGraphs();
            ;
            return null;
        }

        @Override
        protected void done() {
            int count = 0;
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> resultMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
            for (DirectedSparseMultigraph<ModelObject, ModelEdge> graph : collectionOfGraphs) {


                resultMap.put("randomWalk" + (count++), graph);
            }

            HashMap<String, HashMap<String, Double>> dataToDisplay = getSecondOrderMarkovData(resultMap);
            chartDisplay.setTitle("Second order heat map random walk");
            chartDisplay.display(dataToDisplay);
            consoleDisplay.display(dataToDisplay);

        }

    }
}
