package gui;

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
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/18/13
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexStatisticsDialog extends JDialog implements ActionListener {


    private Collection<JCheckBox> dataNameList;


    private final JPanel mainPanel = new JPanel();
    private final JScrollPane pane;

    private final JPanel buttonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");
    private JComboBox<StatisticChoice> statisticChoiceList;
    private JPanel topPanel = new JPanel();
    private JComboBox<Phase> phaseChoiceList;

    public VertexStatisticsDialog() {


        this.dataNameList = new HashSet<JCheckBox>();
        this.setTitle("Vertex Statistics Options");

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

        Phase[] phases = new Phase[]{Phase.EXPLORATION, Phase.TASK_1};


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
        StatisticChoice[] statisticChoices = {StatisticChoice.VERTEX_VISIT_FREQUENCY, StatisticChoice.VERTEX_VISIT_TIMES};

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
            this.getStatistics();


            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }


    }

    private void getStatistics() {

        StatisticChoice choice;
        if (this.statisticChoiceList.getSelectedItem() == StatisticChoice.VERTEX_VISIT_FREQUENCY) {
            choice = StatisticChoice.VERTEX_VISIT_FREQUENCY;
        } else if (this.statisticChoiceList.getSelectedItem() == StatisticChoice.TIME_SPENT_PER_VERTEX) {
            choice = StatisticChoice.TIME_SPENT_PER_VERTEX;
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
            HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
            for (String dataName : dataNames) {
                System.out.println("Processing " + dataName + "...");
                dataNameDataMap.put(dataName, ((NetworkModel) NetworkModel.instance()).getVertexDataFor(dataName, choice));

            }
            HashMap<String, HashMap<String, Long>> data = aggregateData(dataNameDataMap);
            displayChart(data);
            display(data);
        } else {
            System.out.println("Nothing selected!");
        }

    }

    private HashMap<String, HashMap<String, Long>> aggregateData(HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap) {
        HashMap<String, HashMap<String, Long>> result = new HashMap<String, HashMap<String, Long>>();
        Phase phase = (Phase) this.phaseChoiceList.getSelectedItem();
        assert phase != null;
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, HashMap<String, Number>> dataForPerson = dataNameDataMap.get(dataName);

            HashMap<String, Number> library2Data = dataForPerson.get("Library2");
            HashMap<String, Number> libraryGData = dataForPerson.get("LibraryG");
            HashMap<String, Number> saunaData = dataForPerson.get("Sauna");
            HashMap<String, Number> galleryData = dataForPerson.get("Gallery");
            HashMap<String, Number> startData = dataForPerson.get("StartingRoom");
            long valueForLibrary;
            if (library2Data != null) {
                long library2Value = library2Data.get(phase.toString()) == null ? 0 : (library2Data.get(phase.toString())).longValue() ;
                long libraryGValue = (libraryGData.get(phase.toString()) == null ? 0 : (libraryGData.get(phase.toString())).longValue());
                valueForLibrary = library2Value + libraryGValue;
            } else {
                valueForLibrary = 0;
            }
            long valueForSauna;
            if (saunaData != null)
                valueForSauna = saunaData.get(phase.toString()) == null ? 0 : (saunaData.get(phase.toString())).longValue();
            else
                valueForSauna = 0;
            long valueForGallery;
            if (galleryData != null) {
                valueForGallery = galleryData.get(phase.toString()) == null ? 0 : (galleryData.get(phase.toString())).longValue();
            } else {
                valueForGallery =0;
            }
            long valueForStart;
            if (startData != null) {
                valueForStart = startData.get(phase.toString()) == null ? 0 : startData.get(phase.toString()).longValue();
            } else {
                valueForStart =0;
            }
            HashMap<String, Long> tempMap = new HashMap<String, Long>();
            tempMap.put("Library", valueForLibrary);
            tempMap.put("Gallery", valueForGallery);
            tempMap.put("Sauna", valueForSauna);
            tempMap.put("StartingRoom", valueForStart);
            result.put(dataName, tempMap);
        }
        return result;
    }

    private void displayChart(HashMap<String, HashMap<String, Long>> data) {
        final CategoryDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        JFrame frame = new JFrame("Vertex Statistics");
        frame.setContentPane(chartPanel);
        frame.setVisible(true);
        frame.setSize(new Dimension(520, 300));
    }


    private JFreeChart createChart(CategoryDataset dataset) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                "Vertex Statistics",         // chart title
                "Room",               // domain axis label
                "Value",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );


        return chart;
    }

    private CategoryDataset createDataSet(HashMap<String, HashMap<String, Long>> data) {


        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (String dataName : data.keySet()) {
            HashMap<String, Long> dataValue = data.get(dataName);
            for (String room : dataValue.keySet()) {
                dataSet.addValue(dataValue.get(room), dataName, room);
            }
        }

        return dataSet;

    }

    private void display(HashMap<String, HashMap<String, Long>> data) {
        System.out.println("Not implemented");
    }

}
