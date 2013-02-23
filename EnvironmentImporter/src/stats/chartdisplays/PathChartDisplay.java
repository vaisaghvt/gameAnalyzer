package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import gui.Phase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathChartDisplay extends ChartDisplay<HashMultimap<String, String>> {


    private Phase phase;

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    @Override
    public void display(HashMultimap<String, String> data) {
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

    private JFreeChart createChart(CategoryDataset dataSet) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                this.getTitle(),         // chart title
                "",               // domain axis label
                "Number",                  // range axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );


        return chart;

    }

    private CategoryDataset createDataSet(HashMultimap<String, String> data) {

        if (phase == Phase.TASK_2) {
            final String thirdFloor = "3rd Floor";
            final String secondFloor = "2nd Floor";
            final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

            for (String path : data.keySet()) {
                String category = path.substring(0, 1).equals("2") ? secondFloor : thirdFloor;
                dataSet.addValue(data.get(path).size(), path, category);
            }

            return dataSet;
        } else if (phase == Phase.TASK_3) {
            final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
            for (String path : data.keySet()) {

                dataSet.addValue(data.get(path).size(), path, "default");
            }
            return dataSet;

        } else {
            return null;
        }
    }


}