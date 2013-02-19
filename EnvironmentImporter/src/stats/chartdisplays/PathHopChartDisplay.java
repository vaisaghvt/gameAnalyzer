package stats.chartdisplays;

import gui.Phase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathHopChartDisplay extends ChartDisplay<HashMap<String, HashMap<String, String>>> {


    private Phase phase;

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    @Override
    public void display(HashMap<String, HashMap<String, String>> passedData) {
        final XYDataset data = convertToScatterPoints(passedData);
        final JFreeChart chart = createChart(data);
        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        //      panel.setHorizontalZoom(true);
        //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);
        currentFrame.setTitle("blah");
        createNewFrameAndSetLocation();
        currentFrame.setContentPane(panel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(520, 300));
    }

    private JFreeChart createChart(XYDataset data) {
        final NumberAxis domainAxis = new NumberAxis("Path number");
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis("Edge count");
        rangeAxis.setAutoRangeIncludesZero(false);



        final JFreeChart chart = ChartFactory.createScatterPlot("Route Hop relationship",
                "Route", "Number of hops/5", data, PlotOrientation.VERTICAL, true, true, false);;
        Shape cross = ShapeUtilities.createDiagonalCross(3, 1);


        ((XYPlot)chart.getPlot()).getRenderer().setSeriesShape(0, cross);

//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return chart;
    }

    private XYDataset convertToScatterPoints(HashMap<String, HashMap<String, String>> data) {


        float[] result = new float[2];


        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries attempt1 = new XYSeries("Attempt 1 or 2");
        XYSeries attempt2 = new XYSeries("Attempt >2");

        for (String name : data.keySet()) {
            HashMap<String, String> map = data.get(name);
            String path = map.get("path");
            String hops = map.get("hops");
            result[1] = Float.parseFloat(hops) + (0.05f - 0.1f * ((float) Math.random() - 0.5f));


            int k;
            if (path.equals("Shortest")) {
                k = 0;
            } else if (path.equals("Path2")
                    || path.equals("Path3")
                    || path.equals("Path4")) {
                k = 1;
            } else if (path.equals("lost")) {
                k = 3;
            } else {

                k = 2;
            }

            result[0] = k + (0.05f - 0.1f * ((float) Math.random() - 0.5f));


        }

        xySeriesCollection.addSeries(attempt1);
        xySeriesCollection.addSeries(attempt2);

        return xySeriesCollection;


    }


}