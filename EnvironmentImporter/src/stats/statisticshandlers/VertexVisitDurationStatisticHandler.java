package stats.statisticshandlers;

import com.google.common.collect.ArrayListMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.CompleteGraph;
import stats.StatisticChoice;
import stats.chartdisplays.VertexChartDisplay;
import stats.consoledisplays.VertexConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexVisitDurationStatisticHandler extends StatisticsHandler<VertexConsoleDisplay, VertexChartDisplay> {


    public VertexVisitDurationStatisticHandler() {
        super(new VertexChartDisplay(),
                new VertexConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.TIME_SPENT_PER_VERTEX, phase, allOrOne, aggregationType);
        super.actualGenerateAndDisplay(task);

    }

    private HashMap<String, Double> aggregateData(ArrayListMultimap<String, Double> data, StatsDialog.AggregationType aggregationType) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        for (String roomName : data.keySet()) {
            result.put(roomName, StatisticsHandler.aggregate(data.get(roomName), aggregationType));
        }
        return result;


    }


    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        private HashMap<String, HashMap<String, Double>> nameToResultMapping = new HashMap<String, HashMap<String, Double>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;
        private final HashMap<String, Integer> roomEdgeCountMapping;

        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);

            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
            this.roomEdgeCountMapping = CompleteGraph.instance().getEdgesForEachRoom();
        }

        @Override
        protected void doTasks(String dataName) {
            HashMap<String, Number> temp;

            temp = NetworkModel.instance().getVertexDataFor(dataName, choice, phase);

            HashMap<String, Double> result = new HashMap<String, Double>();

            for (String roomName : temp.keySet()) {
                int numberOfEdges = roomEdgeCountMapping.get(roomName);
                long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();

                result.put(roomName, (double) value
                        / (double) numberOfEdges);
            }
            nameToResultMapping.put(dataName, result);
        }

        @Override
        protected void summarizeAndDisplay() {
            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for (String dataName : nameToResultMapping.keySet()) {
                    HashMap<String, Double> result = nameToResultMapping.get(dataName);
                    chartDisplay.setTitle(dataName + ":" + choice.toString() + ":" + phase.toString());
                    chartDisplay.display(result);
                    consoleDisplay.display(result);
                }
            } else {

                ArrayListMultimap<String, Double> resultGrouped = ArrayListMultimap.create();
                for (String dataName : nameToResultMapping.keySet()) {

                    for (String roomName : nameToResultMapping.get(dataName).keySet()) {
                        resultGrouped.put(roomName, nameToResultMapping.get(dataName).get(roomName));
                    }
                }

                HashMap<String, Double> finalResult = VertexVisitDurationStatisticHandler.this.aggregateData(resultGrouped, type);

                chartDisplay.setTitle(choice.toString() + ":" + phase.toString());
                chartDisplay.display(finalResult);
                consoleDisplay.display(finalResult);
            }
        }


    }
}
