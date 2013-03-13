package gui;

import database.Database;
import stats.StatisticChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatsDialog extends JFrame implements ActionListener {

    public enum AllOrOne {
        ALL, EACH;
    }

    public enum AggregationType {
        SUM, MEAN, MIN, MAX;
    }


    private ArrayList<JCheckBox> dataNameList;


    private final JPanel mainPanel = new JPanel();
    private final JScrollPane pane;

    private final JPanel buttonPanel = new JPanel();

    private final JPanel primaryButtonPanel = new JPanel();
    private final JButton bOk = new JButton("Generate");
    private final JButton bCancel = new JButton("Close");

    private final JPanel nameListButtonPanel = new JPanel();
    private final JButton bOneRandom = new JButton("Select One");
    private final JButton bNRandom = new JButton("Select Random");
    private final JButton bNone = new JButton("Clear All");
    private final JButton bAll = new JButton("Select All");
    private final JButton attemptOne = new JButton("Attempt 1");
    private final JButton attemptTwo = new JButton("Attempt 2");
    private final JButton attemptThree = new JButton("Attempt 3");
    private final JButton attemptHigher = new JButton("Attempt >3");

    private final JPanel statOptionPanel = new JPanel();
    private JComboBox<AllOrOne> allOrOneComboBox;
    private JComboBox<AggregationType> aggregationTypeComboBox;


    private JComboBox<StatisticChoice> statisticChoiceList;
    private JPanel topPanel = new JPanel();
    private JComboBox<Phase> phaseChoiceList;

    public StatsDialog() {


        this.dataNameList = new ArrayList<JCheckBox>();

        this.setTitle("Vertex Statistics Options");

        this.setLocation(400, 200);
        this.setSize(1000, 600);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.initialiseTopPanel();
        this.initializeMainPanel();
        this.initialisePrimaryButtonPanel();
        this.initialiseDataNameButtonPanel();
        this.initialiseStatOptionPanel();

        buttonPanel.setLayout(new GridLayout(3, 1));
        buttonPanel.add(nameListButtonPanel);
        buttonPanel.add(statOptionPanel);
        buttonPanel.add(primaryButtonPanel);


        pane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        pane.setVisible(true);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.NORTH);
        this.getContentPane().add(pane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.revalidate();

    }

    private void initialiseStatOptionPanel() {
        allOrOneComboBox = new JComboBox<AllOrOne>(AllOrOne.values());
        allOrOneComboBox.setSelectedIndex(0);
        allOrOneComboBox.addActionListener(this);

        aggregationTypeComboBox = new JComboBox<AggregationType>(AggregationType.values());
        aggregationTypeComboBox.setSelectedIndex(0);

        statOptionPanel.setLayout(new GridLayout(1, 4));
        statOptionPanel.add(new JLabel("  Together or Individual:"));
        statOptionPanel.add(allOrOneComboBox);
        statOptionPanel.add(new JLabel("  AggregationMethod:"));
        statOptionPanel.add(aggregationTypeComboBox);

        StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
        if (!choice.canAggregate()) {
            this.allOrOneComboBox.setEnabled(false);
            this.aggregationTypeComboBox.setEnabled(false);
        } else {
            this.allOrOneComboBox.setEnabled(true);
            if (allOrOneComboBox.getItemAt(allOrOneComboBox.getSelectedIndex()) == AllOrOne.ALL && choice.hasAggregationChoice()) {
                this.aggregationTypeComboBox.setEnabled(true);
            } else {
                this.aggregationTypeComboBox.setEnabled(false);
            }

        }

    }


    private void initializeMainPanel() {
        initialiseDataNameList();
        mainPanel.setLayout(new GridLayout((!dataNameList.isEmpty() ? dataNameList.size() / 3 : 1), 3));
//        mainPanel.setLayout(new FlowLayout());

        for (JCheckBox box : dataNameList) {
            mainPanel.add(box);
        }
    }

    private void initialisePrimaryButtonPanel() {


        bOk.addActionListener(this);
        bCancel.addActionListener(this);

        primaryButtonPanel.setLayout(new GridLayout(1, 2));
        primaryButtonPanel.add(bOk);
        primaryButtonPanel.add(bCancel);


    }

    private void initialiseDataNameButtonPanel() {

        bOneRandom.addActionListener(this);
        bNRandom.addActionListener(this);
        bAll.addActionListener(this);
        bNone.addActionListener(this);
        attemptOne.addActionListener(this);
        attemptTwo.addActionListener(this);
        attemptThree.addActionListener(this);
        attemptHigher.addActionListener(this);

        nameListButtonPanel.setLayout(new GridLayout(1, 7));
        nameListButtonPanel.add(bOneRandom);
        nameListButtonPanel.add(bNRandom);
        nameListButtonPanel.add(bAll);
        nameListButtonPanel.add(bNone);
        nameListButtonPanel.add(attemptOne);
        nameListButtonPanel.add(attemptTwo);
        nameListButtonPanel.add(attemptThree);
        nameListButtonPanel.add(attemptHigher);

    }

    private void initialiseTopPanel() {
        initializeStatisticChoiceBox();
        initializePhaseChoiceBox();
        topPanel.setLayout(new GridLayout(2, 2));
        topPanel.add(new JLabel("  Choose Statistic:"));
        topPanel.add(statisticChoiceList);
        topPanel.add(new JLabel("  Choose Phase:"));
        topPanel.add(phaseChoiceList);
    }

    private void initializePhaseChoiceBox() {


        StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
        this.phaseChoiceList = new JComboBox<Phase>();

        for (Phase phase : choice.getPhases()) {
            phaseChoiceList.addItem(phase);
        }


        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        if(!choice.getPhases().isEmpty()){
            phaseChoiceList.setSelectedIndex(0);
        }else{
            phaseChoiceList.setEnabled(false);
        }
        phaseChoiceList.addActionListener(this);
        phaseChoiceList.setEditable(false);
    }

    public void initialiseDataNameList() {
        List<String> dataNames = new ArrayList(Database.getInstance().getDataNames());

        Collections.sort(dataNames);
        this.dataNameList.clear();
        for (String name : dataNames) {
            JCheckBox cBox = new JCheckBox(name);
            cBox.setSelected(true);
            this.dataNameList.add(cBox);


        }
        JCheckBox randomWalkBox = new JCheckBox("random walk");
        randomWalkBox.setSelected(false);
        StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
        if (!choice.isRandomWalkMeasurePossible()) {
            randomWalkBox.setEnabled(false);
        }
        this.dataNameList.add(randomWalkBox);


    }


    private void initializeStatisticChoiceBox() {


        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        List<StatisticChoice> listOfChoices = Arrays.asList(StatisticChoice.values());
        Collections.sort(listOfChoices, new Comparator<StatisticChoice>() {
            @Override
            public int compare(StatisticChoice statisticChoice, StatisticChoice statisticChoice1) {
                return statisticChoice.toString().compareTo(statisticChoice1.toString());
            }
        });
        int size = listOfChoices.size();
        statisticChoiceList = new JComboBox();
        for (StatisticChoice choice : listOfChoices) {
            if (choice.getStatisticsHandler() != null) {
                statisticChoiceList.addItem(choice);

            }
        }


        statisticChoiceList.setSelectedIndex(0);
        statisticChoiceList.addActionListener(this);
        statisticChoiceList.setEditable(false);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == bOk) {
//            this.getStatistics();
//            this.setVisible(false);
//            this.dispose();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
                    Collection<String> dataNames = new HashSet<String>();
                    for (JCheckBox box : dataNameList) {
                        if (box.isSelected()) {
                            dataNames.add(box.getText());
                        }
                    }
                    if (choice.getStatisticsHandler() != null) {
                        int index = phaseChoiceList.getSelectedIndex();
                        Phase phase = null;
                        if (index != -1) {
                            phase = phaseChoiceList.getItemAt(index);
                        }
                        if (allOrOneComboBox.getItemAt(allOrOneComboBox.getSelectedIndex()) == AllOrOne.EACH) {
                            choice.getStatisticsHandler().generateAndDisplayStats(dataNames, phase, AllOrOne.EACH, null);
                        } else {
                            choice.getStatisticsHandler().generateAndDisplayStats(dataNames, phase, AllOrOne.ALL,
                                    aggregationTypeComboBox.getItemAt(aggregationTypeComboBox.getSelectedIndex()));
                        }

                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "ERROR: That analysis cannot be done.", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == allOrOneComboBox) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AllOrOne choice = allOrOneComboBox.getItemAt(allOrOneComboBox.getSelectedIndex());
                    aggregationTypeComboBox.removeAllItems();
                    if (choice == AllOrOne.ALL && statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex()).hasAggregationChoice()) {
                        aggregationTypeComboBox.setEnabled(true);
                        for (AggregationType type : AggregationType.values()) {
                            aggregationTypeComboBox.addItem(type);
                        }
                        aggregationTypeComboBox.setSelectedIndex(0);
                    } else {
                        aggregationTypeComboBox.setEnabled(false);
                    }
//
//                    this.validate();
                }
            });

        } else if (event.getSource() == statisticChoiceList) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
                    phaseChoiceList.removeAllItems();
                    for (Phase phase : choice.getPhases()) {
                        phaseChoiceList.addItem(phase);
                    }
                    if (choice.getPhases().size() > 0) {
                        phaseChoiceList.setEnabled(true);
                        phaseChoiceList.setSelectedIndex(0);
                    } else {
                        phaseChoiceList.setEnabled(false);
                    }

                    if (!choice.canAggregate()) {
                        allOrOneComboBox.setEnabled(false);
                        aggregationTypeComboBox.setEnabled(false);
                    } else {
                        allOrOneComboBox.setEnabled(true);
                        if (choice.hasAggregationChoice() && allOrOneComboBox.getItemAt(allOrOneComboBox.getSelectedIndex()) == AllOrOne.ALL) {
                            aggregationTypeComboBox.setEnabled(true);
                        } else {
                            aggregationTypeComboBox.setEnabled(false);
                        }

                    }

                    if (!choice.isRandomWalkMeasurePossible()) {
                        for (JCheckBox dataName : dataNameList) {
                            if (dataName.getText().equalsIgnoreCase("random walk")) {
                                dataName.setSelected(false);
                                dataName.setEnabled(false);
                            }
                        }
                    } else {
                        for (JCheckBox dataName : dataNameList) {
                            if (dataName.getText().equalsIgnoreCase("random walk")) {
                                dataName.setSelected(false);
                                dataName.setEnabled(true);
                            }
                        }
                    }
