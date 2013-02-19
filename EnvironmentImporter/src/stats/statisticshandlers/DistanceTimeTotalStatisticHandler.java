package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.DistanceTimeChartDisplay;
import stats.consoledisplays.DistanceTimeConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceTimeTotalStatisticHandler extends StatisticsHandler<DistanceTimeConsoleDisplay, DistanceTimeChartDisplay> {



    public DistanceTimeTotalStatisticHandler() {
        super(new DistanceTimeChartDisplay(),
                new DistanceTimeConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.DISTANCE_TIME_TOTAL_STATISTIC;


        HashMap<String, Double> distanceTraveled =  ((NetworkModel) NetworkModel.instance()).getDistanceTraveledTotal(dataNames);
        HashMap<String, Long> timeTaken =  ((NetworkModel) NetworkModel.instance()).getTimeTraveledTotal(dataNames);

        HashMap<String, HashMap<String, Double>> summary = summarizeDistanceTime(distanceTraveled, timeTaken);
        this.chartDisplay.setTitle(choice.toString());

        this.chartDisplay.display(summary);
        this.consoleDisplay.display(summary);

    }

    private HashMap<String, HashMap<String, Double>> summarizeDistanceTime(HashMap<String, Double> distanceTraveled, HashMap<String, Long> timeTaken) {
        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
        System.out.println(distanceTraveled.keySet().size()+","+timeTaken.keySet().size());
        for(String dataName:distanceTraveled.keySet()){
            HashMap<String, Double> resultPart = new HashMap<String, Double>();
            resultPart.put("distance", distanceTraveled.get(dataName));
            resultPart.put("time", timeTaken.get(dataName).doubleValue());


            result.put(dataName,resultPart );

        }
        return result;

    }


}
