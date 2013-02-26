package stats.statisticshandlers;

import gui.Phase;
import gui.StatsDialog;
import stats.chartdisplays.ChartDisplay;
import stats.consoledisplays.ConsoleDisplay;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StatisticsHandler<T extends ConsoleDisplay, V extends ChartDisplay> implements PropertyChangeListener {

    T consoleDisplay;
    V chartDisplay;
    protected JProgressBar progressBar;
    protected JTextArea taskOutput;
    protected JFrame frame;

    protected StatisticsHandler(V chartDisplay, T consoleDisplay) {
        this.chartDisplay = chartDisplay;

        this.consoleDisplay = consoleDisplay;


    }

    public abstract void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType type);

    public static double aggregate(Set<? extends Number> doubles, StatsDialog.AggregationType aggregationType) {
        double result = 0.0;
        switch (aggregationType) {

            case SUM:

                for (Number value : doubles) {
                    result += value.doubleValue();
                }
                return result;
            case MEAN:

                int n = 0;
                for (Number value : doubles) {
                    result += value.doubleValue();
                    n++;
                }

                return result / n;
            case MIN:

                for (Number value : doubles) {
                    result = Math.min(result, value.doubleValue());
                }
                return result;
            case MAX:

                for (Number value : doubles) {
                    result = Math.max(result, value.doubleValue());
                }
                return result;
            default:
                throw new IllegalArgumentException();
        }
    }

    protected void createProgressBar() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        DefaultCaret caret = (DefaultCaret)taskOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        frame = new JFrame("Processing data");
        frame.setLayout(new BorderLayout());
        frame.add(progressBar, BorderLayout.NORTH);
        frame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setLocation((int) Math.floor(width / 2 - 200), (int) Math.floor(height / 2 - 100));
        frame.setSize(400, 200);
        frame.setVisible(true);

    }
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("progress".equals(event.getPropertyName())) {
            int progress = (Integer) event.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);


        }
    }



}
