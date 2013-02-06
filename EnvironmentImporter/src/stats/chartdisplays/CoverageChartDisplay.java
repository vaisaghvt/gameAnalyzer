package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoverageChartDisplay extends ChartDisplay<HashMultimap<Integer, String>> {





    @Override
    public void display(HashMultimap<Integer, String> data) {
        final CategoryDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        JFrame frame = new JFrame(getTitle());
        frame.setContentPane(chartPanel);
        frame.setVisible(true);
        frame.setLocation(100,100);
        frame.setSize(new Dimension(520, 300));
    }


    public CategoryDataset createDataSet(HashMultimap<Integer, String> data) {

        final float BIN_SIZE =5f;
        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        HashMultimap<Integer, String> result = HashMultimap.create();
        for(Integer number: data.keySet()){
            Integer bin = (int)(Math.floor(number/BIN_SIZE) * Math.floor(BIN_SIZE));
            result.putAll(bin, data.get(number));
        }

        for(int i=0;i<=100;i+=BIN_SIZE){

            if(i!=100){
                dataSet.addValue(result.get(i).size(), i+"-"+(i+BIN_SIZE-1), "");
            }else{
                dataSet.addValue(result.get(i).size(), "100","");
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