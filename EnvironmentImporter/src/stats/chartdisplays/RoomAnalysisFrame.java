package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import gui.NetworkModel;
import gui.Phase;
import gui.ProgressVisualizer;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.Transformer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: vaisagh
 * Date: 14/3/13
 * Time: 11:10 AM
 */
public class RoomAnalysisFrame extends JFrame {
    private static final int MAX_NUMBER_TRACKING = 20;
    private JPanel dataPanel;
    private JPanel chartDisplayPanel = new JPanel();
    private Collection<JFrame> poppedOutFrames = new HashSet<JFrame>();

    private JCheckBox explorationCheckBox = new JCheckBox("Exploration");
    private JCheckBox task1CheckBox = new JCheckBox("Task 1");
    private JCheckBox task2CheckBox = new JCheckBox("Task 2");
    private JCheckBox task3CheckBox = new JCheckBox("Task 3");
    private JSlider startTimeSelection;
    private JLabel startTimeValue = new JLabel("0");
    private JSlider endTimeSelection;
    private JLabel endTimeValue = new JLabel("45");

    private JSlider maxCoverageSlider;
    private JLabel minCoverageLabel = new JLabel("0");
    private JSlider minCoverageSlider;
    private JLabel maxCoverageLabel = new JLabel("100");


    private JButton closeButton = new JButton("Close");
    private JButton generateButton = new JButton("Generate");
    private JComboBox<DisplayType> displayTypeJComboBox;
    private JComboBox<String> roomButtonComboBox;
    private Collection<String> roomList = new ArrayList<String>();
    private JButton popOutButton = new JButton("Pop-out");
    private Collection<String> dataNameList;


    private JButton saveAsImageButton = new JButton("Save as...");

    private RoomDataListener roomDataListener;
    private VisualizationViewer<ModelObject, ? extends ModelEdge> currentVisualizationViewer;
    private String currentTitle;
    private JComboBox<String> sourceComboBox;

    public RoomAnalysisFrame(Collection<String> dataNames) {
        roomDataListener = new RoomDataListener();
        this.dataNameList = dataNames;
        initializeRoomList();


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                dataPanel = createRoomAnalysisPanel();

                setTitle(getTitle());
                setContentPane(dataPanel);
                setVisible(true);
                setSize(new Dimension(1200, 500));
                setLocation(100, 100);
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
        });
    }


    private JPanel createRoomAnalysisPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        JPanel buttonPanel = createButtonPanel();
        JPanel leftPanel = createLeftPanel();
        mainPanel.add(leftPanel);
        mainPanel.add(buttonPanel);
        return mainPanel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
