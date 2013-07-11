package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.GraphUtilities;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import stats.chartdisplays.PathChartDisplay;
import stats.consoledisplays.PathConsoleDisplay;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class HopAndCoverageCalculator extends StatisticsHandler<PathConsoleDisplay,
        PathChartDisplay> implements ActionListener {

    int numberOfHops =300;
    double coverageRequired =50;

    JButton calculateCoverage = new JButton("Calculate Coverage");
    JButton calculateHops = new JButton("Calculate Hops");
    private Collection<String> dataNames;


    public HopAndCoverageCalculator() {
        super(null,null);

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
//
//        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames);
//        super.actualGenerateAndDisplay(task);

        this.dataNames = dataNames;
        new FindTypeFrame();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == calculateCoverage){
//            System.out.println(numberOfHops);
            CoverageTask task = new CoverageTask(dataNames, numberOfHops);
            super.actualGenerateAndDisplay(task);

        }else if (e.getSource() == calculateHops){
//            System.out.println(coverageRequired);
            HopsTask task = new HopsTask(dataNames, coverageRequired);
            super.actualGenerateAndDisplay(task);

        }
    }

    class CoverageTask extends AbstractTask {
        private final int hopsUsed;
        List<Double> coverageList = new ArrayList<Double>();
        HashMultimap<String, String> result = HashMultimap.create();


        public CoverageTask(Collection<String> dataNames, int hopsUsed) {
            super(dataNames);
            this.hopsUsed = hopsUsed;

        }

        @Override
        protected void doTasks(String dataName) {

            DirectedSparseMultigraph<ModelObject, ModelEdge> path
                    = NetworkModel.instance().getDirectedGraphOfPlayer(dataName,
                    Collections.singleton(Phase.EXPLORATION));

            coverageList.add(GraphUtilities.calculateCoverageForHops(path, hopsUsed));


        }

        @Override
        protected void summarizeAndDisplay(){
            HashMap<String, Double> result = findMeanAndSD(coverageList);

            NumberFormat doubleFormat = new DecimalFormat("######.000");
            JOptionPane.showMessageDialog(new JFrame(),
                    "hops Used=" + hopsUsed + "\n" +
                            "<html><font color=\"red\">Coverage = " + doubleFormat.format(result.get("mean")) + " \u00B1 " +
                            doubleFormat.format(result.get("sd")) + "</font></html>", "Coverage calculated", JOptionPane.INFORMATION_MESSAGE
            );


        }
    }

    private<T extends Number> HashMap<String, Double> findMeanAndSD(List<T> valueList) {
        double[] values = new double[valueList.size()];
        int count = 0;
        for (T number : valueList) {
            values[count] = number.doubleValue();
            count++;
        }

        Mean meanCalculator = new Mean();
        double calculatedMean = meanCalculator.evaluate(values);
        StandardDeviation sd = new StandardDeviation();
        double calculatedSD = sd.evaluate(values);

        HashMap<String, Double> result = new HashMap<String, Double>();
        result.put("mean", calculatedMean);
        result.put("sd", calculatedSD);
        return result;
    }


    class HopsTask extends StatisticsHandler.AbstractTask {
        private final double coverageRequired;
        HashMultimap<String, String> result = HashMultimap.create();
        List<Integer> hopsTaken = new ArrayList<Integer>();

        public HopsTask(Collection<String> dataNames, double coverageRequired) {
            super(dataNames);
            this.coverageRequired = coverageRequired;

        }

        @Override
        protected void doTasks(String dataName) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> path
                    = NetworkModel.instance().getDirectedGraphOfPlayer(dataName,
                    Collections.singleton(Phase.EXPLORATION));

            hopsTaken.add(GraphUtilities.calculateHopsForCoverage(path, coverageRequired));
        }

        @Override
        protected void summarizeAndDisplay(){
            HashMap<String, Double> result = findMeanAndSD(hopsTaken);
            NumberFormat doubleFormat = new DecimalFormat("######.000");
            JOptionPane.showMessageDialog(new JFrame(),
                            "coverage required=" + coverageRequired + "\n" +
                            "<html><font color=\"red\">HOPS NEEDED = " + doubleFormat.format(result.get("mean")) + " \u00B1 " +
                            doubleFormat.format(result.get("sd")) + "</font></html>", "Hops needed calculated", JOptionPane.INFORMATION_MESSAGE
            );

        }
    }


    private class FindTypeFrame extends JFrame implements ChangeListener {

        private JSlider hopsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 350, 300);
        private JLabel hopsLabel = new JLabel("300");

        private JSlider coverageSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 100, 60);
        private JLabel coverageLabel = new JLabel("60% ");

        public FindTypeFrame(){
            HopAndCoverageCalculator.this.coverageRequired =60;
            HopAndCoverageCalculator.this.numberOfHops =300;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setLayout(new BorderLayout());
                    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
                    JPanel detailsPanel = new JPanel(new GridLayout(2, 2));


                    detailsPanel.add(new JLabel("Hops required"));
                    JPanel hopsSliderPanel = new JPanel(new BorderLayout());
                    hopsSliderPanel.add(hopsSlider, BorderLayout.CENTER);
                    hopsSliderPanel.add(hopsLabel, BorderLayout.WEST);
                    detailsPanel.add(hopsSliderPanel);

                    detailsPanel.add(new JLabel("Coverage required"));
                    JPanel coverageSliderPanel = new JPanel(new BorderLayout());
                    coverageSliderPanel.add(coverageSlider, BorderLayout.CENTER);
                    coverageSliderPanel.add(coverageLabel, BorderLayout.WEST);
                    detailsPanel.add(coverageSliderPanel);


                    calculateCoverage.addActionListener(HopAndCoverageCalculator.this);
                    calculateHops.addActionListener(HopAndCoverageCalculator.this);

                    coverageSlider.addChangeListener(FindTypeFrame.this);
                    hopsSlider.addChangeListener(FindTypeFrame.this);



                    buttonPanel.add(calculateCoverage);
                    buttonPanel.add(calculateHops);


                    add(detailsPanel, BorderLayout.NORTH);
                    add(buttonPanel, BorderLayout.SOUTH);




                    setLocation(200, 150);
                    setSize(650, 400);
                    setVisible(true);
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    setTitle("Choose data type");
                }
            });
        }


        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource()== coverageSlider){
                coverageLabel.setText(String.valueOf(coverageSlider.getValue()));
                HopAndCoverageCalculator.this.coverageRequired = Double.parseDouble(coverageLabel.getText());
//                System.out.println(HopAndCoverageCalculator.this.coverageRequired);
            } else if(e.getSource() == hopsSlider){
                hopsLabel.setText(String.valueOf(hopsSlider.getValue()));

                HopAndCoverageCalculator.this.numberOfHops = Integer.parseInt(hopsLabel.getText());
//                System.out.println(HopAndCoverageCalculator.this.numberOfHops);
            }
        }
    }
}
