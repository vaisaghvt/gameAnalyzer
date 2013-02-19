package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.RoomDurationTotalFrequencyChartDisplay;
import stats.consoledisplays.RoomDurationTotalFrequencyConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoomDurationBreakupFrequencyStatisticHandler extends StatisticsHandler<RoomDurationTotalFrequencyConsoleDisplay, RoomDurationTotalFrequencyChartDisplay> {


    public RoomDurationBreakupFrequencyStatisticHandler() {
        super(new RoomDurationTotalFrequencyChartDisplay(),
                new RoomDurationTotalFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {

        if (!dataNames.isEmpty()) {
            if (allOrOne == StatsDialog.AllOrOne.ALL) {
                HashMultimap<String, Long> result = HashMultimap.create();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, HashMultimap<String, Long>> temp = NetworkModel.instance().
                            getTimeSpentPerVisit(dataName);

                    for (String roomName : temp.keySet()) {
//                    int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        if (temp.get(roomName).get(phase.toString()) != null) {
                            result.putAll(roomName, temp.get(roomName).get(phase.toString()));
                        }
                    }

                }

                Multiset<Long> dataResult = summarize(result);


                System.out.println("Displaying Chart...");
                this.chartDisplay.setTitle(StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                this.chartDisplay.display(dataResult);
                this.consoleDisplay.display(dataResult);
            } else {
                for (String dataName : dataNames) {
                    HashMultimap<String, Long> result = HashMultimap.create();
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, HashMultimap<String, Long>> temp = NetworkModel.instance().
                            getTimeSpentPerVisit(dataName);

                    for (String roomName : temp.keySet()) {
//                    int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        if (temp.get(roomName).get(phase.toString()) != null) {
                            result.putAll(roomName, temp.get(roomName).get(phase.toString()));
                        }
                    }
                    Multiset<Long> dataResult = summarize(result);


                    this.chartDisplay.setName(dataName);

                    this.chartDisplay.setTitle(dataName + StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                    this.chartDisplay.display(dataResult);
                    this.consoleDisplay.display(dataResult);

                }

            }
        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private Multiset<Long> summarize(HashMultimap<String, Long> result) {
        Multiset<Long> data = HashMultiset.create();


//            HashMultimap<String, Long> tempResult = result.get(dataName);
        for (String roomName : result.keySet()) {
            Set<Long> durationsForRoom = result.get(roomName);
            for (Long duration : durationsForRoom) {
                data.add(duration / 1000);
            }
        }

        return data;
    }
}
