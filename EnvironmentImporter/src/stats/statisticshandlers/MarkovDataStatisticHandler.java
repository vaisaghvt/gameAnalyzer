package stats.statisticshandlers;

import com.google.common.collect.HashBasedTable;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import markovmodeldata.MarkovDataOrganizer;
import markovmodeldata.RecursiveHashMap;
import modelcomponents.CompleteGraph;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarkovDataStatisticHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay>
        implements ActionListener {

    private int order;
    private Collection<String> dataNames;
    private Phase phase;

    private MarkovDataDialog markovDataDialog;
    private MarkovDataDialog.HeatMapType heatMapType;
    private MarkovDataDialog.GraphType graph1Type;
    private RandomWalkOrganizer.RandomWalkType graph1RandomWalkType;
    private MarkovDataDialog.HumanType graph1HumanDataType;
    private int graph1M;
    private MarkovDataDialog.GraphType graph2Type;
    private RandomWalkOrganizer.RandomWalkType graph2RandomWalkType;
    private int graph2M;
    private MarkovDataDialog.HumanType graph2HumanDataType;

    public MarkovDataStatisticHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne,
                                        StatsDialog.AggregationType aggregationType) {

        markovDataDialog = new MarkovDataDialog(this);
        this.dataNames = dataNames;
        this.phase = phase;
        this.order = -1;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        graph1RandomWalkType = null;
        graph1HumanDataType = null;
        graph1M = 0;

        graph2RandomWalkType = null;
        graph2HumanDataType = null;
        graph2M = 0;

        if (e.getSource() == markovDataDialog.generateHeatMapButton) {
            order = markovDataDialog.getOrder();
            heatMapType = markovDataDialog.getHeatMapType();
            graph1Type = markovDataDialog.getGraph1Type();
            if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = markovDataDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = markovDataDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == MarkovDataDialog.HumanType.N_FROM_M) {
                    graph1M = markovDataDialog.getGraph1m();
                }
            }

            if (heatMapType == MarkovDataDialog.HeatMapType.COMPARISON) {
                graph2Type = markovDataDialog.getGraph2Type();
                if (graph2Type == MarkovDataDialog.GraphType.RANDOM_WALK) {
                    graph2RandomWalkType = markovDataDialog.getGraph2RandomWalkType();
                } else {
                    graph2HumanDataType = markovDataDialog.getGraph2HumanType();
                    if (order > 1 && graph2HumanDataType == MarkovDataDialog.HumanType.N_FROM_M) {
                        graph2M = markovDataDialog.getGraph2m();
                    }
                }
            }

            chartDisplay.setType(heatMapType);

            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, order);
            super.actualGenerateAndDisplay(task);
