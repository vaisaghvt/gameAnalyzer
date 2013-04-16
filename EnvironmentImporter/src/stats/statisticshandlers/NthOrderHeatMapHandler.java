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

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class NthOrderHeatMapHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay>
        implements ActionListener {

    private int order;
    private Collection<String> dataNames;
    private Phase phase;

    private HeatMapComparisonDialog heatMapComparisonDialog;
    private HeatMapComparisonDialog.HeatMapType heatMapType;
    private HeatMapComparisonDialog.GraphType graph1Type;
    private RandomWalkOrganizer.RandomWalkType graph1RandomWalkType;
    private HeatMapComparisonDialog.HumanType graph1HumanDataType;
    private int graph1M;
    private HeatMapComparisonDialog.GraphType graph2Type;
    private RandomWalkOrganizer.RandomWalkType graph2RandomWalkType;
    private int graph2M;
    private HeatMapComparisonDialog.HumanType graph2HumanDataType;

    public NthOrderHeatMapHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne,
                                        StatsDialog.AggregationType aggregationType) {

        heatMapComparisonDialog = new HeatMapComparisonDialog(this);
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

        if (e.getSource() == heatMapComparisonDialog.generateHeatMapButton) {
            order = heatMapComparisonDialog.getOrder();
            heatMapType = heatMapComparisonDialog.getHeatMapType();
            graph1Type = heatMapComparisonDialog.getGraph1Type();
            if (graph1Type == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = heatMapComparisonDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = heatMapComparisonDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == HeatMapComparisonDialog.HumanType.N_FROM_M) {
                    graph1M = heatMapComparisonDialog.getGraph1m();
                }
            }

            if (heatMapType == HeatMapComparisonDialog.HeatMapType.COMPARISON) {
                graph2Type = heatMapComparisonDialog.getGraph2Type();
                if (graph2Type == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
                    graph2RandomWalkType = heatMapComparisonDialog.getGraph2RandomWalkType();
                } else {
                    graph2HumanDataType = heatMapComparisonDialog.getGraph2HumanType();
                    if (order > 1 && graph2HumanDataType == HeatMapComparisonDialog.HumanType.N_FROM_M) {
                        graph2M = heatMapComparisonDialog.getGraph2m();
                    }
                }
            }

            chartDisplay.setType(heatMapType);

            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, order);
            super.actualGenerateAndDisplay(task);
            heatMapComparisonDialog.dispose();
        }else if (e.getSource() == heatMapComparisonDialog.calculateHopsForCoverage) {
            order = heatMapComparisonDialog.getOrder();
            heatMapType = heatMapComparisonDialog.getHeatMapType();
            graph1Type = heatMapComparisonDialog.getGraph1Type();
            if (graph1Type == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = heatMapComparisonDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = heatMapComparisonDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == HeatMapComparisonDialog.HumanType.N_FROM_M) {
                    graph1M = heatMapComparisonDialog.getGraph1m();
                }
            }
            int coverageRequired = heatMapComparisonDialog.getCoverageRequired();

            if (graph1HumanDataType == HeatMapComparisonDialog.HumanType.DIRECT) {

                CalculateHopsNeededTask task = new CalculateHopsNeededTask(dataNames, phase, order, coverageRequired);
                super.actualGenerateAndDisplay(task);
                heatMapComparisonDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == heatMapComparisonDialog.calculateCoverageForHopsButton) {
            order = heatMapComparisonDialog.getOrder();
            heatMapType = heatMapComparisonDialog.getHeatMapType();
            graph1Type = heatMapComparisonDialog.getGraph1Type();
            if (graph1Type == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
                graph1RandomWalkType = heatMapComparisonDialog.getGraph1RandomWalkType();
            } else {
                graph1HumanDataType = heatMapComparisonDialog.getGraph1HumanType();
                if (order > 1 && graph1HumanDataType == HeatMapComparisonDialog.HumanType.N_FROM_M) {
                    graph1M = heatMapComparisonDialog.getGraph1m();
                }
            }
            int hopsRequired = heatMapComparisonDialog.getHopsRequired();
            if (graph1HumanDataType == HeatMapComparisonDialog.HumanType.DIRECT) {
                CalculateCoverageTask task = new CalculateCoverageTask(dataNames, phase, order, hopsRequired);
                super.actualGenerateAndDisplay(task);
                heatMapComparisonDialog.dispose();
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

                    data = getNthOrderMarkovHeatMap(dataNameDataMap, graph1Type, graph1RandomWalkType, graph1HumanDataType, graph1M);
                    if (heatMapType == HeatMapComparisonDialog.HeatMapType.COMPARISON) {
                        HashBasedTable<String, String, Double> data2 = getNthOrderMarkovHeatMap(dataNameDataMap, graph2Type, graph2RandomWalkType, graph2HumanDataType, graph2M);
                        data = subtractGraph2FromGraph1(data, data2);
                    }
                    return null;
                }

                protected void done() {
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

    private HashBasedTable<String, String, Double> getNthOrderMarkovHeatMap(HashMap<String,DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameGraphMap,
                                                                            HeatMapComparisonDialog.GraphType graphType,
                                                                            RandomWalkOrganizer.RandomWalkType randomWalkType,
                                                                            HeatMapComparisonDialog.HumanType humanType,
                                                                            int mValue) {
        if (graphType == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
            return MarkovDataOrganizer.instance().getRandomWalkMarkovData(randomWalkType).getDirectMarkovData(order).obtainHeatMap();
        }
        Collection<String> nameList = dataNameGraphMap.keySet();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphList = dataNameGraphMap.values();
        assert graphList.size() == nameList.size();
        Collection<Phase> selectedPhases = Collections.singleton(phase);
        if (humanType == HeatMapComparisonDialog.HumanType.DIRECT)
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).
                    getDirectMarkovData(order).obtainHeatMap();
        else
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList, selectedPhases, graphList).
                    getNFromMData(order, mValue).obtainHeatMap();
    }

    private class CalculateCoverageTask extends AbstractTask {
        private final int hopsRequired;
        private int coverage;
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
                                                           dataNameGraphMap, HeatMapComparisonDialog.GraphType graphType,
                                                   RandomWalkOrganizer.RandomWalkType randomWalkType,
                                                   HeatMapComparisonDialog.HumanType humanType, int mValue) {
        if (graphType == HeatMapComparisonDialog.GraphType.RANDOM_WALK) {
            return MarkovDataOrganizer.instance().getRandomWalkMarkovData(randomWalkType).getDirectMarkovData(order);
        }
        Collection<String> nameList = dataNameGraphMap.keySet();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphList = dataNameGraphMap.values();
        assert graphList.size() == nameList.size();
        Collection<Phase> selectedPhases = Collections.singleton(phase);
        if (humanType == HeatMapComparisonDialog.HumanType.DIRECT)
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
                                    "<html><font color=\"red\">HOPS NEEDED = " +  doubleFormat.format(hopsNeeded.get("mean")) + " \u00B1 " +
                                    doubleFormat.format(hopsNeeded.get("sd")) + "</font></html>", "Hops needed calculated", JOptionPane.INFORMATION_MESSAGE
                    );
                }
            };
            worker.execute();
        }
    }
}

