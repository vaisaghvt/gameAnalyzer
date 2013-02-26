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

        if (!dataNames.isEmpty()) {


            createProgressBar();
            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne, aggregationType);
            task.addPropertyChangeListener(this);
            task.execute();


        } else {

            System.out.println("No Data Names selected!");
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
        Multiset<Long> data = HashMultiset.create();


        for (String roomName : result.keySet()) {
            data.add(result.get(roomName));
        }

        return data;
    }

    class GenerateRequiredDataTask extends SwingWorker<Void, Void> {
        private final Phase phase;
        private final Collection<String> dataNames;

        HashMap<String, HashMap<String, Long>> dataNameDataMap = new HashMap<String, HashMap<String, Long>>();

        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;
        private final HashMap<String, Integer> roomEdgeCountMapping;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            this.dataNames = dataNames;

            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
            synchronized (NetworkModel.instance()) {
                roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
            }

        }

        @Override
        public Void doInBackground() {


            setProgress(0);
            int size = dataNames.size();
            int i = 1;
            for (String dataName : dataNames) {

                taskOutput.append("Processing " + dataName + "...\n");
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


                setProgress((i * 100) / size);

                i++;

            }
            return null;

        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            frame.dispose();
            taskOutput.append("Done.");
            frame.dispose();

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
