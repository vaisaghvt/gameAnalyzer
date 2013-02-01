package stats.chartdisplays;

import com.google.common.collect.HashMultimap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

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
public class RoomRepetitionChartDisplay extends ChartDisplay<HashMap<String, HashMap<String, HashMap<String, String>>>> {





    @Override
    public void display(HashMap<String, HashMap<String, HashMap<String, String>>> hashMap) {
        final XYDataset data = convertToScatterPoints(hashMap);


        final JFreeChart chart = createChart(data);
        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        //      panel.setHorizontalZoom(true);
        //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);
        JFrame frame = new JFrame("blah");
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setSize(new Dimension(520, 300));

    }

    private XYDataset convertToScatterPoints(HashMap<String, HashMap<String, HashMap<String, String>>> hashMap) {
        float[] result = new float[2];

        //TODO : verify that this is doing the right thing!!
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("blah");

        List<String> significantRooms = new ArrayList<String>();
        significantRooms.add("PictureGallery");
        significantRooms.add("Library");
        significantRooms.add("StartingPosition");
        significantRooms.add("Sauna");



        for (String dataName : hashMap.keySet()) {
            HashMap<String, HashMap<String, String>> roomsForPerson = hashMap.get(dataName);

                for (String significantRoom : roomsForPerson.keySet()) {
                    HashMap<String, String> roomRelatedData = roomsForPerson.get(significantRoom);
                    result[1] = Float.parseFloat(roomRelatedData.get("frequency"));
                    if(roomRelatedData.get("remembers").equals("yes"))   {
                        result[0] = significantRooms.indexOf(significantRoom) * 2;
                    } else{
                        result[0] = significantRooms.indexOf(significantRoom) * 2 + 1;
                     }

                    series.add(result[0], (result[1]) / 1000000);
                }


        }

        xySeriesCollection.addSeries(series);


        return xySeriesCollection;


    }
    private JFreeChart createChart(XYDataset data) {
        final NumberAxis domainAxis = new NumberAxis("Group");
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis("Visit Frequency");
        rangeAxis.setAutoRangeIncludesZero(false);


        final JFreeChart chart = ChartFactory.createScatterPlot("Visit frequency to memory relationship",
                "Memory", "Visit Frequency", data, PlotOrientation.VERTICAL, true, true, false);
        ;
        Shape cross = ShapeUtilities.createDiagonalCross(1, 1);


        ((XYPlot) chart.getPlot()).getRenderer().setSeriesShape(0, cross);

//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return chart;
    }





}