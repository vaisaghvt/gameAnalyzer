package stats.chartdisplays;

import com.google.common.collect.HashBasedTable;
import gui.SliderMenuItem;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.HashMap;

/**
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 */
public class PathHeatMapChartDisplay extends ChartDisplay<HashBasedTable<String, String, Double>> implements ActionListener, WindowListener {

    private final SliderMenuItem unscaledDifferenceSlider = new SliderMenuItem("Difference size (*100)", 0, 50, 20);
    private final JButton regenerate = new JButton("regenerate");
    private JFrame differenceSelectorFrame;
    private JLabel statusLabel = new JLabel();
    private HashMap<String, Integer> roomToCodeMapping;
    private MarkovDataDialog.HeatMapType type;
    private double unscaledDifference = 0.2;
    private XYZDataset dataSet;


    @Override
    public void display(HashBasedTable<String, String, Double> data) {


        roomToCodeMapping = generateNewRoomToCodeMapping();
        dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1000, 540));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(1020, 560));
        currentFrame.addWindowListener(this);

        if (type == MarkovDataDialog.HeatMapType.COMPARISON) {
            unscaledDifferenceSlider.setLabelTable(unscaledDifferenceSlider.createStandardLabels(5, 5));
            differenceSelectorFrame = new JFrame("Choose Size of difference");
            differenceSelectorFrame.setLayout(new BorderLayout());
            differenceSelectorFrame.add(unscaledDifferenceSlider, BorderLayout.NORTH);

            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            statusLabel.setText("Current difference size:" + this.unscaledDifference);


            differenceSelectorFrame.add(statusLabel, BorderLayout.CENTER);

            differenceSelectorFrame.add(regenerate, BorderLayout.SOUTH);

            regenerate.addActionListener(this);

            differenceSelectorFrame.setLocation(100, 10);
            differenceSelectorFrame.setSize(300, 200);
            differenceSelectorFrame.setVisible(true);
        }

    }

    private HashMap<String, Integer> generateNewRoomToCodeMapping() {
        int i = 0;
        //Graph<ModelObject, ModelEdge> graph = NetworkModel.instance().getCompleteGraph();
        Collection<String> rooms = CompleteGraph.instance().getFloorNeighbourSortedRooms();
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
                    if (type == MarkovDataDialog.HeatMapType.COMPARISON)
                        value = (data.get(source, destination) + 1.0) / 2; // Scale to 0 to 1 for comparison
                    else
                        value = data.get(source, destination);
                    if (value < 0 || value > 1) {
                        System.out.println("***********Problem in value=" + value + "(" + source + "," + destination + ")");
                    }
                    series.update(sourceId, destinationId, value);
                }
            }

        }


        System.out.println("Room size = " + roomToCodeMapping.keySet().size());
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
        if (this.type == MarkovDataDialog.HeatMapType.COMPARISON)
            paintScale = createNewLookupPaintScale();
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

        double differenceFromCenter = unscaledDifference / 2;
        double lowerLevel = 0.5 - differenceFromCenter;
        double higherLevel = 0.5 + differenceFromCenter;
        paintScale.add(0.0, Color.BLUE);
        paintScale.add(lowerLevel, Color.WHITE);
        paintScale.add(higherLevel, Color.RED);
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


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == regenerate) {

            this.unscaledDifference = ((double) unscaledDifferenceSlider.getValue()) / 100.0;

            statusLabel.setText("Current difference size:" + this.unscaledDifference);

            final JFreeChart chart = createChart(dataSet);
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1000, 540));

            currentFrame.setContentPane(chartPanel);
            currentFrame.revalidate();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.differenceSelectorFrame.dispose();
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}