//        updateChartPanel();
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

        bottomPanel.add(popOutButton);
        bottomPanel.add(saveAsImageButton);

        saveAsImageButton.addActionListener(roomDataListener);
        popOutButton.addActionListener(roomDataListener);
        panel.add(chartDisplayPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createButtonPanel() {

        LimitedBoundedRangeModel startModel = new LimitedBoundedRangeModel(null, LimitedBoundedRangeModel.MAX_OR_MIN.MAX, 0, 45);
        LimitedBoundedRangeModel endModel = new LimitedBoundedRangeModel(startModel, LimitedBoundedRangeModel.MAX_OR_MIN.MIN, 0, 45);
        startModel.limit = endModel;
        startTimeSelection = new JSlider(startModel);
        startTimeSelection.setValue(0);
        endTimeSelection = new JSlider(endModel);
        endTimeSelection.setValue(45);

        LimitedBoundedRangeModel minCoverage = new LimitedBoundedRangeModel(null, LimitedBoundedRangeModel.MAX_OR_MIN.MAX, 0, 100);
        LimitedBoundedRangeModel maxCoverage = new LimitedBoundedRangeModel(minCoverage, LimitedBoundedRangeModel.MAX_OR_MIN.MIN, 0, 100);
        minCoverage.limit = maxCoverage;
        minCoverageSlider = new JSlider(minCoverage);
        minCoverageSlider.setValue(0);
        maxCoverageSlider = new JSlider(maxCoverage);
        maxCoverageSlider.setValue(100);


        generateButton.addActionListener(roomDataListener);
        closeButton.addActionListener(roomDataListener);
        displayTypeJComboBox = new JComboBox<DisplayType>();
        for (DisplayType type : DisplayType.values()) {
            displayTypeJComboBox.addItem(type);
        }
        displayTypeJComboBox.setEditable(false);


        JPanel buttonPanel = new JPanel(new GridLayout(12, 2));

        buttonPanel.add(new JLabel("Choose room :"));
        buttonPanel.add(generateRoomButtonComboBox());
        roomButtonComboBox.addActionListener(roomDataListener);

        buttonPanel.add(new JLabel("Select phases"));
        buttonPanel.add(explorationCheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task1CheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task2CheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task3CheckBox);

        buttonPanel.add(new JLabel("Display type:"));
        buttonPanel.add(displayTypeJComboBox);

        buttonPanel.add(new JLabel("Choose Start time:"));
        JPanel startPanel = new JPanel(new BorderLayout());
        startPanel.add(startTimeSelection, BorderLayout.CENTER);
        startPanel.add(startTimeValue, BorderLayout.WEST);
        buttonPanel.add(startPanel);

        buttonPanel.add(new JLabel("Choose End time"));
        JPanel endPanel = new JPanel(new BorderLayout());
        endPanel.add(endTimeSelection, BorderLayout.CENTER);
        endPanel.add(endTimeValue, BorderLayout.WEST);
        buttonPanel.add(endPanel);

        startTimeSelection.addChangeListener(roomDataListener);
        endTimeSelection.addChangeListener(roomDataListener);

        buttonPanel.add(new JLabel("Choose Min coverage:"));
        JPanel minCoveragePanel = new JPanel(new BorderLayout());
        minCoveragePanel.add(minCoverageSlider, BorderLayout.CENTER);
        minCoveragePanel.add(minCoverageLabel, BorderLayout.WEST);
        buttonPanel.add(minCoveragePanel);

        buttonPanel.add(new JLabel("Choose Max coverage"));
        JPanel maxCoveragePanel = new JPanel(new BorderLayout());
        maxCoveragePanel.add(maxCoverageSlider, BorderLayout.CENTER);
        maxCoveragePanel.add(maxCoverageLabel, BorderLayout.WEST);
        buttonPanel.add(maxCoveragePanel);

        buttonPanel.add(new JLabel("Choose source:"));
        sourceComboBox = new JComboBox<String>();
        buttonPanel.add(sourceComboBox);

        maxCoverageSlider.addChangeListener(roomDataListener);
        minCoverageSlider.addChangeListener(roomDataListener);


        buttonPanel.add(generateButton);
        buttonPanel.add(closeButton);


        JPanel finalPanel = new JPanel(new BorderLayout());
        finalPanel.add(buttonPanel, BorderLayout.NORTH);
        return finalPanel;

    }


    private void updateChartPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chartDisplayPanel.removeAll();
            }
        });


        final HashSet<Phase> selectedPhases = new HashSet<Phase>();
        if (explorationCheckBox.isSelected()) {
            selectedPhases.add(Phase.EXPLORATION);
        }
        if (task1CheckBox.isSelected()) {
            selectedPhases.add(Phase.TASK_1);
        }
        if (task2CheckBox.isSelected()) {
            selectedPhases.add(Phase.TASK_2);
        }
        if (task3CheckBox.isSelected()) {
            selectedPhases.add(Phase.TASK_3);
        }


        if (!selectedPhases.isEmpty()) {
            final DisplayType type = displayTypeJComboBox.getItemAt(displayTypeJComboBox.getSelectedIndex());
            final String room = roomButtonComboBox.getItemAt(roomButtonComboBox.getSelectedIndex());
            final int startTimeSeconds = startTimeSelection.getValue();
            final int endTimeSeconds = endTimeSelection.getValue();
            final int minCoverageValue = minCoverageSlider.getValue();
            final int maxCoverageValue = maxCoverageSlider.getValue();
            final String selectedSource = sourceComboBox.getItemAt(sourceComboBox.getSelectedIndex());

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            });
            SwingWorker<Void, Void> tempWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    System.out.println("Generating name to graph mapping");
                    createCurrentTitle(room, type, selectedPhases);

                    HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap = generateRelevantGraphToNameMap(dataNameList, selectedPhases);
                    HashMap<String, Long> nameToMinCoverageTimeMap = calculateTimeOfCoverage(minCoverageValue, nameToGraphMap);
                    HashMap<String, Long> nameToMaxCoverageTimeMap = calculateTimeOfCoverage(maxCoverageValue, nameToGraphMap);
                    switch (type) {

                        case SECOND_ORDER_MARKOV:
                            generateSecondOrderProbabilities(nameToGraphMap, room, startTimeSeconds, endTimeSeconds, nameToMinCoverageTimeMap, nameToMaxCoverageTimeMap);
                            break;
                        case FIRST_ORDER_MARKOV:

//                            System.out.println("Calling simple complete directed graph");
                            generateFirstOrderProbabilities(nameToGraphMap, room, startTimeSeconds, endTimeSeconds, nameToMinCoverageTimeMap, nameToMaxCoverageTimeMap);
                            break;
                        case TEMPORAL_SECOND_ORDER_MARKOV:
                            generateTemporalSecondOrderMarkov(nameToGraphMap, room, selectedSource);
                            break;
                        case GROUPED_STACK_CHART_FOR_SIMPLE_CORRIDOR:
                            generateGroupedStackChartForSimpleCorridor(nameToGraphMap);
                            break;
                        case GROUPED_STACK_CHART_FOR_DORM_CORR:
                            generateGroupedStackChartForDormCorridor(nameToGraphMap);
                            break;
                        case GROUPED_STACK_CHART_FOR_CORR_A2:
                            generateGroupedStackChartForCorrA2(nameToGraphMap);
                            break;
                        case ORDER_FOR_ROOM_VISITATION:
                            orderForRoomVisitation(nameToGraphMap);
                            break;
                        case CUMULATIVE_FREQUENCY_VISITS:
                            cumulativeFrequencyOfVisits(nameToGraphMap, room);
                            break;
                    }
                    return null;
                }

                protected void done() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(room + ":" + type);
                            chartDisplayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            validate();
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    });
                }

            };
            tempWorker.execute();

        } else {
            JOptionPane.showMessageDialog(this, "No phases selected", "Insufficient data Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cumulativeFrequencyOfVisits(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String roomName) {

        HashMultimap<String, Long> nameToVisitTimeMapping = HashMultimap.create();
        for(String name: nameToGraphMap.keySet()){
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);
//            System.out.println(name);

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });
            edgeCollection.addAll(tempGraph.getEdges());


            boolean starting = true;
            for (ModelEdge edge : edgeCollection) {
                ModelObject sourceRoom = CompleteGraph.instance().findRoomByName(tempGraph.getSource(edge).toString());
                boolean connectedSource = CompleteGraph.instance().getDegreeOfRoom(sourceRoom)>1;
                if (starting) {


                    if(tempGraph.getSource(edge).toString().equalsIgnoreCase(roomName)){
                        nameToVisitTimeMapping.put(name, 0l);
//                        System.out.println(0);
                    }
                    starting = false;
                }

                if(tempGraph.getDest(edge).toString().equalsIgnoreCase(roomName)){
//                    System.out.println(edge.getTime());
                    nameToVisitTimeMapping.put(name, edge.getTime());
                }
//                path.add(tempGraph.getDest(edge).toString());
            }

//            assert path != null && path.size() > 0;


        }

        File file = new File(roomName+".csv");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        for (String name: nameToVisitTimeMapping.keySet()){
            writer.print(name);
            System.out.println(name);
            TreeSet<Long> values = new TreeSet<Long> (nameToVisitTimeMapping.get(name));
            for(Long value: values){
                writer.print(","+value);
            }
            writer.println();
        }
        writer.close();
