package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import gui.YES_NO_CHOICE;
import stats.StatisticChoice;
import stats.chartdisplays.CorridorPrefChartDisplay;
import stats.consoledisplays.CorridorPrefConsoleDisplay;

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
public class CorridorPrefStatisticHandler extends StatisticsHandler<CorridorPrefConsoleDisplay, CorridorPrefChartDisplay> {


    public CorridorPrefStatisticHandler() {
        super(new CorridorPrefChartDisplay(),
                new CorridorPrefConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.CORRIDOR_PREFERENCE_MEASURE,phase);
        super.actualGenerateAndDisplay(task);


    }


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMultimap<String,String> result = HashMultimap.create();



        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
        }

        @Override
        protected void doTasks(String dataName) {
            YES_NO_CHOICE resultTemp;
            synchronized (NetworkModel.instance()) {
                resultTemp = NetworkModel.instance().getCorridorRelatedMotion(dataName, phase);
            }
            if (resultTemp == YES_NO_CHOICE.YES) {
                result.put("yes", dataName);
            } else if (resultTemp == YES_NO_CHOICE.NO) {
                result.put("no", dataName);
            } else {
                result.put("maybe", dataName);
            }
        }

        @Override
        protected void summarizeAndDisplay() {
            chartDisplay.setTitle(choice.toString() + phase.toString());
            chartDisplay.setPhase(phase);
            chartDisplay.display(result);
            consoleDisplay.display(result);
        }
    }


}
