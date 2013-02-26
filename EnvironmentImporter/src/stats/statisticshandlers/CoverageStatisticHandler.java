package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.CoverageChartDisplay;
import stats.consoledisplays.CoverageConsoleDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoverageStatisticHandler extends StatisticsHandler<CoverageConsoleDisplay, CoverageChartDisplay> {


    public CoverageStatisticHandler() {
        super(new CoverageChartDisplay(),
                new CoverageConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.COVERAGE;

        if (!dataNames.isEmpty()) {
            createProgressBar();
            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, choice, phase);
            task.addPropertyChangeListener(this);
            task.execute();
        } else {
            System.out.println("No Data Names selected!");
        }

    }

    class GenerateRequiredDataTask extends SwingWorker<Void, Void> {
        private final Phase phase;
        private final Collection<String> dataNames;
        private final StatisticChoice choice;
        HashMultimap<String, String> result = HashMultimap.create();
        HashMultimap<Integer, String> coverageLevel = HashMultimap.create();


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase) {
            this.dataNames = dataNames;
            this.choice = choice;
            this.phase = phase;

        }

        @Override
        public Void doInBackground() {
            setProgress(0);
            int size = dataNames.size();
            int i = 1;
            for (String dataName : dataNames) {
                taskOutput.append("Processing " + dataName + "...\n");

                synchronized (NetworkModel.instance()) {
                    coverageLevel.put(NetworkModel.instance().getCoverage(dataName, phase), dataName);
                }
                setProgress((i * 100) / size);

                i++;

            }
            return null;

        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            frame.dispose();
            taskOutput.append("Done.");
            frame.dispose();
            chartDisplay.setTitle(choice.toString() + phase.toString());

            chartDisplay.display(coverageLevel);
            consoleDisplay.display(coverageLevel);

        }
    }


}