//        System.out.println(nameToVisitTimeMapping);


    }

    private void generateGroupedStackChartForDormCorridor(HashMap<String,DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        List<List<String>> pathList = getPathList(nameToGraphMap);

        LinkedHashMap<String, LinkedHashMap<DormCorridorBehavior, Integer>> resultHashMap =
                calculateDecisionForEachAttemptDormCorr(pathList);



        final CategoryDataset dataSet = createAggregatedDataSetForDormCorr(resultHashMap);
        final JFreeChart chart = createAggregatedChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1280, 880));
        JFrame chartFrame = new JFrame("Summarized charts");

        chartFrame.setContentPane(chartPanel);
        chartFrame.setVisible(true);
        chartFrame.setSize(new Dimension(1300, 900));
        chartFrame.setLocation(0, 0);
        chartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private LinkedHashMap<String, LinkedHashMap<DormCorridorBehavior, Integer>> calculateDecisionForEachAttemptDormCorr(List<List<String>> pathList) {
        LinkedHashMap<String, LinkedHashMap<DormCorridorBehavior, Integer>> result = new LinkedHashMap<String, LinkedHashMap<DormCorridorBehavior, Integer>>();
        String dormCorrString = "2DormCorr";
        String dorm1 = "Dorm 1";
        String dorm2 = "Dorm 2";
        String corrA2 = "2CorrA2";





        for (List<String> path : pathList) {
            int visitNumber = 0;
            for (int roomNumber = 1; roomNumber < path.size() - 1; roomNumber++) {


                if (!path.get(roomNumber).equalsIgnoreCase(corrA2)) {
                    continue;
                }
                if(!path.get(roomNumber+1).equalsIgnoreCase(dormCorrString)){
                    continue;
                }

                visitNumber++;
                String currentAttemptKey = "Visit " + visitNumber;
                if(!result.containsKey(currentAttemptKey)){
                    result.put(currentAttemptKey,new LinkedHashMap<DormCorridorBehavior, Integer>());
                    result.get(currentAttemptKey).put(DormCorridorBehavior.SEQUENTIAL,0);
                    result.get(currentAttemptKey).put(DormCorridorBehavior.OTHER,0);
                    result.get(currentAttemptKey).put(DormCorridorBehavior.BACK,0);

                }

                if(path.get(roomNumber+2).equalsIgnoreCase(corrA2)){
                    result.get(currentAttemptKey).put(
                            DormCorridorBehavior.BACK,
                            result.get(currentAttemptKey).get(DormCorridorBehavior.BACK) + 1);
                    roomNumber+=1;

                }else if(path.get(roomNumber+2).equalsIgnoreCase(dorm1) && path.get(roomNumber+4).equalsIgnoreCase(dorm2)
                        && path.get(roomNumber+6).equalsIgnoreCase(corrA2)){
                    result.get(currentAttemptKey).put(
                            DormCorridorBehavior.SEQUENTIAL,
                            result.get(currentAttemptKey).get(DormCorridorBehavior.SEQUENTIAL) + 1);
                    roomNumber+=5;
                }else {
                    result.get(currentAttemptKey).put(
                            DormCorridorBehavior.OTHER,
                            result.get(currentAttemptKey).get(DormCorridorBehavior.OTHER) + 1);
                    do{
                        roomNumber++;

                    }while(!path.get(roomNumber+1).equalsIgnoreCase(corrA2));
                }




                if(!path.get(roomNumber+1).equalsIgnoreCase(corrA2)){
                    System.out.println("Trouble!");
                    assert false;
                }




            }


        }
        return result;
    }


    private void generateGroupedStackChartForCorrA2(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        List<List<String>> pathList = getPathList(nameToGraphMap);

        LinkedHashMap<String, LinkedHashMap<TurnDecision, Integer>> resultHashMap =
                calculateDecisionForEachAttemptCorrA2(pathList);



        final CategoryDataset dataSet = createAggregatedDataSetForCorrA2(resultHashMap);

        final JFreeChart chart = createAggregatedChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1280, 880));
        JFrame chartFrame = new JFrame("Summarized charts");

        chartFrame.setContentPane(chartPanel);
        chartFrame.setVisible(true);
        chartFrame.setSize(new Dimension(1300, 900));
        chartFrame.setLocation(0, 0);
        chartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }


    private void orderForRoomVisitation(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        List<List<String>> pathList = getPathList(nameToGraphMap);

        String[][] orderlyPath = new String[][]{
                {"2CorrA67", "2CorrA7", "SB1", "2CorrA7", "SB2", "2CorrA7", "SB3", "2CorrA7", "SB4", "2CorrA7", "TheLounge"},
                {"TheLounge", "2CorrA7", "SB4", "2CorrA7", "SB3", "2CorrA7", "SB2", "2CorrA7", "SB1", "2CorrA7", "2CorrA67"},
        };


        LinkedHashMap<String, LinkedHashMap<PathMovementType, Integer>> resultHashMap = new LinkedHashMap<String, LinkedHashMap<PathMovementType, Integer>>();

        resultHashMap.put("Straight Corridor", calculateMapForDistance(pathList, orderlyPath));

//           System.out.println("Yayee!");



        final CategoryDataset dataSet = createAggregatedDataSetForSequential(resultHashMap);
        final JFreeChart chart = createAggregatedChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1280, 880));
        JFrame chartFrame = new JFrame("Summarized charts");

        chartFrame.setContentPane(chartPanel);
        chartFrame.setVisible(true);
        chartFrame.setSize(new Dimension(1300, 900));
        chartFrame.setLocation(0, 0);
        chartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    private CategoryDataset createAggregatedDataSetForSequential(LinkedHashMap<String, LinkedHashMap<PathMovementType, Integer>> resultHashMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String category : resultHashMap.keySet()) {


            double total = resultHashMap.get(category).get(PathMovementType.SEQUENTIAL) + resultHashMap.get(category).get(PathMovementType.NON_SEQUENTIAL);

            dataset.addValue((double) resultHashMap.get(category).get(PathMovementType.SEQUENTIAL) / total, PathMovementType.SEQUENTIAL.toString(), category);
            dataset.addValue((double) resultHashMap.get(category).get(PathMovementType.NON_SEQUENTIAL) / total, PathMovementType.NON_SEQUENTIAL.toString(), category);

        }
        return dataset;
    }

    private LinkedHashMap<PathMovementType, Integer> calculateMapForDistance
            (List<List<String>> listOfPaths, String[][] requiredPathList) {
        LinkedHashMap<PathMovementType, Integer> occurrenceMap = new LinkedHashMap<PathMovementType, Integer>();
        occurrenceMap.put(PathMovementType.SEQUENTIAL, 0);
        occurrenceMap.put(PathMovementType.NON_SEQUENTIAL, 0);
        for (String[] requiredPath : requiredPathList) {
            updateSequentialOccurrenceMap(listOfPaths, requiredPath, occurrenceMap);

        }

        return occurrenceMap;

    }

    private void updateSequentialOccurrenceMap(List<List<String>> pathList, String[] requiredPath,
                                               LinkedHashMap<PathMovementType, Integer> occurrenceMap) {
        assert occurrenceMap != null;
        assert pathList != null;
        assert requiredPath != null && requiredPath.length > 2;
        String startingRoom = requiredPath[0];
        String secondRoom = requiredPath[1];

        int currentPosition  = 2;
        boolean matched = false;
        for (List<String> path : pathList) {
            matched=false;
            currentPosition=2;
            for (int roomNumber = 0; roomNumber < (path.size()-requiredPath.length); roomNumber++) {
                if (!path.get(roomNumber).equalsIgnoreCase(startingRoom) || !path.get(roomNumber+1).equalsIgnoreCase(secondRoom)) {
                    continue;
                }
                while(currentPosition< requiredPath.length){
                    String pathNextRoom = path.get(roomNumber + currentPosition);
                    String roomInRequired = requiredPath[currentPosition];
                    if(!pathNextRoom.equalsIgnoreCase(roomInRequired)){
                        occurrenceMap.put(PathMovementType.NON_SEQUENTIAL, occurrenceMap.get(PathMovementType.NON_SEQUENTIAL)+1);
                        matched = true;
                        System.out.println(PathMovementType.NON_SEQUENTIAL.toString());
                        break;
                    }
                    currentPosition++;
                }

                //TODO : REALLY??

                if(!matched){

                    System.out.println(PathMovementType.SEQUENTIAL.toString());
                    occurrenceMap.
                            put(PathMovementType.SEQUENTIAL,
                                    occurrenceMap.get(PathMovementType.SEQUENTIAL) + 1);
                }
                break;
            }
        }
    }


    private void generateGroupedStackChartForSimpleCorridor(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {

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

        LinkedHashMap<String, LinkedHashMap<CorridorMovementType, Integer>> resultHashMap = new LinkedHashMap<String, LinkedHashMap<CorridorMovementType, Integer>>();

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
        chartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


//        }

    }

    private JFreeChart createAggregatedChart(CategoryDataset dataSet) {
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

    private CategoryDataset createAggregatedDataSetForSimpleCorridor(
            LinkedHashMap<String,
                    LinkedHashMap<CorridorMovementType, Integer>> resultHashMap) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String category : resultHashMap.keySet()) {


            double total = resultHashMap.get(category).get(CorridorMovementType.PASS_THROUGH) + resultHashMap.get(category).get(CorridorMovementType.RETURN);

            dataset.addValue((double) resultHashMap.get(category).get(CorridorMovementType.PASS_THROUGH) / total, CorridorMovementType.PASS_THROUGH.toString(), category);
            dataset.addValue((double) resultHashMap.get(category).get(CorridorMovementType.RETURN) / total, CorridorMovementType.RETURN.toString(), category);

        }
        return dataset;

    }

    private CategoryDataset createAggregatedDataSetForCorrA2(LinkedHashMap<String, LinkedHashMap<TurnDecision, Integer>> resultHashMap) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();



        for (String category : resultHashMap.keySet()) {


            double total = resultHashMap.get(category).get(TurnDecision.NO_TURN) + resultHashMap.get(category).get(TurnDecision.TURN) + resultHashMap.get(category).get(TurnDecision.BACK);
            if(total==0){
                continue;
            }


            dataSet.addValue((double) resultHashMap.get(category).get(TurnDecision.NO_TURN) / total, TurnDecision.NO_TURN.toString(), category);
            dataSet.addValue((double) resultHashMap.get(category).get(TurnDecision.TURN) / total, TurnDecision.TURN.toString(), category);
            dataSet.addValue((double) resultHashMap.get(category).get(TurnDecision.BACK) / total, TurnDecision.BACK.toString(), category);

        }



        return dataSet;
    }
    private CategoryDataset createAggregatedDataSetForDormCorr(LinkedHashMap<String, LinkedHashMap<DormCorridorBehavior, Integer>> resultHashMap) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();


        for (String category : resultHashMap.keySet()) {


            double total = resultHashMap.get(category).get(DormCorridorBehavior.SEQUENTIAL) + resultHashMap.get(category).get(DormCorridorBehavior.OTHER) + resultHashMap.get(category).get(DormCorridorBehavior.BACK);
            if(total==0){
                continue;
            }

            dataSet.addValue((double) resultHashMap.get(category).get(DormCorridorBehavior.SEQUENTIAL) / total, DormCorridorBehavior.SEQUENTIAL.toString(), category);
            dataSet.addValue((double) resultHashMap.get(category).get(DormCorridorBehavior.OTHER) / total, DormCorridorBehavior.OTHER.toString(), category);
            dataSet.addValue((double) resultHashMap.get(category).get(DormCorridorBehavior.BACK) / total, DormCorridorBehavior.BACK.toString(), category);

        }
        return dataSet;
    }

    private List<List<String>> getPathList(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
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

    private LinkedHashMap<String, LinkedHashMap<TurnDecision, Integer>> calculateDecisionForEachAttemptCorrA2(List<List<String>> pathList) {
        LinkedHashMap<String, LinkedHashMap<TurnDecision, Integer>> result = new LinkedHashMap<String, LinkedHashMap<TurnDecision, Integer>>();
        String dormCorrString = "2DormCorr";
        String corrA1 = "2CorrA1";
        String corrA2 = "2CorrA2";
        String corrA3 = "2CorrA3";



        for (List<String> path : pathList) {
            int visitNumber = 1;
            String currentAttemptKey = "Visit " + visitNumber;
            if(!result.containsKey(currentAttemptKey)){
                result.put(currentAttemptKey,new LinkedHashMap<TurnDecision, Integer>());
                result.get(currentAttemptKey).put(TurnDecision.TURN,0);
                result.get(currentAttemptKey).put(TurnDecision.NO_TURN,0);
                result.get(currentAttemptKey).put(TurnDecision.BACK,0);

            }
            for (int roomNumber = 1; roomNumber < path.size() - 1; roomNumber++) {


                if (!path.get(roomNumber).equalsIgnoreCase(corrA2)) {
                    continue;
                }



                String pathPreviousRoom = path.get(roomNumber - 1);
                String pathNextRoom = path.get(roomNumber + 1);
                if(pathPreviousRoom.equalsIgnoreCase(dormCorrString)&& pathNextRoom.equalsIgnoreCase(dormCorrString)){
//                    System.out.println("Signs of trouble!");
                    //same attempt ...not tracking. continue to next.
                    continue;
                }
                if(pathPreviousRoom.equalsIgnoreCase(dormCorrString) && (pathNextRoom.equalsIgnoreCase(corrA1) ||pathNextRoom.equalsIgnoreCase(corrA3))){
                    visitNumber++;
                    currentAttemptKey = "Visit " + visitNumber;
                    if(!result.containsKey(currentAttemptKey)){
                        result.put(currentAttemptKey,new LinkedHashMap<TurnDecision, Integer>());
                        result.get(currentAttemptKey).put(TurnDecision.TURN,0);
                        result.get(currentAttemptKey).put(TurnDecision.NO_TURN,0);
                        result.get(currentAttemptKey).put(TurnDecision.BACK,0);

                    }
                    continue;
                }

                if ((pathPreviousRoom.equalsIgnoreCase(corrA1) && pathNextRoom.equalsIgnoreCase(corrA3))||
                        (pathPreviousRoom.equalsIgnoreCase(corrA3) && pathNextRoom.equalsIgnoreCase(corrA1))){

                    result.get(currentAttemptKey).put(
                            TurnDecision.NO_TURN,
                            result.get(currentAttemptKey).get(TurnDecision.NO_TURN) + 1);
                }else if ((pathPreviousRoom.equalsIgnoreCase(corrA1) && pathNextRoom.equalsIgnoreCase(corrA1))||
                        (pathPreviousRoom.equalsIgnoreCase(corrA3) && pathNextRoom.equalsIgnoreCase(corrA3))){

                    result.get(currentAttemptKey).put(
                            TurnDecision.BACK,
                            result.get(currentAttemptKey).get(TurnDecision.BACK) + 1);
                }else if ((pathPreviousRoom.equalsIgnoreCase(corrA1) && pathNextRoom.equalsIgnoreCase(dormCorrString))||
                        (pathPreviousRoom.equalsIgnoreCase(corrA3) && pathNextRoom.equalsIgnoreCase(dormCorrString))){

                    result.get(currentAttemptKey).put(
                            TurnDecision.TURN,
                            result.get(currentAttemptKey).get(TurnDecision.TURN) + 1);
                }else {
                    System.out.println("Something wrong");
                    assert false;
                }
            }


        }
        return result;
    }

    private LinkedHashMap<CorridorMovementType, Integer> calculateMap
            (List<List<String>> listOfPaths, String[][] straightCorridorList) {
        LinkedHashMap<CorridorMovementType, Integer> occurrenceMap = new LinkedHashMap<CorridorMovementType, Integer>();
        occurrenceMap.put(CorridorMovementType.PASS_THROUGH, 0);
        occurrenceMap.put(CorridorMovementType.RETURN, 0);
        for (String[] corridor : straightCorridorList) {
            updatePassThroughOccurrenceMap(listOfPaths, corridor, occurrenceMap);

        }

        return occurrenceMap;

    }

    private void updatePassThroughOccurrenceMap(List<List<String>> pathList, String[] corridor, LinkedHashMap<CorridorMovementType, Integer> occurrenceMap) {
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
                            CorridorMovementType.PASS_THROUGH,
                            occurrenceMap.get(CorridorMovementType.PASS_THROUGH) + 1);
                } else if (pathNextRoom.equalsIgnoreCase(corridorStartingRoom)) {
                    occurrenceMap.put(
                            CorridorMovementType.RETURN,
                            occurrenceMap.get(CorridorMovementType.RETURN) + 1);

                } else {
                    System.out.println("Incorrect call to function");
                    assert false;
                }
            }
        }

    }

    private HashMap<String, Long> calculateTimeOfCoverage(final int coverage,
                                                          final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        setEnabled(false);
        SwingWorker<HashMap<String, Long>, String> worker = new SwingWorker<HashMap<String, Long>, String>() {
            public ProgressVisualizer pv;

            @Override
            protected HashMap<String, Long> doInBackground() throws Exception {
                pv = new ProgressVisualizer("Calculating coverage times", ProgressVisualizer.DeterminateType.DETERMINATE);
                this.addPropertyChangeListener(pv);
                int i = 0;
                HashMap<String, Long> result = new HashMap<String, Long>();
                for (String name : nameToGraphMap.keySet()) {
                    DirectedSparseMultigraph<ModelObject, ModelEdge> graph = nameToGraphMap.get(name);
                    pv.print("processing " + name + "...\n");
                    long timeAtCoverage = findTimeAtCoverage(coverage, graph);
                    result.put(name, timeAtCoverage);
                    setProgress((i * 100) / nameToGraphMap.keySet().size());


                }

                pv.finish();
                return result;
            }


        };

        worker.execute();
        try {
            return worker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private long findTimeAtCoverage(int coverage, DirectedSparseMultigraph<ModelObject, ModelEdge> graph) {
        TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge o1, ModelEdge o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });

        edgeCollection.addAll(graph.getEdges());
        int totalSize = CompleteGraph.instance().getVertexCount();
        Collection<ModelObject> visitedVertices = new HashSet<ModelObject>();
        long maxTime = 0;
        for (ModelEdge edge : edgeCollection) {
            Pair<ModelObject> vertices = graph.getEndpoints(edge);
            visitedVertices.addAll(vertices);
            int currentCoverage = ((visitedVertices.size() * 100) / totalSize);
            if (currentCoverage >= coverage) {
                return edge.getTime();
            }
            maxTime = edge.getTime();
        }
        return maxTime;
    }


    private void createCurrentTitle(String room, DisplayType type, HashSet<Phase> selectedPhases) {

        currentTitle = room + ":" + type + ":";

        if (selectedPhases.contains(Phase.EXPLORATION)) {
            currentTitle += "E";
        }
        if (selectedPhases.contains(Phase.TASK_1)) {
            currentTitle += "1";
        }
        if (selectedPhases.contains(Phase.TASK_2)) {
            currentTitle += "2";
        }
        if (selectedPhases.contains(Phase.TASK_3)) {
            currentTitle += "3";
        }
        setTitle(currentTitle);
    }

    private void generateTemporalSecondOrderMarkov(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room,
                                                   String selectedSource) {
        ModelObject sourceFilterRoom;
        if (selectedSource.equals("-")) {
            sourceFilterRoom = null;
        } else {
            sourceFilterRoom = CompleteGraph.instance().findRoomByName(selectedSource);
        }


        final ModelObject mainNode = CompleteGraph.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
        ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>> localGraphs = new ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>>(MAX_NUMBER_TRACKING);

        for (int i = 0; i < MAX_NUMBER_TRACKING; i++) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
            for (ModelObject node : neighbours) {
                localGraph.addVertex(node);
            }
            localGraphs.add(localGraph);
        }


