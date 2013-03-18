package stats.chartdisplays;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathHeatMapChartDisplay extends ChartDisplay<HashMap<String, HashMap<String, Double>>> {


    private HashMap<String, Integer> roomToCodeMapping;

    @Override
    public void display(HashMap<String, HashMap<String, Double>> data) {


        roomToCodeMapping = generateNewRoomToCodeMapping(data);
        final XYZDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(520, 300));
    }

    private HashMap<String, Integer> generateNewRoomToCodeMapping(
            HashMap<String, HashMap<String, Double>> data) {
        int i=0;
        HashMap<String, Integer> nameToCodeMapping = new HashMap<String, Integer>();
        for(String name:data.keySet()){
            nameToCodeMapping.put(name, i++);
        }
        return nameToCodeMapping;
    }

    private XYZDataset createDataSet(HashMap<String, HashMap<String, Double>> data) {

        MatrixSeries series = new MatrixSeries("DataTable",data.keySet().size(),data.keySet().size());

        for(String source: data.keySet()){
            Integer sourceId = roomToCodeMapping.get(source);

            for(String destination : data.get(source).keySet()){
                Integer destinationId = roomToCodeMapping.get(destination);
                series.update(sourceId, destinationId, data.get(source).get(destination));
            }

        }




        return new MatrixSeriesCollection(series);
    }




    private JFreeChart createChart(XYZDataset xyzDataSet) {
        NumberAxis numberAxis = new NumberAxis("X");
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberAxis.setLowerMargin(0.0D);
        numberAxis.setUpperMargin(0.0D);
        numberAxis.setAxisLinePaint(Color.white);
        numberAxis.setTickMarkPaint(Color.white);

        NumberAxis numberAxis1 = new NumberAxis("Y");
        numberAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberAxis1.setLowerMargin(0.0D);
        numberAxis1.setUpperMargin(0.0D);
        numberAxis1.setAxisLinePaint(Color.white);
        numberAxis1.setTickMarkPaint(Color.white);


        XYBlockRenderer xyblockrenderer = new XYBlockRenderer();
        GrayPaintScale graypaintscale = new GrayPaintScale(-2D, 1.0D);
        xyblockrenderer.setPaintScale(graypaintscale);
        XYPlot xyplot = new XYPlot(xyzDataSet, numberAxis, numberAxis1, xyblockrenderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setOutlinePaint(Color.blue);

        JFreeChart jfreechart = new JFreeChart("2nd Order path visit heat map", xyplot);
        jfreechart.removeLegend();

        NumberAxis numberAxis2 = new NumberAxis("Scale");
        numberAxis2.setAxisLinePaint(Color.white);
        numberAxis2.setTickMarkPaint(Color.white);
        numberAxis2.setTickLabelFont(new Font("Dialog", 0, 7));
        PaintScaleLegend paintscalelegend = new PaintScaleLegend(
                new GrayPaintScale(), numberAxis2);
        paintscalelegend.setStripOutlineVisible(false);
        paintscalelegend.setSubdivisionCount(20);
        paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        paintscalelegend.setAxisOffset(5D);
        paintscalelegend.setMargin(new RectangleInsets(5D, 5D, 5D, 5D));
//        paintscalelegend.setFrame(new BlockBorder(Color.red));
        paintscalelegend.setPadding(new RectangleInsets(10D, 10D, 10D, 10D));
        paintscalelegend.setStripWidth(10D);
        paintscalelegend.setPosition(RectangleEdge.LEFT);
        jfreechart.addSubtitle(paintscalelegend);
//        ChartUtilities.applyCurrentTheme(jfreechart);
        return jfreechart;
    }



}