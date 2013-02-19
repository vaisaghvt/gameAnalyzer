package stats.chartdisplays;

import gui.NetworkModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexChartDisplay extends ChartDisplay<HashMap<String, ? extends Number>> {





    @Override
    public void display(HashMap<String,  ? extends Number> data) {
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


    public CategoryDataset createDataSet(HashMap<String,  ? extends Number> data) {


        Collection<String> sortedRoomNames = ((NetworkModel) NetworkModel.instance()).getSortedRooms();
        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (String roomName : sortedRoomNames) {
            if(data.containsKey(roomName)){
                dataSet.addValue(data.get(roomName), roomName, "");
            }else{
                dataSet.addValue(0,roomName,"");
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
                false,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );


        return chart;
    }


}