//        localGraph.addVertex(mainNode);


        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

//            long startTimeSeconds = Math.max(startTime*60000,nameToMinCoverageTimeMap.getProbabilityOfSequence(name) );
//
//            long endTimeSeconds = Math.min(endTime*60000, nameToMaxCoverageTimeMap.getProbabilityOfSequence(name));


            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });

            for (ModelEdge edge : tempGraph.getIncidentEdges(mainNode)) {
//                if (edge.getTime() >= startTimeSeconds && edge.getTime() <= endTimeSeconds)
                edgeCollection.add(edge);
            }


            ModelEdge lonelyEdge = null;
            if (tempGraph.inDegree(mainNode) > tempGraph.outDegree(mainNode)) {
                //Ending Vertex;
                lonelyEdge = edgeCollection.pollLast();

//                localGraph.addVertex(mainNode);
//                localGraph.addEdge(new ModelEdge(),
//                        tempGraph.getOpposite(mainNode, lonelyEdge),
//                        mainNode
//                );

            } else if (tempGraph.inDegree(mainNode) < tempGraph.outDegree(mainNode)) {
                //First Vertex;
                lonelyEdge = edgeCollection.pollFirst();


//                localGraph.addVertex(mainNode);
//                localGraph.addEdge(new ModelEdge(),
//                        mainNode,
//                        tempGraph.getOpposite(mainNode, lonelyEdge)
//                );

            }
            if (tempGraph.inDegree(mainNode) != tempGraph.outDegree(mainNode)) {
                System.out.println("YOU're screwed!!!");
            }

            int visitNumber = 0;

            int size = edgeCollection.size();
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = localGraphs.get(visitNumber);
            for (int i = 0; i < size / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);

                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                if (sourceFilterRoom == null || sourceFilterRoom.equals(from))
                    localGraph.addEdge(new ModelEdge(), from, to);
                if (visitNumber < MAX_NUMBER_TRACKING - 1 && isLeaving(outgoing, tempGraph, to, mainNode)) {
                    visitNumber++;
                    localGraph = localGraphs.get(visitNumber);
                }
            }
        }


