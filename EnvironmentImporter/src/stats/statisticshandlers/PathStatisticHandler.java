package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.PathChartDisplay;
import stats.consoledisplays.PathConsoleDisplay;

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
public class PathStatisticHandler extends StatisticsHandler<PathConsoleDisplay, PathChartDisplay> {


    public PathStatisticHandler() {
        super(new PathChartDisplay(),
                new PathConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.PATH_FREQUENCY, phase);
        super.actualGenerateAndDisplay(task);





    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMultimap<String, String> result = HashMultimap.create();


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
        }

        @Override
        protected void doTasks(String dataName) {
            String paths;
            synchronized (NetworkModel.instance()) {
                paths =  NetworkModel.instance().getPathDataFor(dataName, phase);
            }

            if(phase ==Phase.TASK_3){
                result.put(paths, dataName);
            }else if(phase == Phase.TASK_2){
                String[] parts = paths.split(",");
                result.put(parts[0],dataName);
                result.put(parts[1],dataName);
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
