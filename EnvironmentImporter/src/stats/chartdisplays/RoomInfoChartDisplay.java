package stats.chartdisplays;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoomInfoChartDisplay extends ChartDisplay<Collection<String>> implements ActionListener {

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
    private static JProgressBar progressBar;
    private static JTextArea taskOutput;
    private static JFrame progressFrame;

    @Override
    public void display(Collection<String> dataNames) {

        this.dataNameList = dataNames;
        initializeRoomList();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                dataPanel = createRoomAnalysisPanel();
                createNewFrameAndSetLocation();
                currentFrame.setTitle(getTitle());
                currentFrame.setContentPane(dataPanel);
                currentFrame.setVisible(true);
                currentFrame.setSize(new Dimension(520, 300));
                currentFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
        updateChartPanel();
        popOutButton.addActionListener(this);
        panel.add(chartDisplayPanel, BorderLayout.CENTER);
        panel.add(popOutButton, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createButtonPanel() {


        generateButton.addActionListener(this);
        closeButton.addActionListener(this);
        displayTypeJComboBox = new JComboBox<DisplayType>();
        displayTypeJComboBox.addItem(DisplayType.PROBABILITY_STYLE);
        displayTypeJComboBox.addItem(DisplayType.SIMPLE_NUMBERING_STYLE);
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
                            currentFrame.dispose();
                        }
                    });

        } else if (event.getSource() == popOutButton) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            JFrame poppedOutFrame = new JFrame();

                            poppedOutFrame.add(chartDisplayPanel);
                            poppedOutFrame.setVisible(true);
                            poppedOutFrames.add(poppedOutFrame);
                        }
                    });
        } else if (event.getSource() == generateButton) {
            updateChartPanel();
        }

    }

    private void updateChartPanel() {
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
                    currentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            });
            SwingWorker<Void, Void> tempWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    switch (type) {

                        case PROBABILITY_STYLE:
                            generateProbabilityStyleData(dataNameList, selectedPhases, room);
                            break;
                        case SIMPLE_NUMBERING_STYLE:
                            generateSimpleNumberedStyleData(dataNameList, selectedPhases, room);
                            break;
                        case STAT_DISPLAY:
                            generateStats(dataNameList, selectedPhases, room);
                            break;
                    }
                    return null;
                }

                protected void done() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            currentFrame.setTitle(room + ":" + type);
                            chartDisplayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            currentFrame.validate();
                            currentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    });
                }

            };
            tempWorker.execute();

        }


    }

    private void generateStats(Collection<String> dataNameList, HashSet<Phase> selectedPhases, String room) {

    }

    private void generateSimpleNumberedStyleData(Collection<String> dataNameList, HashSet<Phase> selectedPhases, String room) {
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> nameToGraphMap = generateRelevantGraphToNameMap(dataNameList, selectedPhases);
    }

    private HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> generateRelevantGraphToNameMap(final Collection<String> dataNames, final HashSet<Phase> selectedPhases) {
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
            result.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, selectedPhases));
            final int currentProgress = i;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue((currentProgress * 100) / size);
                }
            });
            i++;
        }


        Toolkit.getDefaultToolkit().beep();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressFrame.dispose();
                taskOutput.append("Done.");
            }
        });


        return result;
    }

    private void generateProbabilityStyleData(Collection<String> dataNameList, HashSet<Phase> selectedPhases, String room) {

    }

    private static void createProgressBar() {
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
                progressFrame = new JFrame("Generating random walk data");
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
        PROBABILITY_STYLE,
        SIMPLE_NUMBERING_STYLE,
        STAT_DISPLAY;
    }

    private void initializeRoomList() {

        synchronized (roomList) {
            roomList = new HashSet<String>(NetworkModel.instance().getFloorDegreeSortedRooms());
        }

    }


}