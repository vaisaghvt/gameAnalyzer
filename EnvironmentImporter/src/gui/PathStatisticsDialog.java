package gui;

import com.google.common.collect.HashMultimap;
import database.Database;
import database.StatisticChoice;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/18/13
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathStatisticsDialog extends JDialog implements ActionListener {

    private Collection<JCheckBox> dataNameList;


    private final JPanel mainPanel = new JPanel();
    private final JScrollPane pane;

    private final JPanel buttonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");
    private JComboBox statisticChoiceList;
    private JPanel topPanel = new JPanel();
    private JComboBox<Phase> phaseChoiceList;

    public PathStatisticsDialog() {


        this.dataNameList = new HashSet<JCheckBox>();
        this.setTitle("Path Statistics Options");

        this.setSize(800, 200);


        this.initialiseTopPanel();
        this.initializeMainPanel();


        this.initialiseButtonPanel();
        pane = new JScrollPane(mainPanel);
        pane.setVisible(true);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.NORTH);
        this.getContentPane().add(pane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.revalidate();

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

        Phase[] phases = new Phase[]{Phase.TASK_2, Phase.TASK_3};


        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        phaseChoiceList = new JComboBox(phases);
        phaseChoiceList.setSelectedIndex(0);
        phaseChoiceList.addActionListener(this);
        phaseChoiceList.setEditable(false);
    }
    private void initialiseButtonPanel() {
        bOk.setPreferredSize(new Dimension(100, 20));
        bCancel.setPreferredSize(new Dimension(100, 20));

        bOk.addActionListener(this);
        bCancel.addActionListener(this);

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(bOk);
        buttonPanel.add(bCancel);
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

    private void initializeMainPanel() {
        initialiseDataNameList();
        mainPanel.setLayout(new GridLayout((!dataNameList.isEmpty() ? dataNameList.size() : 1), 3));
        for (JCheckBox box : dataNameList) {
            mainPanel.add(box);
        }
    }

    private void initializeStatisticChoiceBox() {
        String[] statisticChoices = {"Path Frequency"};

        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        statisticChoiceList = new JComboBox(statisticChoices);
        statisticChoiceList.setSelectedIndex(0);
        statisticChoiceList.addActionListener(this);
        statisticChoiceList.setEditable(false);

    }

    @Override
    public void actionPerformed(ActionEvent event) {


        if (event.getSource() == bOk) {
            this.getStatistics((Phase) phaseChoiceList.getSelectedItem(), (String) statisticChoiceList.getSelectedItem());


            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }


    }

    private void getStatistics(Phase phase, String statisticsChoice) {

        StatisticChoice choice;
        if (statisticsChoice.equals("Path Frequency")) {
            choice = StatisticChoice.PATH_FREQUENCY;
        } else {
            choice = null;
            assert false;
        }
        Collection<String> dataNames = new HashSet<String>();
        for (JCheckBox box : dataNameList) {
            if (box.isSelected()) {
                dataNames.add(box.getText());
            }
        }

        if (!dataNames.isEmpty()) {

            displayChart(((NetworkModel) NetworkModel.instance()).getPathDataFor(dataNames, phase, choice), phase);
        } else {
            System.out.println("No data to display!!");
        }

    }

    private void displayChart(HashMultimap<String, String> data, Phase phase) {
        final CategoryDataset dataSet = createDataSet(data, phase);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        JFrame frame = new JFrame("Path Statistics");
        frame.setContentPane(chartPanel);
        frame.setVisible(true);
        frame.setSize(new Dimension(520,300));
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                "Path Statistics",         // chart title
                "",               // domain axis label
                "Number",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );



        return chart;
    }

    private CategoryDataset createDataSet(HashMultimap<String, String> data, Phase phase) {
        if (phase == Phase.TASK_2) {
            final String thirdFloor = "3rd Floor";
            final String secondFloor = "2nd Floor";
            final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

            for (String path : data.keySet()) {
                String category = path.substring(0,1) .equals("2")?secondFloor:thirdFloor;
                dataSet.addValue(data.get(path).size(), path, category);
            }

            return dataSet;
        } else if(phase == Phase.TASK_3){
            final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
            for (String path : data.keySet()) {

                dataSet.addValue(data.get(path).size(), path, "default");
            }
            return dataSet;

        } else{
            return null;
        }
    }








}
