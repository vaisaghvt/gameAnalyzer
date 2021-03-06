package stats.chartdisplays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SignificantVertexChartDisplay extends ChartDisplay<HashMap<String, HashMap<String, Number>>> {


    @Override
    public void display(HashMap<String, HashMap<String, Number>> data) {
        final CategoryDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(520, 300));
    }


    public CategoryDataset createDataSet(HashMap<String, HashMap<String, Number>> data) {


        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (String dataName : data.keySet()) {
            HashMap<String, Number> dataValue = data.get(dataName);
            for (String room : dataValue.keySet()) {
                dataSet.addValue(dataValue.get(room), dataName, room);
            }
        }

        return dataSet;

    }


    public JFreeChart createChart(CategoryDataset dataSet) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                this.getTitle(),         // chart title
                "Room",               // domain axis label
                "Value",                  // range axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                false,                     // tooltips?
                false                     // URLs?
        );


        return chart;
    }


}