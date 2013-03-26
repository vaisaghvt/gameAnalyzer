package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import randomwalk.RandomWalk;
import stats.StatisticChoice;
import stats.chartdisplays.DoorUseChartDisplay;
import stats.consoledisplays.DoorUseConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoorUseFrequencyStatisticHandler extends StatisticsHandler<DoorUseConsoleDisplay, DoorUseChartDisplay> {

    public DoorUseFrequencyStatisticHandler() {
        super(new DoorUseChartDisplay(),
                new DoorUseConsoleDisplay()
        );
    }

    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.VERTEX_VISIT_FREQUENCY, phase, allOrOne, aggregationType);
        super.actualGenerateAndDisplay(task);

    }


    private HashMap<String, Double> normalizeResult(HashMap<String, Double> personData) {

        HashMap<String, Double> result = new HashMap<String, Double>();
        HashMap<String, Number> randomWalkData = RandomWalk.calculateAverageDoorUseFrequency();
        int NRandom = 0, NPerson = 0;
        for (String room : randomWalkData.keySet()) {
            NRandom += randomWalkData.get(room).intValue();
        }
        for (String room : personData.keySet()) {
            NPerson += personData.get(room).intValue();
        }

        for (String room : personData.keySet()) {
            double originalValueForRoom = personData.get(room).doubleValue();
            double scaledValue = (originalValueForRoom * NRandom) / NPerson;
            double normalizedValue = scaledValue / randomWalkData.get(room).doubleValue();
            result.put(room, normalizedValue);
        }

        return result;
    }

    private HashMap<String, Double> aggregateData(HashMultimap<String, Double> data, StatsDialog.AggregationType aggregationType) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        for (String roomName : data.keySet()) {
            result.put(roomName, StatisticsHandler.aggregate(data.get(roomName), aggregationType));
        }
        return result;


    }


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;
        private Map<String, HashMap<String, Double>> nameToResultMapping = new HashMap<String, HashMap<String, Double>>();


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
        }

        @Override
        protected void doTasks(String dataName) {
            HashMap<String, Integer> temp;

            temp = NetworkModel.instance().getEdgeDataFor(dataName, phase);


            HashMap<String, Double> result = new HashMap<String, Double>();

            for (String roomName : temp.keySet()) {
                double value = temp.get(roomName) == null ? 0 : temp.get(roomName).doubleValue();

                result.put(roomName, value);

            }
            result = normalizeResult(result);
            nameToResultMapping.put(dataName, result);
        }

        @Override
        protected void summarizeAndDisplay() {


            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for (String dataName : nameToResultMapping.keySet()) {
                    HashMap<String, Double> result = nameToResultMapping.get(dataName);
                    chartDisplay.setTitle(dataName + ": Door use :" + phase.toString());
                    chartDisplay.display(result);
                    consoleDisplay.display(result);
                }
            } else {

                HashMultimap<String, Double> resultGrouped = HashMultimap.create();
                for (String dataName : nameToResultMapping.keySet()) {

                    for (String roomName : nameToResultMapping.get(dataName).keySet()) {
                        resultGrouped.put(roomName, nameToResultMapping.get(dataName).get(roomName));
                    }
                }

                HashMap<String, Double> finalResult = aggregateData(resultGrouped, type);

                chartDisplay.setTitle("Door use:" + phase.toString());
                chartDisplay.display(finalResult);
                consoleDisplay.display(finalResult);
            }

        }
    }

}