//            markovDataDialog.dispose();
        } else if (e.getSource() == markovDataDialog.calculateHopsForCoverage) {
            order = markovDataDialog.getOrder();
            heatMapType = markovDataDialog.getHeatMapType();
            graph1Type = markovDataDialog.getGraph1Type();
            if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = markovDataDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = markovDataDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == MarkovDataDialog.HumanType.N_FROM_M) {
                    graph1M = markovDataDialog.getGraph1m();
                }
            }
            int coverageRequired = markovDataDialog.getCoverageRequired();

            if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK || graph1HumanDataType == MarkovDataDialog.HumanType.DIRECT) {

                CalculateHopsNeededTask task = new CalculateHopsNeededTask(dataNames, phase, order, coverageRequired);
                super.actualGenerateAndDisplay(task);
//                markovDataDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == markovDataDialog.calculateCoverageForHopsButton) {
            order = markovDataDialog.getOrder();
            heatMapType = markovDataDialog.getHeatMapType();
            graph1Type = markovDataDialog.getGraph1Type();
            if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = markovDataDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = markovDataDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == MarkovDataDialog.HumanType.N_FROM_M) {
                    graph1M = markovDataDialog.getGraph1m();
                }
            }
            int hopsRequired = markovDataDialog.getHopsRequired();
            if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK || graph1HumanDataType == MarkovDataDialog.HumanType.DIRECT) {
                CalculateCoverageTask task = new CalculateCoverageTask(dataNames, phase, order, hopsRequired);
                super.actualGenerateAndDisplay(task);
//                markovDataDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                        JOptionPane.ERROR_MESSAGE);
            }


        }
    }


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final int order;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String,
                DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, int order) {
            super(dataNames);
            this.phase = phase;
            this.order = order;

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

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public HashBasedTable<String, String, Double> data;

                @Override
                protected Void doInBackground() throws Exception {

                    try {
                        data = getNthOrderMarkovHeatMap(dataNameDataMap, graph1Type, graph1RandomWalkType, graph1HumanDataType, graph1M);
                        assert data != null;
                        if (heatMapType == MarkovDataDialog.HeatMapType.COMPARISON) {
                            HashBasedTable<String, String, Double> data2 = getNthOrderMarkovHeatMap(dataNameDataMap, graph2Type, graph2RandomWalkType, graph2HumanDataType, graph2M);
                            data = subtractGraph2FromGraph1(data, data2);
                            System.out.println("Total difference="+amountOfDifference(data));
                            assert data != null;
                        }
                        System.out.println("Processing done");
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void done() {
                    System.out.println("Done started");
                    assert data != null;
                    chartDisplay.setTitle(order + "th order heat map :" + phase.toString());
                    chartDisplay.display(data);
//                            consoleDisplay.display(data);
                }


            };
            worker.execute();


        }


        private HashBasedTable<String, String, Double> subtractGraph2FromGraph1(HashBasedTable<String, String, Double> graph1, HashBasedTable<String, String, Double> graph2) {


//            HashBasedTable<String, String, Double> randomWalkData = MarkovDataOrganizer.instance().
//                    getRandomWalkMarkovData(RandomWalkOrganizer.RandomWalkType.UNBIASED).getDirectMarkovData(order).
//                    obtainHeatMap();

            HashBasedTable<String, String, Double> result = HashBasedTable.create();
            for (String sourceRoom : CompleteGraph.instance().getSortedRooms()) {

                for (String destinationRoom : CompleteGraph.instance().getSortedRooms()) {
                    if (graph2.contains(sourceRoom, destinationRoom)) {
                        if (graph1.contains(sourceRoom, destinationRoom)) {
                            result.put(sourceRoom, destinationRoom,
                                    graph1.get(sourceRoom, destinationRoom) - graph2.get(sourceRoom, destinationRoom));
                        } else {
                            result.put(sourceRoom, destinationRoom, -graph2.get(sourceRoom, destinationRoom));
                        }
                    } else {
                        if (graph1.contains(sourceRoom, destinationRoom)) {
                            result.put(sourceRoom, destinationRoom,
                                    graph1.get(sourceRoom, destinationRoom));
                        }
                    }

                }
            }

            return result;
        }
    }

    private String amountOfDifference(HashBasedTable<String, String, Double> data) {
        Map<String, Map<String, Double>> rowMap = data.rowMap();
        double sum = 0.0;
        for(String source :rowMap.keySet()){
            for(String destination: rowMap.get(source).keySet()){
                sum += Math.abs(rowMap.get(source).get(destination));
            }

        }
        return String.valueOf(sum);
    }

    private HashBasedTable<String, String, Double> getNthOrderMarkovHeatMap(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameGraphMap,
                                                                            MarkovDataDialog.GraphType graphType,
                                                                            RandomWalkOrganizer.RandomWalkType randomWalkType,
                                                                            MarkovDataDialog.HumanType humanType,
                                                                            int mValue) {
        if (graphType == MarkovDataDialog.GraphType.RANDOM_WALK) {
            return MarkovDataOrganizer.instance().getRandomWalkMarkovData(randomWalkType).getDirectMarkovData(order).obtainHeatMap();
        }
        Collection<String> nameList = dataNameGraphMap.keySet();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphList = dataNameGraphMap.values();
        assert graphList.size() == nameList.size();
        Collection<Phase> selectedPhases = Collections.singleton(phase);
        if (humanType == MarkovDataDialog.HumanType.DIRECT)
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).
                    getDirectMarkovData(order).obtainHeatMap();
        else
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).
                    getNFromMData(order, mValue).obtainHeatMap();
    }

    private class CalculateCoverageTask extends AbstractTask {
        private final int hopsRequired;
        private final Phase phase;
        private final int order;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap =
                new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public CalculateCoverageTask(Collection<String> dataNames, Phase phase, int order, int hopsRequired) {
            super(dataNames);
            this.phase = phase;

            this.order = order;
            this.hopsRequired = hopsRequired;
        }

        @Override
        protected void doTasks(String dataName) {
            if (!dataName.equals("random walk")) {

                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName,
                        Collections.singleton(phase)));

            }
        }

        @Override
        protected void summarizeAndDisplay() {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public RecursiveHashMap data;
                private HashMap<String, Double> coverage;

                @Override
                protected Void doInBackground() throws Exception {

                    data = getNthOrderMarkovData(dataNameDataMap, graph1Type, graph1RandomWalkType, graph1HumanDataType,
                            graph1M);
                    coverage = GraphUtilities.calculateCoverageForPathLength(data, hopsRequired);

                    return null;
                }

                protected void done() {
                    NumberFormat doubleFormat = new DecimalFormat("######.000");
                    JOptionPane.showMessageDialog(new JFrame(),
                            "order = " + order + "\n" +
                                    "graph type = " + graph1Type + "\n" +
                                    "random Walk Type=" + (graph1RandomWalkType == null ? "N/A" : graph1RandomWalkType) + "\n" +
                                    "human Map Type=" + (graph1HumanDataType == null ? "N/A" : graph1HumanDataType) + "\n" +
                                    "m value=" + (graph1M == 0 ? "N/A" : graph1M) + "\n" +
                                    "hops=" + hopsRequired + "\n" +
                                    "<html><font color=\"red\">COVERAGE = " + doubleFormat.format(coverage.get("mean")) + " \u00B1 " +
                                    doubleFormat.format(coverage.get("sd")) + "</font></html>", "Coverage calculated",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            };
            worker.execute();

        }
    }

    private RecursiveHashMap getNthOrderMarkovData(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>
                                                           dataNameGraphMap, MarkovDataDialog.GraphType graphType,
                                                   RandomWalkOrganizer.RandomWalkType randomWalkType,
                                                   MarkovDataDialog.HumanType humanType, int mValue) {
        if (graphType == MarkovDataDialog.GraphType.RANDOM_WALK) {
            return MarkovDataOrganizer.instance().getRandomWalkMarkovData(randomWalkType).getDirectMarkovData(order);
        }
        Collection<String> nameList = dataNameGraphMap.keySet();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphList = dataNameGraphMap.values();
        assert graphList.size() == nameList.size();
        Collection<Phase> selectedPhases = Collections.singleton(phase);
        if (humanType == MarkovDataDialog.HumanType.DIRECT)
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).getDirectMarkovData(order);
        else
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).getNFromMData(order, mValue);
    }

    private class CalculateHopsNeededTask extends AbstractTask {
        private final int coverageRequired;

        private final Phase phase;
        private final int order;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public CalculateHopsNeededTask(Collection<String> dataNames, Phase phase, int order, int coverageRequired) {
            super(dataNames);
            this.phase = phase;

            this.order = order;
            this.coverageRequired = coverageRequired;
        }

        @Override
        protected void doTasks(String dataName) {
            if (!dataName.equals("random walk")) {

                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, Collections.singleton(phase)));

            }
        }

        @Override
        protected void summarizeAndDisplay() {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public RecursiveHashMap data;
                private HashMap<String, Double> hopsNeeded;

                @Override
                protected Void doInBackground() throws Exception {

                    data = getNthOrderMarkovData(dataNameDataMap, graph1Type, graph1RandomWalkType, graph1HumanDataType, graph1M);
                    hopsNeeded = GraphUtilities.calculateHopsNeededForCoverage(data, coverageRequired);

                    return null;
                }

                protected void done() {

                    NumberFormat doubleFormat = new DecimalFormat("######.000");
                    JOptionPane.showMessageDialog(new JFrame(),
                            "order = " + order + "\n" +
                                    "graph type = " + graph1Type + "\n" +
                                    "random Walk Type=" + (graph1RandomWalkType == null ? "N/A" : graph1RandomWalkType) + "\n" +
                                    "human Map Type=" + (graph1HumanDataType == null ? "N/A" : graph1HumanDataType) + "\n" +
                                    "m value=" + (graph1M == 0 ? "N/A" : graph1M) + "\n" +
                                    "coverage required=" + coverageRequired + "\n" +
                                    "<html><font color=\"red\">HOPS NEEDED = " + doubleFormat.format(hopsNeeded.get("mean")) + " \u00B1 " +
                                    doubleFormat.format(hopsNeeded.get("sd")) + "</font></html>", "Hops needed calculated", JOptionPane.INFORMATION_MESSAGE
                    );
                }
            };
            worker.execute();
        }
    }
}

