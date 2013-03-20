package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gui.NetworkModel;
import gui.Phase;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 19/3/13
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathPredictionFrame extends JFrame {
    private JPanel dataPanel;
    private JPanel chartDisplayPanel = new JPanel();
    private Collection<JFrame> poppedOutFrames = new HashSet<JFrame>();

    private JCheckBox explorationCheckBox = new JCheckBox("Exploration");
    private JCheckBox task1CheckBox = new JCheckBox("Task 1");
    private JCheckBox task2CheckBox = new JCheckBox("Task 2");
    private JCheckBox task3CheckBox = new JCheckBox("Task 3");
    private JButton closeButton = new JButton("Close");
    private JButton generateButton = new JButton("Generate");
    private JComboBox<Integer> pathLengthComboBox;
    private JComboBox<String> roomButtonComboBox;
    private Collection<String> roomList = new ArrayList<String>();
    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> data;
    private JButton popOutButton = new JButton("Pop-out");
    private Collection<String> dataNameList;


    private JButton saveAsImageButton = new JButton("Save as...");

    private ActionListener roomDataListener;
    private VisualizationViewer<ModelObject, ? extends ModelEdge> currentVisualizationViewer;
    private String currentTitle;

    public PathPredictionFrame(Collection<String> dataNames) {
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


        generateButton.addActionListener(roomDataListener);
        closeButton.addActionListener(roomDataListener);
        pathLengthComboBox = new JComboBox<Integer>();
        for (int i = 1; i < 21; i++) {
            pathLengthComboBox.addItem(new Integer(i));
        }
        pathLengthComboBox.setEditable(false);


        JPanel buttonPanel = new JPanel(new GridLayout(7, 2));

        buttonPanel.add(new JLabel("Choose Starting location :"));
        buttonPanel.add(getRoomButtonComboBox());

        buttonPanel.add(new JLabel("Select phases"));
        buttonPanel.add(explorationCheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task1CheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task2CheckBox);

        buttonPanel.add(new JLabel());
        buttonPanel.add(task3CheckBox);

        buttonPanel.add(new JLabel("Path hop length:"));
        buttonPanel.add(pathLengthComboBox);


        buttonPanel.add(generateButton);
        buttonPanel.add(closeButton);


        return buttonPanel;

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
            final int pathLength = pathLengthComboBox.getItemAt(pathLengthComboBox.getSelectedIndex()).intValue();
            final String room = roomButtonComboBox.getItemAt(roomButtonComboBox.getSelectedIndex());

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            });
            SwingWorker<Void, Void> tempWorker = new SwingWorker<Void, Void>() {
                public HashMap<String, Double> result;

                @Override
                protected Void doInBackground() throws Exception {
                    System.out.println("Generating name to graph mapping");
                    createCurrentTitle(room, pathLength, selectedPhases);

                    HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap = generateRelevantGraphToNameMap(dataNameList, selectedPhases);

                    result = predictPath(nameToGraphMap, pathLength, room);

                    return null;
                }

                protected void done() {
                    for(String roomName: result.keySet()){
                        System.out.println(roomName+":"+ result.get(roomName));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(room + ": path length =" + pathLength);
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

    private HashMap<String, Double> predictPath(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, int pathLength, String startRoom) {

        HashMap<String, Double> results = new HashMap<String, Double>();

        HashMap<String, Double> firstOrderProbabilities = calculateFirstOrderProbForNeighbouringRooms(nameToGraphMap, startRoom);

        HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities = calculateSecondOrderProb(nameToGraphMap);

        HashMultimap<String, String> currentHistories = HashMultimap.create();


        for (String room : firstOrderProbabilities.keySet()) {
            results.put(room, firstOrderProbabilities.get(room));
            currentHistories.put(room, startRoom);

        }

        for (int hops = 1; hops < pathLength; hops++) {
            addNextHop(results, currentHistories, secondOrderProbabilities);
        }

        return results;
    }

    private HashMap<String, HashMap<String, HashMap<String, Double>>> calculateSecondOrderProb(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {

        HashMap<String, HashMap<String, HashMap<String, Double>>> result = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
        for(String room:roomList){
            result.put(room,
                    calculateSecondOrderProbForNeighbouringRooms(nameToGraphMap, room));

        }

        return result;
    }

    private void addNextHop(HashMap<String, Double> results,
            HashMultimap<String, String> currentHistories,
            HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities) {
        HashMultimap<String, String> futureHistories = HashMultimap.create();
        HashMap<String, Double> newResult = new HashMap<String, Double>();

        for(String currentLocation:currentHistories.keySet()){
            double priorProbability = results.get(currentLocation);
            HashMap<String, HashMap<String, Double>> currentLocationSecondOrderProbabilities = secondOrderProbabilities.get(currentLocation);
            for(String prevLocation: currentHistories.get(currentLocation)) {
                HashMap<String, Double> secondOrderProbabilitiesForConsideration = currentLocationSecondOrderProbabilities.get(prevLocation);
                for(String possibleDestination: secondOrderProbabilitiesForConsideration.keySet()){
                    double secondOrderValue = secondOrderProbabilitiesForConsideration.get(possibleDestination).doubleValue();

                    double probabilityToWrite = priorProbability *secondOrderValue;
                    if(newResult.containsKey(possibleDestination)){
                        double newProbability = newResult.get(possibleDestination)+probabilityToWrite
                                ;
                        newResult.put(possibleDestination,newProbability );
                    }else{
                        newResult.put(possibleDestination,probabilityToWrite );
                    }
                    futureHistories.put(possibleDestination, currentLocation);
                }
            }

        }

        currentHistories.clear();
        currentHistories.putAll(futureHistories);

        results.clear();
        results.putAll(newResult);
    }

    private HashMap<String, HashMap<String, Double>> calculateSecondOrderProbForNeighbouringRooms(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String startRoom) {
        ModelObject mainNode = NetworkModel.instance().findRoomByName(startRoom);
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

            for (int i = 0; i < edgeCollection.size() / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);
            }
        }



        return  convertToSecondOrderProbabilities(localGraph);
    }

    private HashMap<String, HashMap<String, Double>> convertToSecondOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
        HashMap<String, HashMap<String, Integer>> nodeToNodeTravelFrequency = new HashMap<String, HashMap<String, Integer>>();

        for (ModelEdge edge : localGraph.getEdges()) {
            String source = localGraph.getSource(edge).toString();
            String destination = localGraph.getDest(edge).toString();

            if (!nodeToNodeTravelFrequency.containsKey(source)) {
                nodeToNodeTravelFrequency.put(source, new HashMap<String, Integer>());
            }
            if (!nodeToNodeTravelFrequency.get(source).containsKey(destination)) {
                nodeToNodeTravelFrequency.get(source).put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
            nodeToNodeTravelFrequency.get(source).put(destination, currentValue + 1);

        }

        HashMap<String, HashMap<String, Double>> nodeToNodeProbabilities = new HashMap<String, HashMap<String, Double>>();

        for (String source : nodeToNodeTravelFrequency.keySet()) {
            nodeToNodeProbabilities.put(source, new HashMap<String, Double>());
            double totalNumberOfOutEdges = 0.0;
            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {
                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source).get(dest);
            }
            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {

                nodeToNodeProbabilities.get(source).put(dest, (double) nodeToNodeTravelFrequency.get(source).get(dest) / totalNumberOfOutEdges);
            }


        }


       return nodeToNodeProbabilities;
    }

    private HashMap<String, Double> calculateFirstOrderProbForNeighbouringRooms(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room) {
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


            for (ModelEdge edge : tempGraph.getOutEdges(mainNode)) {

                localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
            }

        }


        return convertToFirstOrderProbabilities(localGraph, mainNode);
    }

    private HashMap<String, Double> convertToFirstOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, ModelObject source) {
        HashMap<String, Integer> nodeToNodeTravelFrequency = new HashMap<String, Integer>();

        for (ModelEdge edge : localGraph.getEdges()) {

            String destination = localGraph.getDest(edge).toString();


            if (!nodeToNodeTravelFrequency.containsKey(destination)) {
                nodeToNodeTravelFrequency.put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(destination).intValue();
            nodeToNodeTravelFrequency.put(destination, currentValue + 1);

        }

        HashMap<String, Double> nodeToNodeProbabilities = new HashMap<String, Double>();


        double totalNumberOfOutEdges = 0.0;
        for (String destination : nodeToNodeTravelFrequency.keySet()) {
            totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(destination);
        }
        for (String destination : nodeToNodeTravelFrequency.keySet()) {

            nodeToNodeProbabilities.put(destination,
                    (double) nodeToNodeTravelFrequency.get(destination) / totalNumberOfOutEdges);
        }


        return nodeToNodeProbabilities;
    }

    private void createCurrentTitle(String room, int pathLength, HashSet<Phase> selectedPhases) {

        currentTitle = room + ": length=" + pathLength + ":";

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


    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRelevantGraphToNameMap(
            final Collection<String> dataNames, final HashSet<Phase> selectedPhases) throws ExecutionException, InterruptedException {

        setEnabled(false);
        SwingWorker<HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> tempWorker = new SwingWorker<HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>() {
            private JProgressBar progressBar;
            private JTextArea taskOutput;
            private JFrame progressFrame;

            @Override
            protected HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {
                createProgressBar();

                HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> result = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                final int size = dataNames.size();
                int i = 1;
                for (String dataName : dataNames) {
                    final String tempDataName = dataName;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            taskOutput.append("Processing " + tempDataName + "...\n");
                        }
                    });
                    synchronized (NetworkModel.instance()) {
                        result.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, selectedPhases));
                    }
                    final int currentProgress = i;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setValue((currentProgress * 100) / size);
                        }
                    });
                    i++;
                }
                return result;
            }

            @Override
            protected void done() {
                Toolkit.getDefaultToolkit().beep();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressFrame.dispose();
                        taskOutput.append("Done.");
                    }
                });
                setEnabled(true);
            }

            private void createProgressBar() {
                SwingUtilities.invokeLater(new Runnable() {


                    @Override
                    public void run() {
                        progressBar = new JProgressBar(0, 100);
                        progressBar.setValue(0);
                        progressBar.setStringPainted(true);


                        taskOutput = new JTextArea(5, 20);
                        taskOutput.setMargin(new Insets(5, 5, 5, 5));
                        taskOutput.setEditable(false);
                        DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
                        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                        progressFrame = new JFrame("Generating data");
                        progressFrame.setLayout(new BorderLayout());
                        progressFrame.add(progressBar, BorderLayout.NORTH);
                        progressFrame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
                        progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        double width = screenSize.getWidth();
                        double height = screenSize.getHeight();
                        progressFrame.setLocation((int) Math.floor(width / 2 - 200), (int) Math.floor(height / 2 - 100));
                        progressFrame.setSize(400, 200);
                        progressFrame.setVisible(true);
                    }
                });
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


    private void initializeRoomList() {

        synchronized (roomList) {
            roomList = new ArrayList<String>(NetworkModel.instance().getSortedRooms());
        }

    }


    private class RoomDataListener implements ActionListener {


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

                    int result = jfc.showSaveDialog(PathPredictionFrame.this);
                    if (result == JFileChooser.CANCEL_OPTION)
                        return;
                    File file = jfc.getSelectedFile();


                    writeToDisk(file);
                } else {

                    JOptionPane.showMessageDialog(PathPredictionFrame.this,
                            "No graph to save", "Export error", JOptionPane.ERROR_MESSAGE);
                }

            }

        }
    }

    public void writeToDisk(File file) {
        System.out.println("Not implemented yet!");
    }

}
