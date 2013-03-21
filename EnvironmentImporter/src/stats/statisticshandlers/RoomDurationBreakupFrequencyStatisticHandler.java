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

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne);
        super.actualGenerateAndDisplay(task);
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


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;

        HashMap<String, HashMultimap<String, Long>> dataNameDataMap = new HashMap<String, HashMultimap<String, Long>>();

        private final StatsDialog.AllOrOne allOrOne;
        HashMultimap<String, Long> result;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne) {
            super(dataNames);

            this.phase = phase;
            this.allOrOne = allOrOne;
            result = HashMultimap.create();
        }

        @Override
        protected void doTasks(String dataName) {
            HashMap<String, HashMultimap<String, Long>> temp = NetworkModel.instance().
                    getTimeSpentPerVisit(dataName);

            for (String roomName : temp.keySet()) {
                if (temp.get(roomName).get(phase.toString()) != null) {
                    result.putAll(roomName, temp.get(roomName).get(phase.toString()));
                }
            }
            dataNameDataMap.put(dataName, result);
        }

        @Override
        protected void summarizeAndDisplay() {
            HashMultimap<String, Long> result = HashMultimap.create();
            if (allOrOne == StatsDialog.AllOrOne.ALL) {

                for (String dataName : dataNameDataMap.keySet()) {
                    HashMultimap<String, Long> tempResult = dataNameDataMap.get(dataName);
                    for (String roomName : tempResult.keySet()) {
                        result.putAll(roomName, tempResult.get(roomName));
                    }

                }

                Multiset<Long> dataResult = summarize(result);
                chartDisplay.setTitle(StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                chartDisplay.display(dataResult);
                consoleDisplay.display(dataResult);
            } else {
                for (String dataName : dataNameDataMap.keySet()) {
                    HashMultimap<String, Long> tempResult = dataNameDataMap.get(dataName);
                    for (String roomName : tempResult.keySet()) {
                        result.putAll(roomName, tempResult.get(roomName));
                    }
                    Multiset<Long> dataResult = summarize(result);
                    chartDisplay.setTitle(dataName + StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                    chartDisplay.display(dataResult);
                    consoleDisplay.display(dataResult);
                    result.clear();

                }

            }
        }
    }
}
