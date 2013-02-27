package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.DistanceTimeChartDisplay;
import stats.consoledisplays.DistanceTimeConsoleDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceTimeExplorationStatisticHandler extends StatisticsHandler<DistanceTimeConsoleDisplay, DistanceTimeChartDisplay> {


    public DistanceTimeExplorationStatisticHandler() {
        super(new DistanceTimeChartDisplay(),
                new DistanceTimeConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.DISTANCE_TIME_EXPLORATION_STATISTIC);
        super.actualGenerateAndDisplay(task);



    }

    private HashMap<String, HashMap<String, Double>> summarizeDistanceTime(HashMap<String, Double> distanceTraveled, HashMap<String, Long> timeTaken) {
        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
        System.out.println(distanceTraveled.keySet().size() + "," + timeTaken.keySet().size());
        for (String dataName : distanceTraveled.keySet()) {
            HashMap<String, Double> resultPart = new HashMap<String, Double>();
            resultPart.put("distance", distanceTraveled.get(dataName));
            resultPart.put("time", timeTaken.get(dataName).doubleValue());


            result.put(dataName, resultPart);

        }
        return result;

    }


    class GenerateRequiredDataTask extends AbstractTask {

        private HashMap<String, Double> distanceTraveled;
        private HashMap<String, Long> timeTaken;
        private final StatisticChoice choice;

        public GenerateRequiredDataTask(Collection<String> dataNames,StatisticChoice choice) {
            super(dataNames);
            this.choice = choice;
            this.distanceTraveled = new HashMap<String, Double>();
            this.timeTaken = new HashMap<String, Long>();
        }

        @Override
        protected void doTasks(String dataName) {
            synchronized (NetworkModel.instance()) {
                distanceTraveled.put(dataName, NetworkModel.instance().getDistanceTraveledExploration(dataName));
                timeTaken.put(dataName, NetworkModel.instance().getTimeTraveledExploration(dataName));
            }
        }

        @Override
        protected void summarizeAndDisplay() {
            HashMap<String, HashMap<String, Double>> summary = summarizeDistanceTime(distanceTraveled, timeTaken);
            chartDisplay.setTitle(choice.toString());

            chartDisplay.display(summary);
            consoleDisplay.display(summary);
        }


    }


}
