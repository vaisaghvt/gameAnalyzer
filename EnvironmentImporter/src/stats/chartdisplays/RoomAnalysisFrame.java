package stats.chartdisplays;
import database.Database;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.util.SelfLoopEdgePredicate;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import gui.NetworkModel;
import gui.Phase;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.NotPredicate;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
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
    private JPanel dataPanel;
    private JPanel chartDisplayPanel = new JPanel();
    private Collection<JFrame> poppedOutFrames = new HashSet<JFrame>();

    private JCheckBox explorationCheckBox = new JCheckBox("Exploration");
    private JCheckBox task1CheckBox = new JCheckBox("Task 1");
    private JCheckBox task2CheckBox = new JCheckBox("Task 2");
    private JCheckBox task3CheckBox = new JCheckBox("Task 3");
    private JButton closeButton = new JButton("Close");
    private JButton generateButton = new JButton("Generate");
    private JComboBox<DisplayType> displayTypeJComboBox;
    private JComboBox<String> roomButtonComboBox;
    private Collection<String> roomList = new ArrayList<String>();
    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> data;
    private JButton popOutButton = new JButton("Pop-out");
    private Collection<String> dataNameList;

    private ActionListener roomDataListener;
    private VisualizationViewer<ModelObject, ? extends ModelEdge> currentVisualizationViewer;

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
                setLocation(100,100);
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
        popOutButton.addActionListener(roomDataListener);
        panel.add(chartDisplayPanel, BorderLayout.CENTER);
        panel.add(popOutButton, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createButtonPanel() {


        generateButton.addActionListener(roomDataListener);
        closeButton.addActionListener(roomDataListener);
        displayTypeJComboBox = new JComboBox<DisplayType>();
        displayTypeJComboBox.addItem(DisplayType.PATH_PROBABILITIES);
        displayTypeJComboBox.addItem(DisplayType.SIMPLE_COMPLETE_DIRECTED_GRAPH);
        displayTypeJComboBox.addItem(DisplayType.STAT_DISPLAY);
        displayTypeJComboBox.setEditable(false);


        JPanel buttonPanel = new JPanel(new GridLayout(7, 2));

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
            final DisplayType type = displayTypeJComboBox.getItemAt(displayTypeJComboBox.getSelectedIndex());
            final String room = roomButtonComboBox.getItemAt(roomButtonComboBox.getSelectedIndex());

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

                    HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap = generateRelevantGraphToNameMap(dataNameList, selectedPhases);
                    switch (type) {

                        case PATH_PROBABILITIES:
                            generateProbabilityStyleData(nameToGraphMap, room);
                            break;
                        case SIMPLE_COMPLETE_DIRECTED_GRAPH:

                            System.out.println("Calling simple complete directed graph");
                            generateSimpleNumberedStyleData(nameToGraphMap, room);
                            break;
                        case STAT_DISPLAY:
                            generateStats(nameToGraphMap, room);
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

    private void generateStats(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room) {


    }

    private void generateSimpleNumberedStyleData(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room) {

        ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for(String name: nameToGraphMap.keySet()){
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

            if(!tempGraph.containsVertex(mainNode)){
                continue;
            }

            Collection<ModelEdge> edgeCollection = tempGraph.getInEdges(mainNode);

            for(ModelEdge edge: edgeCollection){

                localGraph.addEdge(new ModelEdge(), tempGraph.getOpposite(mainNode, edge),
                        mainNode);

            }


            edgeCollection = tempGraph.getOutEdges(mainNode);

            for(ModelEdge edge: edgeCollection){

                localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
            }

        }



        final DirectedSparseMultigraph<ModelObject, ModelEdge> graphToDraw = localGraph;
        renderGraphPanel(graphToDraw, mainNode, DisplayType.SIMPLE_COMPLETE_DIRECTED_GRAPH);


    }


    private void generateProbabilityStyleData(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, String room) {
        ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

//        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for(String name: nameToGraphMap.keySet()){
            DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = nameToGraphMap.get(name);

            if(!tempGraph.containsVertex(mainNode)){
                continue;
            }

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime()-o2.getTime());
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

            if(lonelyEdge!=null){

            }



            for (int i = 0; i < edgeCollection.size() / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);


            }






        }


        HashMap<ModelObject, HashMap<ModelObject, Integer>> nodeToNodeTravelFrequency = new HashMap<ModelObject, HashMap<ModelObject, Integer>>();

        for(ModelEdge edge : localGraph.getEdges()){
            ModelObject source = localGraph.getSource(edge);
            ModelObject destination = localGraph.getDest(edge);
            if(source.toString().equals(destination.toString())){
                System.out.println("Cycle detected");
            }

            if(!nodeToNodeTravelFrequency.containsKey(source)){
                nodeToNodeTravelFrequency.put(source, new HashMap<ModelObject, Integer>());
            }
            if(!nodeToNodeTravelFrequency.get(source).containsKey(destination)){
                nodeToNodeTravelFrequency.get(source).put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
            nodeToNodeTravelFrequency.get(source).put(destination, currentValue+1);

        }

        HashMap<ModelObject, HashMap<ModelObject, Double>> nodeToNodeProbabilities = new HashMap<ModelObject, HashMap<ModelObject, Double>>();

        for(ModelObject source : nodeToNodeTravelFrequency.keySet()){
            nodeToNodeProbabilities.put(source, new HashMap<ModelObject, Double>());
            double totalNumberOfOutEdges =0.0;
            for(ModelObject dest: nodeToNodeTravelFrequency.get(source).keySet()){
                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source).get(dest);
            }
            for(ModelObject dest: nodeToNodeTravelFrequency.get(source).keySet()){

                nodeToNodeProbabilities.get(source).put(dest, (double)nodeToNodeTravelFrequency.get(source).get(dest)/totalNumberOfOutEdges);
            }


        }


        DirectedSparseMultigraph<ModelObject, ProbabilityEdge> summarizedGraph = new DirectedSparseMultigraph<ModelObject, ProbabilityEdge>();
        for(ModelObject vertex: localGraph.getVertices()){
            summarizedGraph.addVertex(vertex);
        }
        for(ModelObject source : nodeToNodeProbabilities.keySet()){

            for(ModelObject dest: nodeToNodeProbabilities.get(source).keySet()){
                if(source.toString().equals(dest.toString())){
                    System.out.println("Cycle");
                }

                summarizedGraph.addEdge(new ProbabilityEdge(nodeToNodeProbabilities.get(source).get(dest)),source, dest);

            }


        }

        final DirectedSparseMultigraph<ModelObject, ProbabilityEdge> graphToDraw = summarizedGraph;
        renderGraphPanel(graphToDraw, mainNode, DisplayType.PATH_PROBABILITIES);

    }

    private <T extends ModelEdge> void renderGraphPanel(final DirectedSparseMultigraph<ModelObject, T> graphToDraw, final ModelObject mainNode, final DisplayType simpleNumberingStyle) {
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


                currentVisualizationViewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());



                chartDisplayPanel.add(currentVisualizationViewer);
                currentVisualizationViewer.revalidate();
                chartDisplayPanel.getRootPane().revalidate();

            }


        });
    }


    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRelevantGraphToNameMap(final Collection<String> dataNames, final HashSet<Phase> selectedPhases) throws ExecutionException, InterruptedException {

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
                    synchronized (NetworkModel.instance()){
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


    public enum DisplayType {
        PATH_PROBABILITIES,
        SIMPLE_COMPLETE_DIRECTED_GRAPH,
        STAT_DISPLAY;
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
                                poppedOutFrames.add(poppedOutFrame);

                            }
                        });
            } else if (event.getSource() == generateButton) {
                updateChartPanel();
            }

        }
    }

    private class ProbabilityEdge extends ModelEdge {
        private final Double value;

        public ProbabilityEdge(Double value) {
            this.value = value;
        }

        @Override
        public String toString(){
            return new DecimalFormat("#.000").format(value);
        }
    }


}