//        if (sourceFilterRoom == null) {
//            for (int i = 0; i < MAX_NUMBER_TRACKING; i++) {
//                DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = localGraphs.get(i);
//                final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = convertToProbabilities(localGraph);
//
////            ArrayList<ProbabilityEdge> edges = new ArrayList<ProbabilityEdge>(graphToDraw.getEdges());
////            int size = graphToDraw.getEdges().size();
////            for(int j=0;j<size;j++ ){
////                ProbabilityEdge edge = edges.getProbabilityOfSequence(j);
////                if(edge.prob<0.1) {
////                    graphToDraw.getEdges().remove(edge);
////                }
////            }
//                final int currentAttemptNumber = i;
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        JPanel panel = drawInPanel(graphToDraw, mainNode);
//                        JFrame frame = new JFrame();
//                        frame.getContentPane().add(panel);
//                        frame.setVisible(true);
//                        frame.setSize(600, 600);
//                        frame.revalidate();
//
//                        frame.setTitle("For attempt number :" + (currentAttemptNumber + 1));
//                    }
//                });
//            }
//        } else {
        final CategoryDataset dataSet = createDataSet(localGraphs);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        JFrame chartFrame = new JFrame(room);

        chartFrame.setContentPane(chartPanel);
        chartFrame.setVisible(true);
        chartFrame.setSize(new Dimension(520, 300));
        chartFrame.setLocation(0, 0);
        chartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


