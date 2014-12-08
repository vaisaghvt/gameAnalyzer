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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import randomwalk.RandomWalkOrganizer;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.chartdisplays.RoomAnalysisFrame;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarkovDataStatisticHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay>
        implements ActionListener {

    private boolean FIND_ALL_TILL_ORDER = false;
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
            final int coverageRequired = markovDataDialog.getCoverageRequired();

            SwingWorker<Void, Void> calculator = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK || graph1HumanDataType == MarkovDataDialog.HumanType.DIRECT) {
                        final Semaphore lock = new Semaphore(1);
                        if (FIND_ALL_TILL_ORDER) {


                            int usedOrder;
                            for (int i = order; i >= Math.max(0, order-5); i--) {
                                if (i == 0) {

                                    graph1Type = MarkovDataDialog.GraphType.RANDOM_WALK;

                                    usedOrder = 1;
                                } else {
                                    usedOrder = i;
                                    graph1Type = MarkovDataDialog.GraphType.HUMAN_DATA;
                                }

                                final int actualUsedOrder = usedOrder;


                                CalculateHopsNeededTask task = new CalculateHopsNeededTask(dataNames, phase, actualUsedOrder, coverageRequired, lock);

                                try {
                                    lock.tryAcquire(1, 300, TimeUnit.SECONDS);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                MarkovDataStatisticHandler.super.actualGenerateAndDisplay(task);
                            }


                        } else{

                            CalculateHopsNeededTask task = new CalculateHopsNeededTask(dataNames, phase, order, coverageRequired, lock);

                            try {
                                lock.tryAcquire(1, 300, TimeUnit.SECONDS);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            MarkovDataStatisticHandler.super.actualGenerateAndDisplay(task);

                        }


//                markovDataDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }

            };
            calculator.execute();
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
            final int hopsRequired = markovDataDialog.getHopsRequired();
            SwingWorker<Void, Void> calculator = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK || graph1HumanDataType == MarkovDataDialog.HumanType.DIRECT) {
                        final Semaphore lock = new Semaphore(1);
                        if (FIND_ALL_TILL_ORDER) {

                            int usedOrder;
                            for (int i = order; i >= Math.max(0, order-5); i--) {
                                if (i == 0) {
                                    graph1Type = MarkovDataDialog.GraphType.RANDOM_WALK;

                                    usedOrder = 1;
                                } else {
                                    usedOrder = i;
                                    graph1Type = MarkovDataDialog.GraphType.HUMAN_DATA;
                                }


                                CalculateCoverageTask task = new CalculateCoverageTask(dataNames, phase, usedOrder, hopsRequired, lock);

                                try {
                                    lock.tryAcquire(1, 300, TimeUnit.SECONDS);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                                MarkovDataStatisticHandler.super.actualGenerateAndDisplay(task);
//                markovDataDialog.dispose();

                            }

                        } else {
                            CalculateCoverageTask task = new CalculateCoverageTask(dataNames, phase, order, hopsRequired, lock);

                            try {
                                lock.tryAcquire(1, 300, TimeUnit.SECONDS);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            MarkovDataStatisticHandler.super.actualGenerateAndDisplay(task);
                        }
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            };
            calculator.execute();

        } else if (e.getSource() == markovDataDialog.calculateCorridorGraph) {
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
            final int hopsRequired = markovDataDialog.getHopsRequired();
            SwingWorker<Void, Void> calculator = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (graph1Type == MarkovDataDialog.GraphType.RANDOM_WALK || graph1HumanDataType == MarkovDataDialog.HumanType.DIRECT) {
                        final Semaphore lock = new Semaphore(1);
                        if (FIND_ALL_TILL_ORDER) {
                            throw new UnsupportedOperationException();
                        } else {
                            CalculateCorridorPrefTask task = new CalculateCorridorPrefTask(dataNames, phase, order, hopsRequired, lock);

                            try {
                                lock.tryAcquire(1, 300, TimeUnit.SECONDS);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            MarkovDataStatisticHandler.super.actualGenerateAndDisplay(task);
                        }
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "Cannot generate this data for NFromM!", "Invalid parameter",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            };
            calculator.execute();

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
//                            System.out.println();
//                            System.out.println("Total difference=" + (amountOfDifference(data)/(data.cellSet().size()*2)));
                            System.out.println("Total difference=" + amountOfDifference(data));
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

    private double amountOfDifference(HashBasedTable<String, String, Double> data) {
        Map<String, Map<String, Double>> rowMap = data.rowMap();
        double sum = 0.0;
        for (String source : rowMap.keySet()) {
            for (String destination : rowMap.get(source).keySet()) {
                sum += Math.abs(rowMap.get(source).get(destination));
            }

        }
        return sum;
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
        private final Semaphore lock;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap =
                new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public CalculateCoverageTask(Collection<String> dataNames, Phase phase, int order, int hopsRequired, Semaphore lock) {
            super(dataNames);
            this.phase = phase;

            this.order = order;
            this.hopsRequired = hopsRequired;
            this.lock = lock;

        }

        @Override
        protected void doTasks(String dataName) {

//            if (!dataName.equals("random walk")) {
            if (graph1Type != MarkovDataDialog.GraphType.RANDOM_WALK) {

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
//                    System.out.println("Taken number of paths="+data.getNumberOfTakenPaths() );
//                    System.out.println("Possible number of paths="+ CompleteGraph.instance().countPathsOfLength(order));
                    File directory=new File("coverage");
                    if (!directory.exists()) {
                        if (!directory.mkdir()) {
                            System.out.println("Type Directory could not be created for " + directory);
                        }
                    }
                    if (directory.exists()) {
                        String fileName = "coverage"+ File.separatorChar + graph1Type + "-"+hopsRequired+"-"+ order;
                        if(graph1Type== MarkovDataDialog.GraphType.RANDOM_WALK){
                            fileName += "-"+graph1RandomWalkType;
                        }
                        coverage = GraphUtilities.calculateCoverageForPathLength(data, hopsRequired, fileName);
                    }
                    return null;

                }

                protected void done() {
                    NumberFormat doubleFormat = new DecimalFormat("######.000");
//                    JOptionPane.showMessageDialog(new JFrame(),
//                            "order = " + order + "\n" +
//                                    "graph type = " + graph1Type + "\n" +
//                                    "random Walk Type=" + (graph1RandomWalkType == null ? "N/A" : graph1RandomWalkType) + "\n" +
//                                    "human Map Type=" + (graph1HumanDataType == null ? "N/A" : graph1HumanDataType) + "\n" +
//                                    "m value=" + (graph1M == 0 ? "N/A" : graph1M) + "\n" +
//                                    "hops=" + hopsRequired + "\n" +
//                                    "<html><font color=\"red\">COVERAGE = " + doubleFormat.format(coverage.get("mean")) + " \u00B1 " +
//                                    doubleFormat.format(coverage.get("sd")) + "</font></html>", "Coverage calculated",
//                            JOptionPane.INFORMATION_MESSAGE
//                    );

                    System.out.println("Order="+order+";Hops="+hopsRequired+";COVERAGE = " + doubleFormat.format(coverage.get("mean")) + " \u00B1 " +
                            doubleFormat.format(coverage.get("sd")));

                    CalculateCoverageTask.this.lock.release();
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
        private final Semaphore lock;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public CalculateHopsNeededTask(Collection<String> dataNames, Phase phase, int order, int coverageRequired, Semaphore lock) {
            super(dataNames);
            this.phase = phase;

            this.order = order;
            this.coverageRequired = coverageRequired;
            this.lock = lock;


        }

        @Override
        protected void doTasks(String dataName) {
//            if (!dataName.equals("random walk")) {
            if (graph1Type != MarkovDataDialog.GraphType.RANDOM_WALK) {
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
                    File directory=new File("hops");
                    if (!directory.exists()) {
                        if (!directory.mkdir()) {
                            System.out.println("Type Directory could not be created for " + directory);
                        }
                    }
                    if (directory.exists()) {
                        String fileName = "hops"+ File.separatorChar +graph1Type+"-"+coverageRequired+"-"+ order;
                        if(graph1Type== MarkovDataDialog.GraphType.RANDOM_WALK){
                            fileName += "-"+graph1RandomWalkType;
                        }
                        hopsNeeded = GraphUtilities.calculateHopsNeededForCoverage(data, coverageRequired, fileName);
                    }

                    return null;
                }

                protected void done() {

                    NumberFormat doubleFormat = new DecimalFormat("######.000");
//                    JOptionPane.showMessageDialog(new JFrame(),
//                            "order = " + order + "\n" +
//                                    "graph type = " + graph1Type + "\n" +
//                                    "random Walk Type=" + (graph1RandomWalkType == null ? "N/A" : graph1RandomWalkType) + "\n" +
//                                    "human Map Type=" + (graph1HumanDataType == null ? "N/A" : graph1HumanDataType) + "\n" +
//                                    "m value=" + (graph1M == 0 ? "N/A" : graph1M) + "\n" +
//                                    "coverage required=" + coverageRequired + "\n" +
//                                    "<html><font color=\"red\">HOPS NEEDED = " + doubleFormat.format(hopsNeeded.get("mean")) + " \u00B1 " +
//                                    doubleFormat.format(hopsNeeded.get("sd")) + "</font></html>", "Hops needed calculated", JOptionPane.INFORMATION_MESSAGE
//                    );
                    System.out.println("Order="+order+";Coverage="+coverageRequired+";Hops = " + doubleFormat.format(hopsNeeded.get("mean")) + " \u00B1 " +
                            doubleFormat.format(hopsNeeded.get("sd")));

                    CalculateHopsNeededTask.this.lock.release();
                }
            };
            worker.execute();
        }
    }


    private class CalculateCorridorPrefTask extends AbstractTask {
        private final int hopsRequired;
        private final Phase phase;
        private final int order;
        private final Semaphore lock;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap =
                new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();


        public CalculateCorridorPrefTask(Collection<String> dataNames, Phase phase, int order, int hopsRequired, Semaphore lock) {
            super(dataNames);
            this.phase = phase;

            this.order = order;
            this.hopsRequired = hopsRequired;
            this.lock = lock;

        }

        @Override
        protected void doTasks(String dataName) {

//            if (!dataName.equals("random walk")) {
            if (graph1Type != MarkovDataDialog.GraphType.RANDOM_WALK) {

                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName,
                        Collections.singleton(phase)));

            }
        }

        @Override
        protected void summarizeAndDisplay() {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public RecursiveHashMap data;
                private HashMap<String, Double> coverage;
                public HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataMap;

                @Override
                protected Void doInBackground() throws Exception {

                    data = getNthOrderMarkovData(dataNameDataMap, graph1Type, graph1RandomWalkType, graph1HumanDataType,
                            graph1M);
//                    System.out.println("Taken number of paths="+data.getNumberOfTakenPaths() );
//                    System.out.println("Possible number of paths="+ CompleteGraph.instance().countPathsOfLength(order));
//                    File directory=new File("coverage");
//                    if (!directory.exists()) {
//                        if (!directory.mkdir()) {
//                            System.out.println("Type Directory could not be created for " + directory);
//                        }
//                    }
//                    if (directory.exists()) {
//                        String fileName = "coverage"+ File.separatorChar + graph1Type + "-"+hopsRequired+"-"+ order;
//                        if(graph1Type== MarkovDataDialog.GraphType.RANDOM_WALK){
//                            fileName += "-"+graph1RandomWalkType;
//                        }
//                        coverage = GraphUtilities.calculateCoverageForPathLength(data, hopsRequired, fileName);
//                    }
                    dataMap = GraphUtilities.getGeneratedPathsForHops(data, hopsRequired);

                    return null;

                }

                protected void done() {
                    MarkovDataStatisticHandler.generateGroupedStackChartForSimpleCorridor(dataMap);
//                    NumberFormat doubleFormat = new DecimalFormat("######.000");
//                    JOptionPane.showMessageDialog(new JFrame(),
//                            "order = " + order + "\n" +
//                                    "graph type = " + graph1Type + "\n" +
//                                    "random Walk Type=" + (graph1RandomWalkType == null ? "N/A" : graph1RandomWalkType) + "\n" +
//                                    "human Map Type=" + (graph1HumanDataType == null ? "N/A" : graph1HumanDataType) + "\n" +
//                                    "m value=" + (graph1M == 0 ? "N/A" : graph1M) + "\n" +
//                                    "hops=" + hopsRequired + "\n" +
//                                    "<html><font color=\"red\">COVERAGE = " + doubleFormat.format(coverage.get("mean")) + " \u00B1 " +
//                                    doubleFormat.format(coverage.get("sd")) + "</font></html>", "Coverage calculated",
//                            JOptionPane.INFORMATION_MESSAGE
//                    );

//                    System.out.println("Order="+order+";Hops="+hopsRequired+";COVERAGE = " + doubleFormat.format(coverage.get("mean")) + " \u00B1 " +
//                            doubleFormat.format(coverage.get("sd")));
//
//                    CalculateCorridorPrefTask.this.lock.release();
                }
            };
            worker.execute();

        }
    }


    public static void generateGroupedStackChartForSimpleCorridor(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {

        List<List<String>> pathList = getPathList(nameToGraphMap);

        String[][] straightCorridorList = new String[][]{
                {"1Corner34", "1LeftCorridor3", "1LeftCorridor2"},
                {"1LeftCorridor2", "1LeftCorridor3", "1Corner34"},
                {"1Corner45", "1LeftCorridor5", "1Corner56L"},
                {"1Corner56L", "1LeftCorridor5", "1Corner45"},
                {"1Corner12R", "1RightCorridor2", "1RightCorridor3"},
                {"1RightCorridor3", "1RightCorridor2", "1Corner12R"},
                {"2CorrA4", "2CorrA3", "2CorrA2"},
                {"2CorrA2", "2CorrA3", "2CorrA4"},
                {"2CornerB12", "2CorrB1", "BToDown"},
                {"BToDown", "2CorrB1", "2CornerB12"},
                {"2CornerB12", "2CorrB2", "2Corner23"},
                {"2Corner23", "2CorrB2", "2CornerB12"},
                {"2CorrB3", "2CorrB4", "2CorrB5"},
                {"2CorrB5", "2CorrB4", "2CorrB3"},
                {"2CorrC3", "2CorrC2", "2CorrC1"},
                {"2CorrC1", "2CorrC2", "2CorrC3"},
                {"3Corr2", "3Corr4", "3Corr5"},
                {"3Corr5", "3Corr4", "3Corr2"}
        };

        String[][] staircaseCorridorList = new String[][]{
                {"LeftGStairCorr", "left G main", "left 2 stair"},
                {"left 2 stair", "left G main", "LeftGStairCorr"},
                {"RightGStairCorr", "right G main", "right 2 stair"},
                {"right 2 stair", "right G main", "RightGStairCorr"},
                {"ACToDown", "right 2 stair", "right G main"},
                {"right G main", "right 2 stair", "ACToDown"}
        };

        String[][] simpleCornerList = new String[][]{
                {"2CorrC1", "2CorrC2", "2CorrC3"},
                {"2CorrC3", "2CorrC2", "2CorrC1"},
                {"2CorrMain", "2CorrA1", "2CorrA2"},
                {"2CorrA2", "2CorrA1", "2CorrMain"},
                {"1LeftCorridor3","1Corner34",  "1LeftCorridor4"},
                {"1LeftCorridor4", "1Corner34" , "1LeftCorridor3"},
                {"1LeftCorridor4","1Corner45",  "1LeftCorridor5"},
                {"1LeftCorridor5", "1Corner45" , "1LeftCorridor4"},
                {"1LeftCorridor5","1Corner56L",  "1LeftCorridor6"},
                {"1LeftCorridor6", "1Corner56L" , "1LeftCorridor5"},
                {"1LeftCorridor7","1Corner67L",  "1LeftCorridor6"},
                {"1LeftCorridor6", "1Corner67L" , "1LeftCorridor7"},
                {"2CorrB2", "2Corner23", "2CorrB3"},
                {"2CorrB3", "2Corner23", "2CorrB2"},
                {"2CorrB2", "2CornerB12", "2CorrB1"},
                {"2CorrB1", "2CornerB12", "2CorrB2"},
                {"2CorrA5", "2CornerA56", "2CorrA6"},
                {"2CorrA6", "2CornerA56", "2CorrA5"}
        }    ;

        String[][] roomToRoomLineOfSightList = new String[][]{
                {"2CorrC3", "DB2", "MB1"},
                {"MB1", "DB2", "2CorrC3"},
                {"DB2", "MB1", "2PassB"},
                {"2PassB", "MB1", "DB2"},
                {"2PassB", "MB3", "TheLounge"},
                {"Study3", "MR", "3Corr3"},
                {"MR", "Study3", "3Corr8"},
                {"Gallery", "Study1", "3Corr1"},
                {"3Corr1", "Study1", "Gallery"},
                {"Study1", "Gallery", "3Corr7"}
//                {"SPCorr","Conf 2","1LeftCorridor4"},

        };

        String[][] roomToRoomNoLineOfSightList = new String[][]{
                {"TheLounge", "MB3", "2PassB"},
                {"3Corr3", "MR", "Study3"},
                {"3Corr8", "Study3", "MR"},  //Debatable need to think about it
                {"3Corr7", "Gallery", "Study1"},
//                {"SPCorr","Conf 2","1LeftCorridor6"},
        };

        LinkedHashMap<String, LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer>> resultHashMap = new LinkedHashMap<String, LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer>>();

        resultHashMap.put("Straight Corridor", calculateMap(pathList, straightCorridorList));
        resultHashMap.put("Staircase Corridor", calculateMap(pathList, staircaseCorridorList));
        resultHashMap.put("Simple Corner", calculateMap(pathList, simpleCornerList));
        resultHashMap.put("Room To Room Visible", calculateMap(pathList, roomToRoomLineOfSightList));
        resultHashMap.put("Room To Room invisible", calculateMap(pathList, roomToRoomNoLineOfSightList));

//           System.out.println("Yayee!");


        final CategoryDataset dataSet = createAggregatedDataSetForSimpleCorridor(resultHashMap);
        final JFreeChart chart = createAggregatedChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1280, 880));
        JFrame chartFrame = new JFrame("Summarized charts");

        chartFrame.setContentPane(chartPanel);
        chartFrame.setVisible(true);
        chartFrame.setSize(new Dimension(1300, 900));
        chartFrame.setLocation(0, 0);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


//        }

    }


    private static JFreeChart createAggregatedChart(CategoryDataset dataSet) {
        final JFreeChart stackedChart = ChartFactory.createStackedBarChart("Summarized", "Type", "Probability",
                dataSet, PlotOrientation.VERTICAL, true, true, false);

        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
        ((GroupedStackedBarRenderer)renderer).setBarPainter(new StandardBarPainter());

        //margin between bar.
//        renderer.setItemMargin(0.25);
        renderer.setMaximumBarWidth(.09);
        //end



        CategoryPlot plot = (CategoryPlot) stackedChart.getPlot();
//        plot.setDomainAxis(domainAxis);
        plot.setRenderer(renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setBackgroundPaint(Color.white);

        plot.getRangeAxis().setRange(0,1.0);
        return stackedChart;
    }


    private static CategoryDataset createAggregatedDataSetForSimpleCorridor(
            LinkedHashMap<String,
                    LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer>> resultHashMap) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String category : resultHashMap.keySet()) {
            System.out.println(category);

            double total = resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH) + resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.RETURN);

            dataset.addValue((double) resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH) / total, RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH.toString(), category);
            dataset.addValue((double) resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.RETURN) / total, RoomAnalysisFrame.CorridorMovementType.RETURN.toString(), category);

            System.out.println((double) resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH) / total);
            System.out.println((double) resultHashMap.get(category).get(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH) / total);
        }
        return dataset;

    }
    public static List<List<String>> getPathList(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        List<List<String>> listOfPaths = new ArrayList<List<String>>();
        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);
            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });
            edgeCollection.addAll(tempGraph.getEdges());

            List<String> path = new ArrayList<String>();
            boolean starting = true;
            for (ModelEdge edge : edgeCollection) {
                if (starting) {
                    path.add(tempGraph.getSource(edge).toString());
                    starting = false;
                }
                path.add(tempGraph.getDest(edge).toString());
            }
            assert path != null && path.size() > 0;
            listOfPaths.add(path);
        }
        assert listOfPaths.size() == nameToGraphMap.size();
        return listOfPaths;
    }

    private static LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer> calculateMap
            (List<List<String>> listOfPaths, String[][] straightCorridorList) {
        LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer> occurrenceMap = new LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer>();
        occurrenceMap.put(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH, 0);
        occurrenceMap.put(RoomAnalysisFrame.CorridorMovementType.RETURN, 0);
        for (String[] corridor : straightCorridorList) {
            updatePassThroughOccurrenceMap(listOfPaths, corridor, occurrenceMap);

        }

        return occurrenceMap;

    }

    private static void updatePassThroughOccurrenceMap(List<List<String>> pathList, String[] corridor, LinkedHashMap<RoomAnalysisFrame.CorridorMovementType, Integer> occurrenceMap) {
        assert occurrenceMap != null;
        assert pathList != null;
        assert corridor != null && corridor.length == 3;
        String corridorStartingRoom = corridor[0];
        String corridorCenterRoom = corridor[1];
        String corridorEndingRoom = corridor[2];

        for (List<String> path : pathList) {
            for (int roomNumber = 1; roomNumber < path.size() - 1; roomNumber++) {
                if (!path.get(roomNumber).equalsIgnoreCase(corridorCenterRoom)) {
                    continue;
                }
                String pathPreviousRoom = path.get(roomNumber - 1);


                if (!corridorStartingRoom.equalsIgnoreCase(pathPreviousRoom)) {
                    continue;
                }

                String pathNextRoom = path.get(roomNumber + 1);

                if (pathNextRoom.equalsIgnoreCase(corridorEndingRoom)) {
                    occurrenceMap.put(
                            RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH,
                            occurrenceMap.get(RoomAnalysisFrame.CorridorMovementType.PASS_THROUGH) + 1);
                } else if (pathNextRoom.equalsIgnoreCase(corridorStartingRoom)) {
                    occurrenceMap.put(
                            RoomAnalysisFrame.CorridorMovementType.RETURN,
                            occurrenceMap.get(RoomAnalysisFrame.CorridorMovementType.RETURN) + 1);

                } else {
                    System.out.println("Incorrect call to function");
                    assert false;
                }
            }
        }

    }


}

