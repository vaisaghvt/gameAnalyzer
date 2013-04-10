package stats.chartdisplays;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 19/3/13
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathPredictionFrame extends JFrame {

    private static final int PATH_LENGTH_ALLOWED = 300;
    private JTextArea resultArea;

    public enum PathDataType {
        DESTINATION_PROBABILITIES, COVERAGE_COMPARISON, HOP_COMPARISON
    }

    private JPanel dataPanel;
    private JPanel chartDisplayPanel = new JPanel();
    private Collection<JFrame> poppedOutFrames = new HashSet<JFrame>();


    private JButton closeButton = new JButton("Close");
    private JButton generateButton = new JButton("Generate");
    private JComboBox<Integer> pathLengthComboBox;
    private JComboBox<String> roomButtonComboBox;
    private Collection<String> roomList = new ArrayList<String>();
    private Collection<String> dataNameList;

    private ActionListener roomDataListener;
    private String currentTitle;
    private JComboBox<PathDataType> typeComboBox;

    public PathPredictionFrame(Collection<String> dataNames) {
        roomDataListener = new RoomDataListener();
        this.dataNameList = dataNames;
        initializeRoomList();


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                dataPanel = createPathPredictionFrame();

                setTitle(getTitle());
                setContentPane(dataPanel);
                setVisible(true);
                setSize(new Dimension(1210, 800));

                setLocation(100, 100);

                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
        });
    }


    private JPanel createPathPredictionFrame() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = createButtonPanel();

        JPanel panel = createResultPanel();
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createResultPanel() {
        resultArea = new JTextArea();
        resultArea.setMargin(new Insets(5, 5, 5, 5));
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) resultArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JPanel pane = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(resultArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        pane.add(scrollPane, BorderLayout.CENTER);
        return pane;
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
                                StringBuilder outputToWrite = new StringBuilder();
                                NumberFormat doubleFormat = new DecimalFormat("00.000");

                                Ordering<String> valueComparator = Ordering.natural().onResultOf(Functions.forMap(result)).reverse();

                                ImmutableSortedMap<String, Double> resultMap = ImmutableSortedMap.copyOf(result, valueComparator);

                                for (String roomName : resultMap.keySet()) {
                                    if (resultMap.get(roomName) >= 0.001)
                                        outputToWrite.append(String.format("%-15s : %s %%,  ", roomName, doubleFormat.format(resultMap.get(roomName) * 100)));
                                    total += result.get(roomName);
                                }
                                System.out.println("total probabilities=" + total);
                                final String resultText = outputToWrite.toString();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(room + ": path length =" + pathLength);
                                        resultArea.setText(" Destination probabilities\n");
                                        resultArea.append(resultText);
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
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(room + ": path length =" + pathLength);
                                        NumberFormat doubleFormat = new DecimalFormat("##.000");

                                        resultArea.setText(" Coverage comparison \n");
                                        resultArea.append("Coverage Of RandomWalk = " + doubleFormat.format(result.get("random")) + "%\n");
                                        resultArea.append("Coverage Of Data = " + doubleFormat.format(result.get("data")) + "%\n");

                                        validate();
                                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                    }
                                });
                            }

                        };

                        break;
                    case HOP_COMPARISON:
                        tempWorker = new SwingWorker<Void, Void>() {
                            public HashMap<String, HashMap<String, Double>> result;

                            @Override
                            protected Void doInBackground() throws Exception {
                                result = compareHopRequirement(nameToGraphMap, pathLength, room);
                                return null;
                            }

                            protected void done() {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(room + ": path length =" + pathLength);
                                        NumberFormat doubleFormat = new DecimalFormat("##.000");


                                        resultArea.setText(" Hop requirement comparison \n");
                                        resultArea.append("Coverage Of RandomWalk = " + doubleFormat.format(result.get("random").get("mean")) + "\u00B1" + result.get("random").get("sd") + "\n");
                                        resultArea.append("Coverage Of Human = " + doubleFormat.format(result.get("data").get("mean"))+ "\u00B1"+result.get("data").get("sd")+"\n");

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

    private HashMap<String, HashMap<String, Double>> compareHopRequirement(final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap,final int pathLength, final String startingRoom) {
        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();

        final Semaphore coverageAreaSemaphore = new Semaphore(2);
        try {
            coverageAreaSemaphore.acquire(2);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        SwingWorker<HashMap<String, Double>, Void> randomWalkCoverageCalculator = new SwingWorker<HashMap<String, Double>, Void>() {
            @Override
            protected HashMap<String, Double> doInBackground() throws Exception {

                return RandomWalkOrganizer.getHopsRequiredForRandomWalks(startingRoom, coverageAreaSemaphore);
            }
        };
        SwingWorker<HashMap<String, Double>, Void> dataBasedCoverageGenerator = new SwingWorker<HashMap<String, Double>, Void>() {
            @Override
            protected HashMap<String, Double> doInBackground() throws Exception {
                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                for (String name : nameToGraphMap.keySet()) {
                    graphCollection.add(nameToGraphMap.get(name));
                }
                return getHopsRequiredForHumans(graphCollection, startingRoom, coverageAreaSemaphore);

            }
        };

        dataBasedCoverageGenerator.execute();
        randomWalkCoverageCalculator.execute();

        try {
            coverageAreaSemaphore.tryAcquire(2, 300, TimeUnit.SECONDS);
            result.put("random", randomWalkCoverageCalculator.get());

            result.put("data", dataBasedCoverageGenerator.get());
        } catch (InterruptedException e) {
            e.printStackTrace();

        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return result;
    }


    private HashMap<String, Double> compareCoverage(
            final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap,
            final int pathLength, final String startingRoom) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        final Semaphore coverageAreaSemaphore = new Semaphore(2);
        try {
            coverageAreaSemaphore.acquire(2);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        SwingWorker<Double, Void> randomWalkCoverageCalculator = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {

                return RandomWalkOrganizer.getCoverageOfRandomWalks(pathLength, startingRoom, coverageAreaSemaphore) * 100;
            }
        };
        SwingWorker<Double, Void> dataBasedCoverageGenerator = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {
                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection = new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                for (String name : nameToGraphMap.keySet()) {
                    graphCollection.add(nameToGraphMap.get(name));
                }
                return getCoverageOfData(graphCollection, pathLength, startingRoom, coverageAreaSemaphore) * 100;

            }
        };

        dataBasedCoverageGenerator.execute();
        randomWalkCoverageCalculator.execute();

        try {
            coverageAreaSemaphore.tryAcquire(2, 300, TimeUnit.SECONDS);
            result.put("random", randomWalkCoverageCalculator.get());

            result.put("data", dataBasedCoverageGenerator.get());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return result;
    }

    private HashMap<String, Double> getHopsRequiredForHumans(final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection,
                                                             final String startingRoom, Semaphore coverageAreaSemaphore) {
        final Semaphore semaphore = new Semaphore(2);
        final Semaphore mutex = new Semaphore(1);
        try {
            mutex.acquire();
            semaphore.acquire(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingWorker<HashMap<String, Double>, Void> tempWorker = new SwingWorker<HashMap<String, Double>, Void>() {
            @Override
            protected HashMap<String, Double> doInBackground() throws Exception {


                SwingWorker<HashMap<String, HashMap<String, Double>>, Void> firstOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, Double>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, Double>> doInBackground() throws Exception {

                        return GraphUtilities.calculateFirstOrderProbabilities(graphCollection, semaphore);
                    }


                };


                SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void> secondOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, HashMap<String, Double>>> doInBackground() throws Exception {

                        return GraphUtilities.calculateSecondOrderProbabilities(graphCollection, semaphore);
                    }


                };

                firstOrderCalculator.execute();
                secondOrderCalculator.execute();


                System.out.println("Working..");
                semaphore.tryAcquire(2, 300, TimeUnit.SECONDS);
                System.out.println("Ready to getValue");
                HashMap<String, HashMap<String, Double>> firstOrderProbs = firstOrderCalculator.get();
                HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbs = secondOrderCalculator.get();
                System.out.println("Received");

                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> pathCollections = GraphUtilities.generatePathsTillCoverage(firstOrderProbs, secondOrderProbs, startingRoom, RandomWalkOrganizer.COVERAGE_REQUIRED);
                HashMap<String, Double> result = GraphUtilities.calculateNumberOfHops(pathCollections);

                mutex.release();

                return result;
            }


        };
        tempWorker.execute();

        try {
            mutex.tryAcquire(1, 300, TimeUnit.SECONDS);
            coverageAreaSemaphore.release();
            return tempWorker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }

    }


    private Double getCoverageOfData(
            final Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection,
            final int pathLength, final String startingRoom, Semaphore coverageAreaSemaphore) {
        final Semaphore semaphore = new Semaphore(2);
        final Semaphore mutex = new Semaphore(1);
        try {
            mutex.acquire();
            semaphore.acquire(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingWorker<Double, Void> tempWorker = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {


                SwingWorker<HashMap<String, HashMap<String, Double>>, Void> firstOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, Double>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, Double>> doInBackground() throws Exception {

                        return GraphUtilities.calculateFirstOrderProbabilities(graphCollection, semaphore);
                    }


                };


                SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void> secondOrderCalculator = new SwingWorker<HashMap<String, HashMap<String, HashMap<String, Double>>>, Void>() {
                    @Override
                    protected HashMap<String, HashMap<String, HashMap<String, Double>>> doInBackground() throws Exception {

                        return GraphUtilities.calculateSecondOrderProbabilities(graphCollection, semaphore);
                    }


                };

                firstOrderCalculator.execute();
                secondOrderCalculator.execute();


                System.out.println("Working..");
                semaphore.tryAcquire(2, 300, TimeUnit.SECONDS);
                System.out.println("Ready to getValue");
                HashMap<String, HashMap<String, Double>> firstOrderProbs = firstOrderCalculator.get();
                HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbs = secondOrderCalculator.get();
                System.out.println("Received");

                Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> pathCollections = GraphUtilities.generatePaths(firstOrderProbs, secondOrderProbs, startingRoom, pathLength);
                Double result = GraphUtilities.calculateAverageCoverage(pathCollections);

                mutex.release();

                return result;
            }


        };
        tempWorker.execute();

        try {

            mutex.tryAcquire(1, 300, TimeUnit.SECONDS);
            coverageAreaSemaphore.release();
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
//            results.putValue(room, firstOrderProbabilities.getValue(room));
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
        return GraphUtilities.calculateFirstOrderProbabilities(graphCollection, new Semaphore(1));
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
        return GraphUtilities.calculateSecondOrderProbabilities(graphCollection, new Semaphore(1));
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

            } else if (event.getSource() == generateButton) {
                updateChartPanel();
            }

        }
    }

}
