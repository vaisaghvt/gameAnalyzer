package stats.chartdisplays;

import com.google.common.collect.Multiset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexVisitChartDisplay extends ChartDisplay<Multiset<Double>> {


    @Override
    public void display(Multiset<Double> data) {
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


    public CategoryDataset createDataSet(Multiset<Double> data) {


        TreeSet<Double> vertices = new TreeSet<Double>();
        vertices.addAll(data.elementSet());

        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (Double frequency : vertices) {
            dataSet.addValue(data.count(frequency), frequency.toString(), "");

        }

        return dataSet;

    }


    public JFreeChart createChart(CategoryDataset dataSet) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                this.getTitle(),         // chart title
                "Normalized Number of visits",               // domain axis label
                "Frequency",                  // range axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL, // orientation
                false,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );



        return chart;
    }


}