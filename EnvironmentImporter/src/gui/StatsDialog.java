package gui;

import database.Database;
import stats.StatisticChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatsDialog extends JDialog implements ActionListener {

    private ArrayList<JCheckBox> dataNameList;


    private final JPanel mainPanel = new JPanel();
    private final JScrollPane pane;

    private final JPanel buttonPanel = new JPanel();

    private final JPanel primaryButtonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");

    private final JPanel nameListButtonPanel = new JPanel();
    private final JButton bOneRandom = new JButton("Select 1");
    private final JButton bNRandom = new JButton("Select Random");
    private final JButton bNone = new JButton("Clear All");
    private final JButton bAll = new JButton("Select All");


    private JComboBox<StatisticChoice> statisticChoiceList;
    private JPanel topPanel = new JPanel();
    private JComboBox<Phase> phaseChoiceList;

    public StatsDialog() {


        this.dataNameList = new ArrayList<JCheckBox>();
        this.setTitle("Vertex Statistics Options");

        this.setLocation(400, 200);
        this.setSize(800, 600);


        this.initialiseTopPanel();
        this.initializeMainPanel();
        this.initialisePrimaryButtonPanel();
        this.initialiseDataNameButtonPanel();

        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.add(nameListButtonPanel);
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

        nameListButtonPanel.setLayout(new GridLayout(1, 4));
        nameListButtonPanel.add(bOneRandom);
        nameListButtonPanel.add(bNRandom);
        nameListButtonPanel.add(bAll);
        nameListButtonPanel.add(bNone);


    }

    private void initialiseTopPanel() {
        initializeStatisticChoiceBox();
        initializePhaseChoiceBox();
        topPanel.setLayout(new GridLayout(2, 2));
        topPanel.add(new JLabel("Choose Statistic:"));
        topPanel.add(statisticChoiceList);
        topPanel.add(new JLabel("Choose Phase:"));
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
        phaseChoiceList.setSelectedIndex(0);
        phaseChoiceList.addActionListener(this);
        phaseChoiceList.setEditable(false);
    }

    public void initialiseDataNameList() {
        Collection<String> dataNames = Database.getInstance().getDataNames();

        this.dataNameList.clear();
        for (String name : dataNames) {
            JCheckBox cBox = new JCheckBox(name);
            cBox.setSelected(true);
            this.dataNameList.add(cBox);


        }

    }


    private void initializeStatisticChoiceBox() {


        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        statisticChoiceList = new JComboBox(StatisticChoice.values());
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
                if(index!=-1){
                    phase = phaseChoiceList.getItemAt(index);
                }
                choice.getStatisticsHandler().generateAndDisplayStats(dataNames, phase);
            } else {
                JOptionPane.showMessageDialog(this, "ERROR: That analysis cannot be done.", "An Error Occured", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == statisticChoiceList) {
            StatisticChoice choice = statisticChoiceList.getItemAt(statisticChoiceList.getSelectedIndex());
            this.phaseChoiceList.removeAllItems();
            for (Phase phase : choice.getPhases()) {
                phaseChoiceList.addItem(phase);
            }
            if (choice.getPhases().size() > 0) {
                phaseChoiceList.setSelectedIndex(0);
            }
            this.validate();
        } else if (event.getSource() == bAll) {

            for (JCheckBox cBox : dataNameList) {
                cBox.setSelected(true);
            }

        } else if (event.getSource() == bNone) {

            for (JCheckBox cBox : dataNameList) {
                cBox.setSelected(false);
            }

        } else if (event.getSource() == bOneRandom) {


            for (JCheckBox cBox : dataNameList) {
                cBox.setSelected(false);
            }
            int i = (int) Math.floor(Math.random() * dataNameList.size());
            JCheckBox cBox = this.dataNameList.get(i);
            cBox.setSelected(true);

        } else if (event.getSource() == bNRandom) {


            for (JCheckBox cBox : dataNameList) {
                if (Math.random() < 0.5)
                    cBox.setSelected(false);
                else
                    cBox.setSelected(true);
            }
        }
    }


}
