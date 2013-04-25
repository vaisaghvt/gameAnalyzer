package stats.chartdisplays;

import com.google.common.collect.HashBasedTable;
import modelcomponents.CompleteGraph;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import stats.statisticshandlers.MarkovDataDialog;

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
public class PathHeatMapChartDisplay extends ChartDisplay<HashBasedTable<String, String, Double>> {


    private HashMap<String, Integer> roomToCodeMapping;
    private MarkovDataDialog.HeatMapType type;


    @Override
    public void display(HashBasedTable<String, String, Double> data) {


        roomToCodeMapping = generateNewRoomToCodeMapping();
        final XYZDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1000, 540));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(1020, 560));
    }

    private HashMap<String, Integer> generateNewRoomToCodeMapping() {
        int i = 0;
        //Graph<ModelObject, ModelEdge> graph = NetworkModel.instance().getCompleteGraph();
        Collection<String> rooms= CompleteGraph.instance().getFloorNeighbourSortedRooms();
        HashMap<String, Integer> nameToCodeMapping = new HashMap<String, Integer>();
        for (String name : rooms) {
//            System.out.println(i+":"+name);
            nameToCodeMapping.put(name, i++);
        }

        return nameToCodeMapping;
    }

    private XYZDataset createDataSet(HashBasedTable<String, String, Double> data) {

        MatrixSeries series = new MatrixSeries("DataTable", roomToCodeMapping.keySet().size(), roomToCodeMapping.keySet().size());

        for (String source : roomToCodeMapping.keySet()) {
            Integer sourceId = roomToCodeMapping.get(source);


            for (String destination : roomToCodeMapping.keySet()) {

                Integer destinationId = roomToCodeMapping.get(destination);
                if (!data.contains(source, destination)) {
                    series.update(sourceId, destinationId, -1.0);
                } else {
                    double value;
                    if(type == MarkovDataDialog.HeatMapType.COMPARISON)
                        value =(data.get(source,destination)+1.0)/2; // Scale to 0 to 1 for comparison
                    else
                        value = data.get(source, destination);
                    if(value<0 ||value>1){
                        System.out.println("***********Problem in value=" + value + "(" +source+","+destination+")" );
                    }
                    series.update(sourceId,destinationId, value);
                }
            }

        }


        System.out.println("Room size = "+ roomToCodeMapping.keySet().size());
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
        PaintScale paintScale;
        if(this.type== MarkovDataDialog.HeatMapType.COMPARISON)
            paintScale= createNewLookupPaintScale();
        else
            paintScale = new GrayPaintScale();

//        paintScale.add(-1.0,Color.WHITE );
        xyblockrenderer.setPaintScale(paintScale);

        XYPlot xyplot = new XYPlot(xyzDataSet, numberAxis, numberAxis1, xyblockrenderer);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setOutlinePaint(Color.blue);

        JFreeChart jfreechart = new JFreeChart("heat map", xyplot);
        jfreechart.removeLegend();

        NumberAxis numberAxis2 = new NumberAxis("Scale");
        numberAxis2.setAxisLinePaint(Color.white);
        numberAxis2.setTickMarkPaint(Color.white);
        numberAxis2.setRange(-1.0D, 1.0D);
        numberAxis2.setTickLabelFont(new Font("Dialog", 0, 7));
        PaintScaleLegend paintscalelegend = new PaintScaleLegend(
                paintScale, numberAxis2);
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

    private LookupPaintScale createNewLookupPaintScale() {
        LookupPaintScale paintScale = new LookupPaintScale();

        paintScale.add(0.0, Color.BLUE);
        paintScale.add(0.4, Color.WHITE);
        paintScale.add(0.6, Color.RED);
        return paintScale;
//        double valueDivisionSize = 2.0/NUMBER_OF_DIVISIONS;
//        int colorDivisionSize = 255/NUMBER_OF_DIVISIONS;
//
//
//        int redValue = 0;
//        int blueValue =255;
//        double value= -1.0;
//        for(int i=0;i<NUMBER_OF_DIVISIONS;i++){
//            paintScale.add(value, new Color(redValue, 0, blueValue));
//            value+=valueDivisionSize;
//            redValue+=colorDivisionSize;
//            blueValue-=colorDivisionSize;
//        }
//
//        return paintScale;
    }


    public void setType(MarkovDataDialog.HeatMapType type) {
        this.type = type;
    }
}