//        }


    }

    private JFreeChart createChart(CategoryDataset dataset) {
        final JFreeChart stackedChart = ChartFactory.createStackedBarChart("Through " + roomButtonComboBox.getItemAt(roomButtonComboBox.getSelectedIndex()), "Category", "Value",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        //create group
        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
        List rowKeys = dataset.getRowKeys();
        KeyToGroupMap map = null;

        HashSet<String> sourceSet = new HashSet<String>();


        for (Object row : rowKeys) {
            String sourceDestinationMap = row.toString();
            String[] parts = sourceDestinationMap.split(":");
            String source = parts[0];


            if (map == null) {
                map = new KeyToGroupMap(source);
            }
            map.mapKeyToGroup(sourceDestinationMap, source);

            sourceSet.add(source);

        }

        SubCategoryAxis domainAxis = new SubCategoryAxis("Choice / Attempt");
//        //Margin between group
////        domainAxis.setCategoryMargin(0.1);
//// descale the position of the category label
//        domainAxis.setCategoryLabelPositionOffset(70);
//// apply the affine trasnform with a rotation and a translate
//        AffineTransform trans = AffineTransform.getTranslateInstance(0, 65);
//        trans.concatenate(AffineTransform.getRotateInstance(-Math.PI / 4));
//        domainAxis.setSubLabelFont(domainAxis.getSubLabelFont().deriveFont(1, trans));
//        for(String key: sourceSet){
//            domainAxis.addSubCategory(key);
//        }
//        dom_axis.setSub(CategoryLabelPositions.DOWN_90);
        //end


        renderer.setSeriesToGroupMap(map);
        //margin between bar.
        renderer.setItemMargin(0.03);
        //end


        CategoryPlot plot = (CategoryPlot) stackedChart.getPlot();
        plot.setDomainAxis(domainAxis);
        plot.setRenderer(renderer);

        return stackedChart;
    }

    private CategoryDataset createDataSet(ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>> localGraphs) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < MAX_NUMBER_TRACKING; i++) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = localGraphs.get(i);
            final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = convertToProbabilities(localGraph);
            for (ProbabilityEdge edge : graphToDraw.getEdges()) {
                String source = graphToDraw.getSource(edge).toString();
                String destination = graphToDraw.getDest(edge).toString();
                double value = edge.prob; //TODO: with frequency instead of probability

                String valueForBar = source + ":" + destination;
                String category = "Attempt " + (i + 1);
                dataset.addValue(value, valueForBar, category);

            }

        }
        return dataset;
    }

    private boolean isLeaving(ModelEdge incomingEdge, DirectedSparseMultigraph<ModelObject, ModelEdge> graph, ModelObject mainNode, ModelObject from) {
        long timeOfIncomingEdge = incomingEdge.getTime();
        TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge o1, ModelEdge o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });

        edgeCollection.addAll(graph.getOutEdges(mainNode));

        for (ModelEdge edge : edgeCollection) {
            if (edge.getTime() >= timeOfIncomingEdge) {
                ModelObject destination = graph.getOpposite(mainNode, edge);
                if (destination.toString().equals(from.toString())) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return true;

    }

    private void generateFirstOrderProbabilities(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room, int startTimeSeconds, int endTimeSeconds, HashMap<String, Long> nameToMinCoverageTimeMap, HashMap<String, Long> nameToMaxCoverageTimeMap) {

        ModelObject mainNode = CompleteGraph.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }

//            Collection<ModelEdge> edgeCollection = tempGraph.getInEdges(mainNode);
//
//            for (ModelEdge edge : edgeCollection) {
//
//                if (edge.getTime() >= startTimeSeconds * 60000 && edge.getTime() <= endTimeSeconds * 60000)
//                    localGraph.addEdge(new ModelEdge(), tempGraph.getOpposite(mainNode, edge),
//                            mainNode);
//
//            }


            Collection<ModelEdge> edgeCollection = tempGraph.getOutEdges(mainNode);

            for (ModelEdge edge : edgeCollection) {
                if (edge.getTime() >= startTimeSeconds * 60000 && edge.getTime() <= endTimeSeconds * 60000)
                    localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
            }

        }


        final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = convertToProbabilities(localGraph);

        renderGraphPanel(graphToDraw, mainNode, DisplayType.FIRST_ORDER_MARKOV);


    }

    private DirectedSparseMultigraph<ModelObject, ProbabilityEdge> convertToProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
        HashMap<ModelObject, HashMap<ModelObject, Integer>> nodeToNodeTravelFrequency = new HashMap<ModelObject, HashMap<ModelObject, Integer>>();

        for (ModelEdge edge : localGraph.getEdges()) {
            ModelObject source = localGraph.getSource(edge);
            ModelObject destination = localGraph.getDest(edge);
//            if (source.toString().equals(destination.toString())) {
//                System.out.println("Cycle detected");
//            }

            if (!nodeToNodeTravelFrequency.containsKey(source)) {
                nodeToNodeTravelFrequency.put(source, new HashMap<ModelObject, Integer>());
            }
            if (!nodeToNodeTravelFrequency.get(source).containsKey(destination)) {
                nodeToNodeTravelFrequency.get(source).put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
            nodeToNodeTravelFrequency.get(source).put(destination, currentValue + 1);

        }

        HashMap<ModelObject, HashMap<ModelObject, Double>> nodeToNodeProbabilities = new HashMap<ModelObject, HashMap<ModelObject, Double>>();

        for (ModelObject source : nodeToNodeTravelFrequency.keySet()) {
            nodeToNodeProbabilities.put(source, new HashMap<ModelObject, Double>());
            double totalNumberOfOutEdges = 0.0;
            for (ModelObject dest : nodeToNodeTravelFrequency.get(source).keySet()) {
                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source).get(dest);
            }
            for (ModelObject dest : nodeToNodeTravelFrequency.get(source).keySet()) {

                nodeToNodeProbabilities.get(source).put(dest, (double) nodeToNodeTravelFrequency.get(source).get(dest) / totalNumberOfOutEdges);
            }


        }


        DirectedSparseMultigraph<ModelObject, ProbabilityEdge> summarizedGraph = new DirectedSparseMultigraph<ModelObject, ProbabilityEdge>();
        for (ModelObject vertex : localGraph.getVertices()) {
            summarizedGraph.addVertex(vertex);
        }
        for (ModelObject source : nodeToNodeProbabilities.keySet()) {

            for (ModelObject dest : nodeToNodeProbabilities.get(source).keySet()) {
                if (source.toString().equals(dest.toString())) {
                    System.out.println("Cycle");
                }

                summarizedGraph.addEdge(new ProbabilityEdge(nodeToNodeProbabilities.get(source).get(dest), nodeToNodeTravelFrequency.get(source).get(dest)), source, dest);

            }


        }
        return summarizedGraph;
    }


    private void generateSecondOrderProbabilities(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room,
                                                  int startTime, int endTime, HashMap<String, Long> nameToMinCoverageTimeMap,
                                                  HashMap<String, Long> nameToMaxCoverageTimeMap) {
        ModelObject mainNode = CompleteGraph.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = CompleteGraph.instance().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();


//        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);
            long startTimeSeconds = Math.max(startTime * 60000, nameToMinCoverageTimeMap.get(name));

            long endTimeSeconds = Math.min(endTime * 60000, nameToMaxCoverageTimeMap.get(name));
            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });

            for (ModelEdge edge : tempGraph.getIncidentEdges(mainNode)) {
                if (edge.getTime() >= startTimeSeconds && edge.getTime() <= endTimeSeconds)
                    edgeCollection.add(edge);
            }


            ModelEdge lonelyEdge = null;
            if (tempGraph.inDegree(mainNode) > tempGraph.outDegree(mainNode)) {
                //Ending Vertex;
                lonelyEdge = edgeCollection.pollLast();

                localGraph.addVertex(mainNode);
                localGraph.addEdge(new ModelEdge(),
                        tempGraph.getOpposite(mainNode, lonelyEdge),
                        mainNode
                );

            } else if (tempGraph.inDegree(mainNode) < tempGraph.outDegree(mainNode)) {
                //First Vertex;
                lonelyEdge = edgeCollection.pollFirst();


                localGraph.addVertex(mainNode);
                localGraph.addEdge(new ModelEdge(),
                        mainNode,
                        tempGraph.getOpposite(mainNode, lonelyEdge)
                );

            }
            if (tempGraph.inDegree(mainNode) != tempGraph.outDegree(mainNode)) {
                System.out.println("YOU're screwed!!!");
            }
            int size = edgeCollection.size();
            for (int i = 0; i < size / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);
            }
        }


        final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = convertToProbabilities(localGraph);
        renderGraphPanel(graphToDraw, mainNode, DisplayType.SECOND_ORDER_MARKOV);

    }

    private JPanel drawInPanel(DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw, final ModelObject mainNode) {
        Layout<ModelObject, ProbabilityEdge> layout = new SpringLayout2<ModelObject, ProbabilityEdge>(graphToDraw);


        layout.setSize(new Dimension(600, 500));


        VisualizationViewer<ModelObject, ProbabilityEdge> tempVisualizationViewer = new VisualizationViewer<ModelObject, ProbabilityEdge>(layout);
        tempVisualizationViewer.setPreferredSize(new Dimension(600, 500));


        // Setup up a new vertex to paint transformer...
        tempVisualizationViewer.getRenderContext().setVertexFillPaintTransformer(new Transformer<ModelObject, Paint>() {
            @Override
            public Paint transform(ModelObject modelObject) {
                if (modelObject.toString().equals(mainNode.toString())) {
                    return Color.RED;
                } else {
                    return Color.WHITE;
                }
            }
        });

        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        tempVisualizationViewer.setGraphMouse(gm);


        tempVisualizationViewer.getRenderContext().setVertexShapeTransformer(new VertexEllipseTransformer<ModelObject, Shape>());


        tempVisualizationViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<ModelObject>());
        tempVisualizationViewer.getRenderer().getVertexLabelRenderer().setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR);

