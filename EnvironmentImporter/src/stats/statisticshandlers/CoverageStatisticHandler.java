package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.CoverageChartDisplay;
import stats.consoledisplays.CoverageConsoleDisplay;

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

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.COVERAGE, phase);
        super.actualGenerateAndDisplay(task);


    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMultimap<String, String> result = HashMultimap.create();
        HashMultimap<Integer, String> coverageLevel = HashMultimap.create();


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
        }

        @Override
        protected void doTasks(String dataName) {

            coverageLevel.put(NetworkModel.instance().getCoverage(dataName, phase), dataName);

        }

        @Override
        protected void summarizeAndDisplay() {
            chartDisplay.setTitle(choice.toString() + phase.toString());

            chartDisplay.display(coverageLevel);
            consoleDisplay.display(coverageLevel);
        }
    }


}
