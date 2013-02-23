package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.PathHopChartDisplay;
import stats.consoledisplays.PathHopConsoleDisplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathHopStatisticHandler extends StatisticsHandler<PathHopConsoleDisplay, PathHopChartDisplay> {


    public PathHopStatisticHandler() {
        super(new PathHopChartDisplay(),
                new PathHopConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.PATH_HOP_RELATIONSHIP;
        HashMultimap<String, String> pathData = NetworkModel.instance().getPathDataFor(dataNames, phase);
        HashMap<String, HashMap<String, Integer>> hopsPerPhasePerPerson = NetworkModel.instance().getHopDataFor(dataNames);

        HashMap<String, HashMap<String, String>> data = convertToActualData(pathData, hopsPerPhasePerPerson);

        this.chartDisplay.setTitle(choice.toString() + phase.toString());
        this.chartDisplay.setPhase(phase);
        this.chartDisplay.display(data);
        this.consoleDisplay.display(data);


    }


    private HashMap<String, HashMap<String, String>> convertToActualData(HashMultimap<String, String> pathData, HashMap<String, HashMap<String, Integer>> hopsPerPhasePerPerson) {
        Phase phaseToBeConsidered = Phase.EXPLORATION;
        ArrayList<String> pathNames = new ArrayList<String>();
        pathNames.addAll(pathData.keySet());
        HashMap<String, HashMap<String, String>> dataForPerson = new HashMap<String, HashMap<String, String>>();


        for (String name : hopsPerPhasePerPerson.keySet()) {
            HashMap<String, Integer> hopsPerPhase = hopsPerPhasePerPerson.get(name);

            HashMap<String, String> result = dataForPerson.get(name);
            if (result == null) {
                result = new HashMap<String, String>();
            }
            Integer hopsForSelectedPhase = hopsPerPhase.get(phaseToBeConsidered.toString());
            Integer number = hopsForSelectedPhase != null ? hopsForSelectedPhase : 0;
            result.put("hops", number.toString());


            for (int j = 0; j < pathNames.size(); j++) {
                if (pathData.containsEntry(pathNames.get(j), name)) {

                    if (pathNames.get(j).equals("Shortest")) {

                        result.put("path", "shortest");
                    } else if (pathNames.get(j).equals("Path2")
                            || pathNames.get(j).equals("Path3")
                            || pathNames.get(j).equals("Path4")) {
                        result.put("path", "normal");
                    } else if (pathNames.get(j).equals("lost")) {
                        result.put("path", "lost");
                    } else {
                        result.put("path", "topFloor");
                    }


                }
            }
            dataForPerson.put(name, result);
        }
        return dataForPerson;
    }


}
