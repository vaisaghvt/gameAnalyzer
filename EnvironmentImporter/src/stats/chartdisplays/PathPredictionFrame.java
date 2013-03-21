package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gui.NetworkModel;
import gui.Phase;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalk;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 19/3/13
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathPredictionFrame extends JFrame {

    private static final int PATH_LENGTH_ALLOWED = 300;

    public enum PathDataType {
        DESTINATION_PROBABILITIES, COVERAGE_COMPARISON
    }

    private JPanel dataPanel;
    private JPanel chartDisplayPanel = new JPanel();
    private Collection<JFrame> poppedOutFrames = new HashSet<JFrame>();


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
    private JComboBox<PathDataType> typeComboBox;

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
        for (int i = 1; i <= PATH_LENGTH_ALLOWED; i++) {
            pathLengthComboBox.addItem(new Integer(i));
        }
        pathLengthComboBox.setEditable(false);


        typeComboBox = new JComboBox<PathDataType>();
        for (PathDataType type : PathDataType.values()) {
            typeComboBox.addItem(type);
        }
        typeComboBox.setEditable(false);


        JPanel buttonPanel = new JPanel(new GridLayout(4, 2));

        buttonPanel.add(new JLabel("Choose Starting location :"));
        buttonPanel.add(getRoomButtonComboBox());

        buttonPanel.add(new JLabel("Select type:"));
        buttonPanel.add(typeComboBox);

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
        selectedPhases.add(Phase.EXPLORATION);

        final PathDataType type = typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
        final int pathLength = pathLengthComboBox.getItemAt(pathLengthComboBox.getSelectedIndex()).intValue();
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
                createCurrentTitle(room, pathLength);

                final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap = generateRelevantGraphToNameMap(dataNameList, selectedPhases);
                SwingWorker<Void, Void> tempWorker = null;

                switch (type) {

                    case DESTINATION_PROBABILITIES:
                        tempWorker = new SwingWorker<Void, Void>() {
                            public HashMap<String, Double> result;

                            @Override
                            protected Void doInBackground() throws Exception {
                                result = predictDestinations(nameToGraphMap, pathLength, room);
                                return null;
                            }

                            protected void done() {
                                double total = 0.0;
                                for (String roomName : result.keySet()) {
                                    System.out.println(roomName + ":" + result.get(roomName));
                                    total += result.get(roomName);
                                }
                                System.out.println("total probabilities=" + total);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(room + ": path length =" + pathLength);
                                        validate();
                                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                    }
                                });
                            }

                        };


                        break;
                    case COVERAGE_COMPARISON:
                        tempWorker = new SwingWorker<Void, Void>() {
                            public HashMap<String, Double> result;

                            @Override
                            protected Void doInBackground() throws Exception {
                                result = compareCoverage(nameToGraphMap, pathLength, room);
                                return null;
                            }

                            protected void done() {
                                System.out.println("Coverage Of RandomWalk = " + result.get("random"));
                                System.out.println("Coverage Of Data = " + result.get("data"));
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(room + ": path length =" + pathLength);
                                        validate();
                                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                    }
                                });
                            }

                        };

                        break;
                }

                tempWorker.execute();
                return null;
            }

        };
        tempWorker.execute();


    }

    private HashMap<String, Double> compareCoverage(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap,
            int pathLength, String startingRoom) {
        HashMap<String, Double> result = new HashMap<String, Double>();
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        for (String name : nameToGraphMap.keySet()) {
            graphCollection.add(nameToGraphMap.get(name));
        }

        result.put("random", RandomWalk.getCoverageOfRandomWalks(pathLength, startingRoom)*100);

        result.put("data", getCoverageOfData(graphCollection, pathLength, startingRoom)*100);
        return result;
    }

    private Double getCoverageOfData(
            final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection,
            final int pathLength, final String startingRoom) {


        SwingWorker<Double, Void> tempWorker = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {


                HashMap<String, HashMap<String, Double>> firstOrderProbs = GraphUtilities.calculateFirstOrderProbabilities(graphCollection);
                HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbs = GraphUtilities.calculateSecondOrderProbabilities(graphCollection);

                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> pathCollections = GraphUtilities.generatePaths(firstOrderProbs, secondOrderProbs, startingRoom, pathLength);
                return GraphUtilities.calculateAverageCoverage(pathCollections);
            }


        };
        tempWorker.execute();

        try {
            return tempWorker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }

    }

    private HashMap<String, Double> predictDestinations(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap, int pathLength, String startRoom) {


        HashMap<String, HashMap<String, Double>> firstOrderProbabilities = calculateFirstOrderProb(nameToGraphMap);


        if (pathLength == 1) {


            return firstOrderProbabilities.get(startRoom);
        }

        HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities =
                calculateSecondOrderProb(nameToGraphMap);


        HashMultimap<String, String> currentHistories = HashMultimap.create();


        for (String room : firstOrderProbabilities.get(startRoom).keySet()) {
            currentHistories.put(room, startRoom);
//            results.put(room, firstOrderProbabilities.get(room));
        }
        HashMap<String, HashMap<String, Double>> results = calculateFirstHop(firstOrderProbabilities.get(startRoom), currentHistories, firstOrderProbabilities, secondOrderProbabilities);


        for (int hops = 2; hops < pathLength; hops++) {
            addNextHop(results, currentHistories, firstOrderProbabilities, secondOrderProbabilities);
        }

        HashMap<String, Double> finalResult = new HashMap<String, Double>();
        for (String finalLocation : results.keySet()) {
            double value = 0.0;
            for (String penultimateLocation : results.get(finalLocation).keySet()) {
                value += results.get(finalLocation).get(penultimateLocation);
            }
            finalResult.put(finalLocation, value);
        }

        return finalResult;
    }

    private HashMap<String, HashMap<String, Double>> calculateFirstOrderProb(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        for (String name : nameToGraphMap.keySet()) {
            graphCollection.add(nameToGraphMap.get(name));
        }
        return GraphUtilities.calculateFirstOrderProbabilities(graphCollection);
    }

    private void addNextHop(HashMap<String, HashMap<String, Double>> results,
                            HashMultimap<String, String> currentHistories, HashMap<String, HashMap<String, Double>> firstOrderProbabilities, HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities) {

        HashMultimap<String, String> futureHistories = HashMultimap.create();
        HashMap<String, HashMap<String, Double>> newResults = new HashMap<String, HashMap<String, Double>>();

        for (String currentLocation : currentHistories.keySet()) {       // For each of the possible current locations


            HashMap<String, HashMap<String, Double>> currentLocationSecondOrderProbabilities = secondOrderProbabilities.get(currentLocation);
            // The probability of reaching a destination by going through that room.
            for (String prevLocation : currentHistories.get(currentLocation)) {
                double priorProbability = results.get(currentLocation).get(prevLocation);  // THe prabability of being in that current location.


                HashMap<String, Double> secondOrderProbabilitiesForConsideration = currentLocationSecondOrderProbabilities.containsKey(prevLocation) ?
                        currentLocationSecondOrderProbabilities.get(prevLocation) :
                        firstOrderProbabilities.get(currentLocation);
                // Given a previous location the probability of reaching a particular destination on passing through current location


                for (String possibleDestination : secondOrderProbabilitiesForConsideration.keySet()) {
                    if (!newResults.containsKey(possibleDestination)) {
                        newResults.put(possibleDestination, new HashMap<String, Double>());
                    }
                    HashMap<String, Double> newResultsForPossibleDestination = newResults.get(possibleDestination);


                    double secondOrderValue = secondOrderProbabilitiesForConsideration.get(possibleDestination).doubleValue();

                    double probabilityToWrite = Math.exp(Math.log(priorProbability) + Math.log(secondOrderValue));
                    if (newResultsForPossibleDestination.containsKey(currentLocation)) {
                        double newValue = newResultsForPossibleDestination.get(currentLocation) + probabilityToWrite;
                        newResultsForPossibleDestination.put(currentLocation, newValue);


                    } else {
                        newResultsForPossibleDestination.put(currentLocation, probabilityToWrite);
                    }


                    futureHistories.put(possibleDestination, currentLocation);
                    newResults.put(possibleDestination, newResultsForPossibleDestination);
                }
            }


        }

        currentHistories.clear();
        currentHistories.putAll(futureHistories);

        results.clear();
        results.putAll(newResults);

        if (!newResults.keySet().containsAll(futureHistories.keySet()) || !futureHistories.keySet().containsAll(newResults.keySet())) {
            System.out.println("Calculation Mistake.");
        }

    }

    private HashMap<String, HashMap<String, HashMap<String, Double>>> calculateSecondOrderProb(
            HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap) {
        Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        for (String name : nameToGraphMap.keySet()) {
            graphCollection.add(nameToGraphMap.get(name));
        }
        return GraphUtilities.calculateSecondOrderProbabilities(graphCollection);
    }

    private HashMap<String, HashMap<String, Double>> calculateFirstHop(HashMap<String, Double> results,
                                                                       HashMultimap<String, String> currentHistories,
                                                                       HashMap<String, HashMap<String, Double>> firstOrderProbabilities, HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities) {

        HashMultimap<String, String> futureHistories = HashMultimap.create();
        HashMap<String, HashMap<String, Double>> newResult = new HashMap<String, HashMap<String, Double>>();

        for (String currentLocation : currentHistories.keySet()) {       // For each of the possible current locations
            double priorProbability = results.get(currentLocation);  // THe prabability of being in that current location.

            HashMap<String, HashMap<String, Double>> currentLocationSecondOrderProbabilities = secondOrderProbabilities.get(currentLocation);
            // The probability of reaching a destination by going through that room.
            for (String prevLocation : currentHistories.get(currentLocation)) {
                HashMap<String, Double> secondOrderProbabilitiesForConsideration = currentLocationSecondOrderProbabilities.containsKey(prevLocation) ?
                        currentLocationSecondOrderProbabilities.get(prevLocation) :
                        firstOrderProbabilities.get(currentLocation);


                for (String possibleDestination : secondOrderProbabilitiesForConsideration.keySet()) {
                    if (!newResult.containsKey(possibleDestination)) {
                        newResult.put(possibleDestination, new HashMap<String, Double>());
                    }
                    HashMap<String, Double> newResultsForPossibleDestination = newResult.get(possibleDestination);
                    double secondOrderValue = secondOrderProbabilitiesForConsideration.get(possibleDestination).doubleValue();

                    double probabilityToWrite = Math.exp(Math.log(priorProbability) + Math.log(secondOrderValue));
                    if (newResultsForPossibleDestination.containsKey(currentLocation)) {
                        System.out.println("HOW???");


                    } else {
                        newResultsForPossibleDestination.put(currentLocation, probabilityToWrite);
                    }


                    newResult.put(possibleDestination, newResultsForPossibleDestination);
                    futureHistories.put(possibleDestination, currentLocation);
                }
            }


        }

        currentHistories.clear();
        currentHistories.putAll(futureHistories);


        if (!newResult.keySet().containsAll(futureHistories.keySet()) || !futureHistories.keySet().containsAll(newResult.keySet())) {
            System.out.println("Calculation Mistake.");
        }
        return newResult;
    }


    private void createCurrentTitle(String room, int pathLength) {

        currentTitle = room + ": length=" + pathLength + ":";

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


    public void writeToDisk(File file) {
        System.out.println("Not implemented yet!");
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
                    JFileChooser jfc = new JFileChooser(new File("."+File.separatorChar + "SavedGraphs"));
//                    jfc.addChoosableFileFilter(new PngFileFilter());

                    int result = jfc.showSaveDialog(PathPredictionFrame.this);
                    if (result == JFileChooser.CANCEL_OPTION)
                        return;
                    File file = jfc.getSelectedFile();


                    writeToDisk(file);
                }  else {

                    JOptionPane.showMessageDialog(PathPredictionFrame.this,"No graph to save","Export error", JOptionPane.ERROR_MESSAGE);
                }

            }

        }
    }

}
