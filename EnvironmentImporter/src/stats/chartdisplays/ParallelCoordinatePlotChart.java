package stats.chartdisplays;

import database.Database;
import org.apache.commons.lang3.math.NumberUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParallelCoordinatePlotChart extends ChartDisplay<HashMap<String, HashMap<String, String>>> implements ActionListener {


    private JButton okButton = new JButton("OK");
    private HashSet<JCheckBox> statChoices;

    private HashMap<String, HashMap<String, String>> data;
    private JFrame optionFrame;

    @Override
    public void display(HashMap<String, HashMap<String, String>> data) {


        this.data = data;
        initialiseChoices(data.values().iterator().next().keySet());

    }

    public void initialiseChoices(Set<String> keySet) {


        this.optionFrame = new JFrame("Choose stats to use");
        optionFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        optionFrame.setLayout(new BorderLayout());

        JPanel statChoicePanel = new JPanel(new GridLayout(keySet.size(),1));

        statChoices = new HashSet<JCheckBox>();
        for(String key:keySet){
            JCheckBox cBox = new JCheckBox(key);
            cBox.setSelected(true);
            statChoicePanel.add(cBox);
            statChoices.add(cBox);
        }

        okButton.addActionListener(this);


        optionFrame.getContentPane().add(statChoicePanel, BorderLayout.NORTH);
        optionFrame.getContentPane().add(okButton, BorderLayout.SOUTH);

        optionFrame.setVisible(true);
        optionFrame.setSize(400, 500);


    }


    public XYDataset createDataSet(HashMap<String, HashMap<String, String>> data, HashSet<String> statsChosen) {

        HashMap<String, Double> maxForEachKey = getMaxForEachKey(data);

        final XYSeriesCollection dataset = new XYSeriesCollection();


        HashMap<String, Integer> locationMap=null;
        for (String dataName : data.keySet()) {
            final XYSeries series = new XYSeries(dataName);

            HashMap<String, String> resultForName = data.get(dataName);
            if (locationMap == null) {
                locationMap = generateLocationForKeys(statsChosen);
            }
            for (String key : statsChosen) {
                if (NumberUtils.isNumber(resultForName.get(key))) {
                    double location = (double) locationMap.get(key);
                    double value = (Double.parseDouble(resultForName.get(key))) / maxForEachKey.get(key);

                    series.add(location, value);
                }
            }


            dataset.addSeries(series);
        }


        return dataset;
    }

    private HashMap<String, Double> getMaxForEachKey(HashMap<String, HashMap<String, String>> data) {

        HashMap<String, Double> maxForEachKey = new HashMap<String, Double>();
        for(String dataName : data.keySet()){
            HashMap<String, String> resultForName = data.get(dataName);
            for(String key:resultForName.keySet()){
                if (NumberUtils.isNumber(resultForName.get(key))) {
                    if(maxForEachKey.containsKey(key)){
                        maxForEachKey.put(key,
                                Math.max(
                                        maxForEachKey.get(key),
                                        Double.parseDouble(resultForName.get(key))));

                    }else{
                        maxForEachKey.put(key,
                                Double.parseDouble(resultForName.get(key)));
                    }

                }
            }
        }
        return maxForEachKey;
    }

    private HashMap<String, Integer> generateLocationForKeys(Set<String> strings) {
        int n = 0;
        System.out.println("Legend");
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (String key : strings) {
            result.put(key, n);
            System.out.println(n + ":" + key);
            n++;
        }
        return result;
    }


    public JFreeChart createChart(XYDataset dataset) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                this.getTitle(),      // chart title
                "Key(refer to console)",                      // x axis label
                "NormalizedValue",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
        //      legend.setDisplaySeriesShapes(true);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(okButton)){
            HashSet<String> statsChosen = new HashSet<String>();
            for(JCheckBox box: statChoices){
                if(box.isSelected()){
                    statsChosen.add(box.getText());
                }
            }
            optionFrame.dispose();


            final XYDataset dataSet = createDataSet(data, statsChosen);
            final JFreeChart chart = createChart(dataSet);
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 270));

            createNewFrameAndSetLocation();
            currentFrame.setTitle(this.getTitle());
            currentFrame.setContentPane(chartPanel);
            currentFrame.setVisible(true);
            currentFrame.setSize(new Dimension(520, 300));
        }
    }
}