//                if (style.equals(DisplayType.SECOND_ORDER_MARKOV)) {
        tempVisualizationViewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
//                }


        tempVisualizationViewer.revalidate();

        return tempVisualizationViewer;
    }

    private <T extends ModelEdge> void renderGraphPanel(final DirectedSparseMultigraph<ModelObject, T> graphToDraw, final ModelObject mainNode, final DisplayType style) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {


                Layout<ModelObject, T> layout = new SpringLayout2<ModelObject, T>(graphToDraw);


                layout.setSize(new Dimension(600, 500));


                currentVisualizationViewer = new VisualizationViewer<ModelObject, T>(layout);
                currentVisualizationViewer.setPreferredSize(new Dimension(600, 500));


                // Setup up a new vertex to paint transformer...
                currentVisualizationViewer.getRenderContext().setVertexFillPaintTransformer(new Transformer<ModelObject, Paint>() {
                    @Override
                    public Paint transform(ModelObject modelObject) {
                        if (modelObject.toString().equals(mainNode.toString())) {
                            return (Paint) Color.RED;
                        } else {
                            return (Paint) Color.WHITE;
                        }
                    }
                });

                // Create a graph mouse and add it to the visualization component
                DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
                gm.setMode(ModalGraphMouse.Mode.PICKING);
                currentVisualizationViewer.setGraphMouse(gm);


                currentVisualizationViewer.getRenderContext().setVertexShapeTransformer(new VertexEllipseTransformer<ModelObject, Shape>());


                currentVisualizationViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<ModelObject>());
                currentVisualizationViewer.getRenderer().getVertexLabelRenderer().setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR);

//                if (style.equals(DisplayType.SECOND_ORDER_MARKOV)) {
                currentVisualizationViewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