//
//                    this.validate();
                }
            });
        } else if (event.getSource() == bAll) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (!cBox.getText().equalsIgnoreCase("random walk")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });

        } else if (event.getSource() == attemptOne) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (cBox.getText().contains("1")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });

        } else if (event.getSource() == attemptTwo) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (cBox.getText().contains("2")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });

        } else if (event.getSource() == attemptThree) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (cBox.getText().contains("3")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });

        } else if (event.getSource() == attemptHigher) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (!cBox.getText().contains("1")
                                && !cBox.getText().contains("2")
                                && !cBox.getText().contains("3")
                                && !cBox.getText().equalsIgnoreCase("random walk")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });

        } else if (event.getSource() == bNone) {

            for (JCheckBox cBox : dataNameList) {
                cBox.setSelected(false);
            }

        } else if (event.getSource() == bOneRandom) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        cBox.setSelected(false);
                    }
                    int i = (int) Math.floor(Math.random() * dataNameList.size());
                    JCheckBox cBox = dataNameList.get(i);
                    cBox.setSelected(true);
                }
            });

        } else if (event.getSource() == bNRandom) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (JCheckBox cBox : dataNameList) {
                        if (Math.random() < 0.5) {
                            cBox.setSelected(false);
                        } else if (!cBox.getText().equalsIgnoreCase("random walk")) {
                            cBox.setSelected(true);
                        }
                    }
                }
            });
        }
    }


}
