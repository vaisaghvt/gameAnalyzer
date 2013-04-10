package stats.chartdisplays;

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
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.Transformer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 14/3/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class RoomAnalysisFrame extends JFrame {
    private static final int MAX_NUMBER_TRACKING = 10;
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

        LimitedBoundedRangeModel startModel = new LimitedBoundedRangeModel(null, LimitedBoundedRangeModel.MAX_OR_MIN.MAX,0,45);
        LimitedBoundedRangeModel endModel = new LimitedBoundedRangeModel(startModel, LimitedBoundedRangeModel.MAX_OR_MIN.MIN,0,45);
        startModel.limit = endModel;
        startTimeSelection = new JSlider(startModel);
        startTimeSelection.setValue(0);
        endTimeSelection = new JSlider(endModel);
        endTimeSelection.setValue(45);

        LimitedBoundedRangeModel minCoverage = new LimitedBoundedRangeModel(null, LimitedBoundedRangeModel.MAX_OR_MIN.MAX,0,100);
        LimitedBoundedRangeModel maxCoverage = new LimitedBoundedRangeModel(minCoverage, LimitedBoundedRangeModel.MAX_OR_MIN.MIN,0,100);
        minCoverage.limit = maxCoverage;
        minCoverageSlider = new JSlider(minCoverage);
        minCoverageSlider.setValue(0);
        maxCoverageSlider = new JSlider(maxCoverage);
        maxCoverageSlider.setValue(100);


        generateButton.addActionListener(roomDataListener);
        closeButton.addActionListener(roomDataListener);
        displayTypeJComboBox = new JComboBox<DisplayType>();
        for(DisplayType type : DisplayType.values()){
            displayTypeJComboBox.addItem(type);
        }
        displayTypeJComboBox.setEditable(false);


        JPanel buttonPanel = new JPanel(new GridLayout(11, 2));

        buttonPanel.add(new JLabel("Choose room :"));
        buttonPanel.add(getRoomButtonComboBox());

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
                            generateTemporalSecondOrderMarkov(nameToGraphMap, room, startTimeSeconds, endTimeSeconds, nameToMinCoverageTimeMap, nameToMaxCoverageTimeMap);
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

    private HashMap<String, Long> calculateTimeOfCoverage(final int coverage, final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        setEnabled(false);
        SwingWorker<HashMap<String, Long>, String> worker = new SwingWorker<HashMap<String, Long>, String>() {
            public ProgressVisualizer pv;

            @Override
            protected HashMap<String, Long> doInBackground() throws Exception {
                pv = new ProgressVisualizer("Calculating coverage times", ProgressVisualizer.DeterminateType.DETERMINATE);
                this.addPropertyChangeListener(pv);
                int i=0;
                HashMap<String, Long> result = new HashMap<String, Long>();
                for(String name:nameToGraphMap.keySet()){
                    DirectedSparseMultigraph<ModelObject, ModelEdge> graph = nameToGraphMap.get(name);
                    pv.print("processing "+name+"...\n");
                    long timeAtCoverage = findTimeAtCoverage(coverage, graph);
                    result.put(name, timeAtCoverage);
                    final int currentProgress = i;
                    setProgress((currentProgress*100) /nameToGraphMap.keySet().size());


                }

                 pv.finish();
                return result;
            }


        };

        worker.execute();
        try {
            return worker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        int totalSize = NetworkModel.instance().getCompleteGraph().getVertexCount();
        Collection<ModelObject> visitedVertices = new HashSet<ModelObject>();
        long maxTime=0;
        for(ModelEdge edge: edgeCollection){
            Pair<ModelObject> vertices = graph.getEndpoints(edge);
            visitedVertices.addAll(vertices);
            int currentCoverage = ((visitedVertices.size() * 100) / totalSize);
            if(currentCoverage>=coverage){
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
                                                   int startTime, int endTime, HashMap<String, Long> nameToMinCoverageTimeMap, HashMap<String, Long> nameToMaxCoverageTimeMap) {
        final ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>> localGraphs = new ArrayList<DirectedSparseMultigraph<ModelObject, ModelEdge>>(MAX_NUMBER_TRACKING);

        for(int i=0;i<MAX_NUMBER_TRACKING;i++){
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
            for (ModelObject node : neighbours) {
                localGraph.addVertex(node);
            }
            localGraphs.add(localGraph);
        }




//        localGraph.addVertex(mainNode);



        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);
//            long startTimeSeconds = Math.max(startTime*60000,nameToMinCoverageTimeMap.getValue(name) );
//
//            long endTimeSeconds = Math.min(endTime*60000, nameToMaxCoverageTimeMap.getValue(name));



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
            if(tempGraph.inDegree(mainNode)!= tempGraph.outDegree(mainNode)){
                System.out.println("YOU're screwed!!!");
            }

            int visitNumber =0;

            int size = edgeCollection.size();
            DirectedSparseMultigraph<ModelObject,ModelEdge> localGraph = localGraphs.get(visitNumber);
            for (int i = 0; i < size / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);
                if(visitNumber<MAX_NUMBER_TRACKING-1 && isLeaving(outgoing, tempGraph, to, mainNode)){
                    visitNumber++;
                    localGraph = localGraphs.get(visitNumber);
                }
            }
        }


        for(int i=0;i<MAX_NUMBER_TRACKING;i++){
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = localGraphs.get(i);
            final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = convertToProbabilities(localGraph);

//            ArrayList<ProbabilityEdge> edges = new ArrayList<ProbabilityEdge>(graphToDraw.getEdges());
//            int size = graphToDraw.getEdges().size();
//            for(int j=0;j<size;j++ ){
//                ProbabilityEdge edge = edges.getValue(j);
//                if(edge.prob<0.1) {
//                    graphToDraw.getEdges().remove(edge);
//                }
//            }
            final int currentAttemptNumber = i;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JPanel panel = drawInPanel(graphToDraw, mainNode);
                    JFrame frame = new JFrame();
                    frame.getContentPane().add(panel);
                    frame.setVisible(true);
                    frame.setSize(600,600);
                    frame.revalidate();

                    frame.setTitle("For attempt number :"+ (currentAttemptNumber+1));
                }
            });
        }


    }



    private boolean isLeaving(ModelEdge incomingEdge, DirectedSparseMultigraph<ModelObject, ModelEdge> graph,ModelObject mainNode, ModelObject from) {
        long timeOfIncomingEdge = incomingEdge.getTime();
        TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge o1, ModelEdge o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });

        edgeCollection.addAll(graph.getOutEdges(mainNode));

        for(ModelEdge edge :edgeCollection){
            if(edge.getTime()>= timeOfIncomingEdge){
                ModelObject destination = graph.getOpposite(mainNode, edge);
                if(destination.toString().equals(from.toString())){
                    return false;
                }
                else {
                    return true;
                }
            }
        }
        return true;

    }

    private void generateFirstOrderProbabilities(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room, int startTimeSeconds, int endTimeSeconds, HashMap<String, Long> nameToMinCoverageTimeMap, HashMap<String, Long> nameToMaxCoverageTimeMap) {

        ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
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

                summarizedGraph.addEdge(new ProbabilityEdge(nodeToNodeProbabilities.get(source).get(dest),nodeToNodeTravelFrequency.get(source).get(dest)), source, dest);

            }


        }
        return summarizedGraph;
    }


    private void generateSecondOrderProbabilities(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room,
                                                  int startTime, int endTime, HashMap<String, Long> nameToMinCoverageTimeMap,
                                                  HashMap<String, Long> nameToMaxCoverageTimeMap) {
        ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();


//        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (String name : nameToGraphMap.keySet()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);
            long startTimeSeconds = Math.max(startTime*60000,nameToMinCoverageTimeMap.get(name) );

            long endTimeSeconds = Math.min(endTime*60000, nameToMaxCoverageTimeMap.get(name));
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
            if(tempGraph.inDegree(mainNode)!= tempGraph.outDegree(mainNode)){
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

    private JPanel drawInPanel(DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw,final  ModelObject mainNode) {
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


    public JComboBox<String> getRoomButtonComboBox() {
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
        TEMPORAL_SECOND_ORDER_MARKOV("Temporal 2nd order markov");
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
            roomList = new ArrayList<String>(NetworkModel.instance().getSortedRooms());
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
            if (event.getSource() == closeButton) {
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
//                    System.out.println("Start time at "+startTimeSelection.getValue());
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
//                    System.out.println("End time at "+endTimeSelection.getValue());
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
//                    System.out.println("Start time at "+startTimeSelection.getValue());
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
//                    System.out.println("End time at "+endTimeSelection.getValue());
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
            this.freq= freq;
        }

        @Override
        public String toString() {
            String valueString= new DecimalFormat("#.000").format(prob);
            return valueString+"("+freq+")";
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

}