//                }


                chartDisplayPanel.add(currentVisualizationViewer);
                currentVisualizationViewer.revalidate();
                chartDisplayPanel.getRootPane().revalidate();

            }


        });
    }


    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRelevantGraphToNameMap(final Collection<String> dataNames, final HashSet<Phase> selectedPhases) throws ExecutionException, InterruptedException {

        setEnabled(false);
        SwingWorker<HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> tempWorker = new SwingWorker<HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>() {


            public ProgressVisualizer pv;

            @Override
            protected HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {
                pv = new ProgressVisualizer("generating graphs...", ProgressVisualizer.DeterminateType.DETERMINATE);

                HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> result = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                final int size = dataNames.size();
                int i = 1;
                this.addPropertyChangeListener(pv);
                for (String dataName : dataNames) {
                    final String tempDataName = dataName;
                    pv.print("Processing " + tempDataName + "...\n");

                    synchronized (NetworkModel.instance()) {
                        result.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, selectedPhases));
                    }
                    final int currentProgress = i;

                    setProgress((currentProgress * 100) / size);

                    i++;
                }
                return result;
            }

            @Override
            protected void done() {
                Toolkit.getDefaultToolkit().beep();
                pv.finish();
                setEnabled(true);
            }


        };

        tempWorker.execute();


        return tempWorker.get();
    }


    public JComboBox<String> generateRoomButtonComboBox() {
        roomButtonComboBox = new JComboBox<String>();
        synchronized (roomList) {
            if (roomList != null && !roomList.isEmpty()) {
                SwingUtilities.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (String room : roomList) {
                                    roomButtonComboBox.addItem(room);
                                }
                                roomButtonComboBox.setEditable(false);
                                roomButtonComboBox.validate();
                            }
                        }
                );
            }
        }

        return roomButtonComboBox;
    }


    public enum DisplayType {
        FIRST_ORDER_MARKOV("1st order markov"),
        SECOND_ORDER_MARKOV("2nd order markov"),
        TEMPORAL_SECOND_ORDER_MARKOV("Temporal 2nd order markov"),
        GROUPED_STACK_CHART_FOR_DORM_CORR("Dorm Corridor Summary"),
        GROUPED_STACK_CHART_FOR_CORR_A2("Corr A2 Corridor Summary"),
        GROUPED_STACK_CHART_FOR_SIMPLE_CORRIDOR("Simple Corridor Summary"),
        ORDER_FOR_ROOM_VISITATION("Order for Room Visitation."),
        CUMULATIVE_FREQUENCY_VISITS("Cumulative frequency of visits");
        private final String name;

        DisplayType(String s) {
            name = s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void initializeRoomList() {

        synchronized (roomList) {
            roomList = new ArrayList<String>(CompleteGraph.instance().getSortedRooms());
        }

    }


    private class VertexEllipseTransformer<ModelObject, Shape> implements Transformer<ModelObject, Shape> {

        @Override
        public Shape transform(ModelObject modelObject) {

//            int width = modelObject.toString().length() * 10;
//
//
//            return (Shape) new Ellipse2D.Double(-width / 2, -width / 2, width, width);
            int width = modelObject.toString().length() * 10;
            return (Shape) new Rectangle(-width / 2, -10, width, 20);

        }
    }

    private class RoomDataListener implements ActionListener, ChangeListener {


        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == roomButtonComboBox) {
                final String currentRoom = roomButtonComboBox.getItemAt(roomButtonComboBox.getSelectedIndex());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Collection<ModelObject> neighbours = CompleteGraph.instance().
                                getNeighbors(CompleteGraph.instance().findRoomByName(currentRoom));
                        sourceComboBox.removeAllItems();
                        sourceComboBox.addItem("-");
                        for (ModelObject neighbour : neighbours) {
                            String neighbourString = neighbour.toString();
                            sourceComboBox.addItem(neighbourString);
                        }

                        sourceComboBox.setSelectedIndex(0);
                    }
                });
            } else if (event.getSource() == closeButton) {
                SwingUtilities.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (JFrame frame : poppedOutFrames) {
                                    frame.dispose();
                                }
                                dispose();
                            }
                        });

            } else if (event.getSource() == popOutButton) {
                SwingUtilities.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                JFrame poppedOutFrame = new JFrame();

                                poppedOutFrame.setSize(chartDisplayPanel.getSize());
                                poppedOutFrame.add(currentVisualizationViewer);
                                poppedOutFrame.setVisible(true);
                                poppedOutFrame.setTitle(currentTitle);
                                poppedOutFrames.add(poppedOutFrame);

                            }
                        });
            } else if (event.getSource() == generateButton) {
                updateChartPanel();
            } else if (event.getSource() == saveAsImageButton) {


                if (currentVisualizationViewer != null) {
                    JFileChooser jfc = new JFileChooser(new File("." + File.separatorChar + "SavedGraphs"));
//                    jfc.addChoosableFileFilter(new PngFileFilter());

                    int result = jfc.showSaveDialog(RoomAnalysisFrame.this);
                    if (result == JFileChooser.CANCEL_OPTION)
                        return;
                    File file = jfc.getSelectedFile();


                    writeToDisk(file);
                } else {

                    JOptionPane.showMessageDialog(RoomAnalysisFrame.this, "No graph to save", "Export error", JOptionPane.ERROR_MESSAGE);
                }

            }

        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == startTimeSelection) {
                if (!startTimeSelection.getValueIsAdjusting()) {
//                    System.out.println("Start time at "+startTimeSelection.getProbabilityOfSequence());
                    final String currentValue = String.valueOf(startTimeSelection.getValue());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            startTimeValue.setText(currentValue);
                        }
                    });
                }
            } else if (e.getSource() == endTimeSelection) {
                if (!endTimeSelection.getValueIsAdjusting()) {
//                    System.out.println("End time at "+endTimeSelection.getProbabilityOfSequence());
                    final String currentValue = String.valueOf(endTimeSelection.getValue());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            endTimeValue.setText(currentValue);
                        }
                    });
                }
            } else if (e.getSource() == minCoverageSlider) {
                if (!minCoverageSlider.getValueIsAdjusting()) {
//                    System.out.println("Start time at "+startTimeSelection.getProbabilityOfSequence());
                    final String currentValue = String.valueOf(minCoverageSlider.getValue());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            minCoverageLabel.setText(currentValue);
                        }
                    });
                }
            } else if (e.getSource() == maxCoverageSlider) {
                if (!maxCoverageSlider.getValueIsAdjusting()) {
//                    System.out.println("End time at "+endTimeSelection.getProbabilityOfSequence());
                    final String currentValue = String.valueOf(maxCoverageSlider.getValue());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            maxCoverageLabel.setText(currentValue);
                        }
                    });
                }
            }
        }
    }

    public void writeToDisk(File file) {
        //Dimension loDims = getGraphLayout().getSize();
        Dimension vsDims = currentVisualizationViewer.getSize();

        int width = vsDims.width;
        int height = vsDims.height;
        Color bg = currentVisualizationViewer.getBackground();

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics = im.createGraphics();
        graphics.setColor(bg);
        graphics.fillRect(0, 0, width, height);


        currentVisualizationViewer.paint(graphics);

        try {
            ImageIO.write(im, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ProbabilityEdge extends ModelEdge {
        private final Double prob;
        private final Integer freq;

        public ProbabilityEdge(Double value, Integer freq) {
            this.prob = value;
            this.freq = freq;
        }

        @Override
        public String toString() {
            String valueString = new DecimalFormat("#.000").format(prob);
            return valueString + "(" + freq + ")";
        }
    }

    public static class LimitedBoundedRangeModel extends DefaultBoundedRangeModel {

        enum MAX_OR_MIN {
            MAX, MIN;
        }

        ;

        BoundedRangeModel limit;
        MAX_OR_MIN constraintType;

        public LimitedBoundedRangeModel(BoundedRangeModel limit, MAX_OR_MIN constraintType, int min, int max) {
            this.limit = limit;
            this.constraintType = constraintType;
            this.setMaximum(max);
            this.setMinimum(min);
        }

        /**
         * @inherited <p>
         */
        @Override
        public void setRangeProperties(int newValue, int newExtent, int newMin,
                                       int newMax, boolean adjusting) {
            if (limit != null) {
                if (constraintType == MAX_OR_MIN.MAX) {
                    //can't be greater than other value
                    if (newValue >= limit.getValue()) {
                        newValue = limit.getValue() - 1;
                    }
                } else {
                    if (newValue <= limit.getValue()) {
                        newValue = limit.getValue() + 1;
                    }
                }
            }


            boolean invoke =
                    (adjusting != getValueIsAdjusting()) && !adjusting;
            super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
            if (invoke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        fireStateChanged();
                    }
                });
            }

        }
    }

    public enum CorridorMovementType {
        PASS_THROUGH, RETURN;
    }

    public enum PathMovementType {
        SEQUENTIAL, NON_SEQUENTIAL;
    }

    public enum TurnDecision {
        TURN, NO_TURN, BACK;
    }

    public enum DormCorridorBehavior {
        BACK, SEQUENTIAL, OTHER;
    }

}
