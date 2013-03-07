package stats.chartdisplays;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.SliderMenuItem;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeRepetitionFrequencyChartDisplay extends ChartDisplay<Multiset<Double>> implements WindowListener, ActionListener {

    private final SliderMenuItem miBinSize = new SliderMenuItem("BIN_SIZE", 1, 60, 1);
    private final JButton regenerate = new JButton("regenerate");
    private JFrame binSizeFrame;
    private Multiset<Double> data;
    private JLabel statusLabel = new JLabel();

    @Override
    public void display(Multiset<Double> data) {


        this.data = data;
        final Dataset dataSet = createDataSet(data, miBinSize.getValue());
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle() + ": deltaT = Time between Room use");
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(520, 300));
        currentFrame.addWindowListener(this);

        miBinSize.setLabelTable(miBinSize.createStandardLabels(5, 5));
        binSizeFrame = new JFrame("Choose bin size:");
        binSizeFrame.setLayout(new BorderLayout());
        binSizeFrame.add(miBinSize, BorderLayout.NORTH);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setText("Current bin size:" + miBinSize.getValue() + " sec");
        binSizeFrame.add(statusLabel, BorderLayout.CENTER);

        binSizeFrame.add(regenerate, BorderLayout.SOUTH);
        regenerate.addActionListener(this);

        binSizeFrame.setLocation(100, 10);
        binSizeFrame.setSize(300, 200);
        binSizeFrame.setVisible(true);
    }


    public JFreeChart createChart(Dataset dataSet) {
//        final JFreeChart chart = ChartFactory.createScatterPlot(
//                this.getTitle() + ": deltaT = Time between Room use",
//                "deltaT = Time between Room use",
//                "Frequency",
//                (XYDataset) dataSet,
//                PlotOrientation.VERTICAL,
//                false,
//                true,
//                false
//        );

        final JFreeChart chart = ChartFactory.createXYLineChart(
                this.getTitle() + ": deltaT = Time between Room use",
                "deltaT = Time between Room use",
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


        return chart;

    }


    public Dataset createDataSet(Multiset<Double> data, int value) {
        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
        final XYSeries series = new XYSeries("blah");

        TreeSet<Double> sortedKeys = new TreeSet<Double>();

//        long lastKey =0;

        Multiset<Double> resizedBins = resizeBins(data, value);

        sortedKeys.addAll(resizedBins.elementSet());
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

    private Multiset<Double> resizeBins(Multiset<Double> data, int value) {
        if (value == 1) {
            return data;
        } else {
            Multiset<Double> result = HashMultiset.create();
            for (Double key : data.elementSet()) {
                double newValue = Math.floor(key / value);
                result.add(newValue, data.count(key));

            }
            return result;
        }
    }


    @Override
    public void windowOpened(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void windowClosed(WindowEvent e) {
        binSizeFrame.dispose();
    }

    @Override
    public void windowIconified(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == regenerate) {
            int value = miBinSize.getValue();
            statusLabel.setText("Current bin size:" + value + " sec");
            final Dataset dataSet = createDataSet(data, value);
            final JFreeChart chart = createChart(dataSet);
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 270));

            currentFrame.setContentPane(chartPanel);
            currentFrame.revalidate();
        }
    }
}