package stats.chartdisplays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceTimeChartDisplay extends ChartDisplay<HashMap<String, HashMap<String, Double>>> {





    @Override
    public void display(HashMap<String, HashMap<String, Double>> data) {
        final CategoryDataset timeDataSet = createTimeDataSet(data);
        final CategoryDataset distanceDataSet = createDistanceDataSet(data);
        currentFrame.setVisible(false);
        createFrameAndChart(timeDataSet, "time");
        createFrameAndChart(distanceDataSet, "distance");

    }

    private void createFrameAndChart(CategoryDataset dataSet, String type) {
        String st;
        if(getTitle().contains("Total")){
           st = "Total :";
        }else{
            st = "Tasks :";
        }

        final JFreeChart chart = createChart(dataSet,st+type+ " for each agent" );
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        JFrame frame = new JFrame("total "+type+ " for each agent");
        frame.setContentPane(chartPanel);
        frame.setVisible(true);
        frame.setLocation(100,100);
        frame.setSize(new Dimension(520, 300));
    }


    public CategoryDataset createDistanceDataSet(HashMap<String, HashMap<String, Double>> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String dataName : data.keySet()) {

            dataset.addValue(data.get(dataName).get("distance"),"distance", dataName);

//            dataset.addValue(data.get(dataName).get("distance"),"time", dataName);
        }
        return dataset;

    }


    public CategoryDataset createTimeDataSet(HashMap<String, HashMap<String, Double>> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String dataName : data.keySet()) {

            dataset.addValue(data.get(dataName).get("time"),"time", dataName);
            System.out.println(dataName+":"+data.get(dataName).get("time"));
//            dataset.addValue(data.get(dataName).get("distance"),"time", dataName);
        }
        return dataset;

    }
    public JFreeChart createChart(CategoryDataset dataSet, String s) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                s,         // chart title
                "Name",               // domain axis label
                "Value",                  // range axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );



        return chart;
    }


}