package stats.chartdisplays;

import com.google.common.collect.Multiset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeRepetitionFrequencyChartDisplay extends ChartDisplay<Multiset<Double>> {


    @Override
    public void display(Multiset<Double> data) {
        final Dataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle() + ": deltaT = Time between Door use");
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(520, 300));

    }


    public JFreeChart createChart(Dataset dataSet) {
        final JFreeChart chart = ChartFactory.createScatterPlot(
                this.getTitle() + ": deltaT = Time between Door use",
                "deltaT = Time between Room visits",
                "Frequency",
                (XYDataset) dataSet,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        Shape cross = ShapeUtilities.createDiagonalCross(1, 1);


        ((XYPlot) chart.getPlot()).getRenderer().setSeriesShape(0, cross);

        // set the range axis to display integers only...
        NumberAxis rangeAxis = (NumberAxis) ((XYPlot) chart.getPlot()).getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());



        return chart;

    }


    public Dataset createDataSet(Multiset<Double> data) {
        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
        final XYSeries series = new XYSeries("blah");

        TreeSet<Double> sortedKeys = new TreeSet<Double>();
        sortedKeys.addAll(data.elementSet());
//        long lastKey =0;
        for (Double key : sortedKeys) {
//            while(lastKey<key){
//                series.add(0, lastKey);
////                dataSet.addValue(0, ""+lastKey, "");
//                lastKey++;
//            }
//            System.out.println(key+","+ data.count(key));

            series.add((double) key, (double) data.count(key));
//            dataSet.addValue(data.count(key), key.toString(), "");
        }

        seriesCollection.addSeries(series);

        return seriesCollection;
    }


}