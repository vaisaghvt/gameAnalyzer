package stats.statisticshandlers;

import gui.Phase;
import gui.StatsDialog;
import stats.chartdisplays.PathPredictionChartDisplay;
import stats.chartdisplays.RoomInfoChartDisplay;
import stats.consoledisplays.PathPredictionConsoleDisplay;
import stats.consoledisplays.RoomInfoConsoleDisplay;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathPredictionHandler extends StatisticsHandler<PathPredictionConsoleDisplay, PathPredictionChartDisplay> {


    public PathPredictionHandler() {
        super(new PathPredictionChartDisplay(),
                new PathPredictionConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, allOrOne);
        super.actualGenerateAndDisplay(task);


    }



    class GenerateRequiredDataTask extends AbstractTask {


        private final StatsDialog.AllOrOne allOrOne;
        private final HashSet<Phase> allPhases;


        public GenerateRequiredDataTask(Collection<String> dataNames, StatsDialog.AllOrOne allOrOne) {
            super(dataNames);

            this.allOrOne = allOrOne;
            allPhases = new HashSet<Phase>();
            allPhases.addAll(Arrays.asList(Phase.values()));
        }

        @Override
        protected void doTasks(String dataName) {
        }

        @Override
        protected void summarizeAndDisplay() {
            if (allOrOne == StatsDialog.AllOrOne.EACH) {

                for (String dataName : dataNames) {
                    HashSet<String> tempDataNamesList= new HashSet<String>();
                    tempDataNamesList.add(dataName);

//                    Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                    chartDisplay.setName(dataName);
                    chartDisplay.setTitle(dataName + ": Path Hypothesis display");
                    chartDisplay.display(tempDataNamesList);
                    consoleDisplay.display(tempDataNamesList);

                }
            } else {

//                Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                chartDisplay.setTitle("Path Hypothesis display");
                chartDisplay.display(dataNames);
                consoleDisplay.display(dataNames);
            }
        }

    }


}
