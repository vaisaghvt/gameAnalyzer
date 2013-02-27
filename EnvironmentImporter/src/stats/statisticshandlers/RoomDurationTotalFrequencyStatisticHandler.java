package stats.statisticshandlers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.RoomDurationTotalFrequencyChartDisplay;
import stats.consoledisplays.RoomDurationTotalFrequencyConsoleDisplay;

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
public class RoomDurationTotalFrequencyStatisticHandler extends StatisticsHandler<RoomDurationTotalFrequencyConsoleDisplay, RoomDurationTotalFrequencyChartDisplay> {


    public RoomDurationTotalFrequencyStatisticHandler() {
        super(new RoomDurationTotalFrequencyChartDisplay(),
                new RoomDurationTotalFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne, aggregationType);
        super.actualGenerateAndDisplay(task);

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
        Multiset<Long> data = HashMultiset.create();


        for (String roomName : result.keySet()) {
            data.add(result.get(roomName));
        }

        return data;
    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;

        HashMap<String, HashMap<String, Long>> dataNameDataMap = new HashMap<String, HashMap<String, Long>>();

        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;
        private final HashMap<String, Integer> roomEdgeCountMapping;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);

            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
            synchronized (NetworkModel.instance()) {
                roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
            }

        }

        @Override
        protected void doTasks(String dataName) {
            HashMap<String, Number> temp;
            synchronized (NetworkModel.instance()) {
                temp = NetworkModel.instance().
                        getVertexDataFor(dataName, StatisticChoice.TIME_SPENT_PER_VERTEX, phase);
            }
            HashMap<String, Long> result = new HashMap<String, Long>();
            for (String roomName : temp.keySet()) {
                int numberOfEdges = roomEdgeCountMapping.get(roomName);
                long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();
                result.put(roomName, value / (numberOfEdges * 1000));
            }
            dataNameDataMap.put(dataName, result);
        }

        @Override
        protected void summarizeAndDisplay() {
            if (allOrOne == StatsDialog.AllOrOne.ALL) {
                Multiset<Long> dataResult = summarizeAll(dataNameDataMap);



                chartDisplay.setTitle(StatisticChoice.ROOM_DURATION_FREQUENCY.toString());
                chartDisplay.display(dataResult);
                consoleDisplay.display(dataResult);
            } else {
                for (String dataName : dataNames) {
                    Multiset<Long> dataResult = summarizeEach(dataNameDataMap.get(dataName));


                    chartDisplay.setName(dataName);
                    chartDisplay.setTitle(dataName + StatisticChoice.ROOM_DURATION_FREQUENCY.toString() + ":Total");
                    chartDisplay.display(dataResult);
                    consoleDisplay.display(dataResult);
                }
            }

        }
    }


}
