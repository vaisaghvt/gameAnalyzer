package stats.statisticshandlers;

import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
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
    protected JFrame progressFrame;


    protected StatisticChoice choice;

    protected StatisticsHandler(V chartDisplay, T consoleDisplay) {
        this.chartDisplay = chartDisplay;

        this.consoleDisplay = consoleDisplay;
        String OS = System.getProperty("os.name").toLowerCase();

    }

    public abstract void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType type);

    public <U extends AbstractTask> void actualGenerateAndDisplay(U task) {
        if (!task.getDataNames().isEmpty()) {
            createProgressBar();
            task.addPropertyChangeListener(this);
            task.execute();
        } else {
            System.out.println("No Data Names selected!");
        }

    }


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
        DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        progressFrame = new JFrame("Processing data");
        progressFrame.setLayout(new BorderLayout());
        progressFrame.add(progressBar, BorderLayout.NORTH);
        progressFrame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        progressFrame.setLocation((int) Math.floor(width / 2 - 200), (int) Math.floor(height / 2 - 100));
        progressFrame.setSize(400, 200);
        progressFrame.setVisible(true);

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("progress".equals(event.getPropertyName())) {
            int progress = (Integer) event.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);


        }
    }

    abstract class AbstractTask extends SwingWorker<Void, Void> {

        protected final Collection<String> dataNames;


        public AbstractTask(Collection<String> dataNames) {
            this.dataNames = dataNames;


        }

        protected abstract void doTasks(String dataName);

        protected abstract void summarizeAndDisplay();

        @Override
        public final Void doInBackground() {
//            if(!windows){
//               SwingUtilities.invokeLater(new Runnable() {
//                   @Override
//                   public void run() {
//                       taskOutput.append("Refer to console for progress");
//                   }
//               });
//
//            }
            setProgress(0);
            int size = dataNames.size();
            int i = 1;
            for (String dataName : dataNames) {
                final String tempDataName = dataName;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        taskOutput.append("Processing " + tempDataName + "...\n");
                    }
                });
                doTasks(dataName);
                setProgress((i * 100) / size);
                i++;
            }
            return null;
        }

        @Override
        public final void done() {
            Toolkit.getDefaultToolkit().beep();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressFrame.dispose();
                    taskOutput.append("Done.");
                }
            });


            summarizeAndDisplay();
        }

        public Collection<String> getDataNames() {
            return dataNames;
        }
    }


}
