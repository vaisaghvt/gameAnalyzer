package stats.statisticshandlers;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.ProgressVisualizer;
import gui.StatsDialog;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class FirstOrderPathHeatMapHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay> {


    private static final boolean USE_FIRST_ORDER_BIASED_RANDOM = true;

    public FirstOrderPathHeatMapHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne);
        super.actualGenerateAndDisplay(task);
    }

    private HashMap<String, HashMap<String, Double>> getFirstOrderMarkovData(
            final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap) {

        final ProgressVisualizer pv = new ProgressVisualizer("Generating first order probabilities", ProgressVisualizer.DeterminateType.DETERMINATE);
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        SwingWorker<HashMap<String, HashMap<String, Integer>>, Void> firstOrderProbabilityCalculator = new SwingWorker<HashMap<String, HashMap<String, Integer>>, Void>() {
            @Override
            protected HashMap<String, HashMap<String, Integer>> doInBackground() throws Exception {
                int numberOfRooms = CompleteGraph.instance().getSortedRooms().size();
                int count = 0;
                HashMap<String, HashMap<String, Integer>> nodeToNodeFrequencyTable = new HashMap<String, HashMap<String, Integer>>();
                this.addPropertyChangeListener(pv);
                HashMap<String, HashMap<String, HashMap<String, Double>>> result = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
                Collection<ModelObject> rooms = CompleteGraph.instance().getVertices();

                for (ModelObject vertex : rooms) {
                    pv.print("processing " + vertex.toString() + "...\n");
                    DirectedSparseMultigraph<ModelObject, ModelEdge> localGraphAtVertex =
                            calculateLocalGraphAtVertex(vertex, dataNameDataMap);


                    addToNodeToNodeTable(localGraphAtVertex, nodeToNodeFrequencyTable);

                    setProgress((count++ * 100) / numberOfRooms);

                }

                return nodeToNodeFrequencyTable;

            }

            @Override
            protected void done() {
                pv.finish();
                semaphore.release();
            }
        };

        firstOrderProbabilityCalculator.execute();

        HashMap<String, HashMap<String, Integer>> nodeToNodeFrequencyTable;
        try {
            semaphore.tryAcquire(1, 600, TimeUnit.SECONDS);
            nodeToNodeFrequencyTable = firstOrderProbabilityCalculator.get();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }


        HashMap<String, HashMap<String, Double>> result = summarizeToProbTable(nodeToNodeFrequencyTable);


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


        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
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

            for (ModelEdge inEdge : tempGraph.getIncidentEdges(mainNode)) {
                localGraph.addEdge(inEdge, tempGraph.getSource(inEdge), tempGraph.getDest(inEdge));
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

        }

        @Override
        protected void doTasks(String dataName) {
            if (!dataName.equals("random walk")) {

                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, Collections.singleton(phase)));

            }
        }

        @Override
        protected void summarizeAndDisplay() {
            System.out.println("Done");
            switch (allOrOne) {

                case ALL:
                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        public HashMap<String, HashMap<String, Double>> data;

                        @Override
                        protected Void doInBackground() throws Exception {
                            data = getFirstOrderMarkovData(dataNameDataMap);
                            data = subtractRandomWalk(data);
                            return null;
                        }

                        protected void done() {
                            chartDisplay.setTitle("First order heat map :" + phase.toString());
                            chartDisplay.display(data);
                            consoleDisplay.display(data);
                        }
                    };
                    worker.execute();
                    break;
                case EACH:

                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }
                    for (final String name : dataNames) {

                        final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> tempDataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                        tempDataNameDataMap.put(name, dataNameDataMap.get(name));
                        SwingWorker<Void, Void> worker2 = new SwingWorker<Void, Void>() {
                            public HashMap<String, HashMap<String, Double>> data;

                            @Override
                            protected Void doInBackground() throws Exception {
                                data = getFirstOrderMarkovData(tempDataNameDataMap);
                                data = subtractRandomWalk(data);
                                return null;
                            }

                            protected void done() {
                                chartDisplay.setTitle("First order heat map :" + name + ":" + phase.toString());
                                chartDisplay.display(data);
                                consoleDisplay.display(data);

                            }
                        };
                        worker2.execute();

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
            collectionOfGraphs = RandomWalkOrganizer.getAllRandomWalkGraphs(new Semaphore(1));

            return null;
        }

        @Override
        protected void done() {
            int count = 0;
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> resultMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
            for (DirectedSparseMultigraph<ModelObject, ModelEdge> graph : collectionOfGraphs) {
                resultMap.put("randomWalk" + (count++), graph);
            }

            HashMap<String, HashMap<String, Double>> dataToDisplay = getFirstOrderMarkovData(resultMap);
            dataToDisplay = subtractRandomWalk(dataToDisplay);
            chartDisplay.setTitle("First order heat map random walk");
            chartDisplay.display(dataToDisplay);
            consoleDisplay.display(dataToDisplay);

        }

    }

    private HashMap<String, HashMap<String, Double>> subtractRandomWalk(HashMap<String, HashMap<String, Double>> data) {

        final Semaphore generatorSemaphore = new Semaphore(1);
        try {
            generatorSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> randomWalkGenerator = new SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>() {
            @Override
            protected Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {
                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> collectionOfGraphs;


                    collectionOfGraphs = RandomWalkOrganizer.getAllRandomWalkGraphs(generatorSemaphore);
                return collectionOfGraphs;
            }
        };
        randomWalkGenerator.execute();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> collectionOfGraphs = null;
        try {
            generatorSemaphore.tryAcquire(1, 300, TimeUnit.SECONDS);
            collectionOfGraphs = randomWalkGenerator.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Calculating new Data");
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> resultMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        int count = 0;
        for (DirectedSparseMultigraph<ModelObject, ModelEdge> graph : collectionOfGraphs) {
            resultMap.put("randomWalk" + (count++), graph);
        }

        HashMap<String, HashMap<String, Double>> randomWalkData = getFirstOrderMarkovData(resultMap);

        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
        for (String sourceRoom : randomWalkData.keySet()) {
            result.put(sourceRoom, new HashMap<String, Double>());
            for (String destinationRoom : randomWalkData.get(sourceRoom).keySet()) {
                if (data.containsKey(sourceRoom) && data.get(sourceRoom).containsKey(destinationRoom)) {
                    result.get(sourceRoom).put(destinationRoom,
                            data.get(sourceRoom).get(destinationRoom) - randomWalkData.get(sourceRoom).get(destinationRoom));
                } else {
                    result.get(sourceRoom).put(destinationRoom, -randomWalkData.get(sourceRoom).get(destinationRoom));
                }

            }
        }

        return result;  //To change body of created methods use File | Settings | File Templates.
    }


}

