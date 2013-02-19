package stats.statisticshandlers;

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

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoomDurationTotalFrequencyStatisticHandler extends StatisticsHandler<RoomDurationTotalFrequencyConsoleDisplay, RoomDurationTotalFrequencyChartDisplay> {


    public RoomDurationTotalFrequencyStatisticHandler() {
        super(new RoomDurationTotalFrequencyChartDisplay(),
                new RoomDurationTotalFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {

        if (!dataNames.isEmpty()) {
            if (allOrOne == StatsDialog.AllOrOne.ALL) {
                HashMap<String, Integer> roomEdgeCountMapping = ((NetworkModel) NetworkModel.instance()).getEdgesForEachRoom();

                HashMap<String, HashMap<String, Long>> fullResult = new HashMap<String, HashMap<String, Long>>();

                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, Number> temp = ((NetworkModel) NetworkModel.instance()).
                            getVertexDataFor(dataName, StatisticChoice.TIME_SPENT_PER_VERTEX, phase);
                    HashMap<String, Long> result = new HashMap<String, Long>();
                    for (String roomName : temp.keySet()) {
                        int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();
                        result.put(roomName, value / (numberOfEdges * 1000));
                    }
                    fullResult.put(dataName, result);
                }

                Multiset<Long> dataResult = summarizeAll(fullResult);


                System.out.println("Displaying Chart...");
                this.chartDisplay.setTitle(StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                this.chartDisplay.display(dataResult);
                this.consoleDisplay.display(dataResult);
            } else {
                HashMap<String, Integer> roomEdgeCountMapping = ((NetworkModel) NetworkModel.instance()).getEdgesForEachRoom();


                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, Number> temp = ((NetworkModel) NetworkModel.instance()).
                            getVertexDataFor(dataName, StatisticChoice.TIME_SPENT_PER_VERTEX, phase);
                    HashMap<String, Long> result = new HashMap<String, Long>();
                    for (String roomName : temp.keySet()) {
                        int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();
                        result.put(roomName, value / (numberOfEdges * 1000));
                    }


                    Multiset<Long> dataResult = summarizeEach(result);


                    this.chartDisplay.setName(dataName);
                    this.chartDisplay.setTitle(dataName + StatisticChoice.ROOM_DURATION_FREQUENCY.toString() + ":Total");
                    this.chartDisplay.display(dataResult);
                    this.consoleDisplay.display(dataResult);
                }
            }

        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private Multiset<Long> summarizeAll(HashMap<String, HashMap<String, Long>> result) {
        Multiset<Long> data = HashMultiset.create();

        for (String dataName : result.keySet()) {
            HashMap<String, Long> tempResult = result.get(dataName);
            for (String roomName : tempResult.keySet()) {
                data.add(tempResult.get(roomName));
            }
        }
        return data;
    }

    private Multiset<Long> summarizeEach(HashMap<String, Long> result) {
        Multiset<Long> data= HashMultiset.create();



        for (String roomName: result.keySet()){
            data.add(result.get(roomName));
        }

        return data;
    }


}
