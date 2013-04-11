package stats.statisticshandlers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.CompleteGraph;
import stats.StatisticChoice;
import stats.chartdisplays.VertexVisitChartDisplay;
import stats.consoledisplays.VertexVisitConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexVisitCountStatisticHandler extends StatisticsHandler<VertexVisitConsoleDisplay, VertexVisitChartDisplay> {


    public VertexVisitCountStatisticHandler() {
        super(new VertexVisitChartDisplay(),
                new VertexVisitConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.VERTEX_VISIT_FREQUENCY, phase, allOrOne);
        super.actualGenerateAndDisplay(task);


    }

    private Multiset<Double> summarizeData(HashMap<String, HashMap<String, Number>> dataNameDataMap, Phase phase) {
        Multiset<Double> result = HashMultiset.create();
        HashMap<String, Integer> roomEdgeCountMapping = CompleteGraph.instance().getEdgesForEachRoom();
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, Number> dataForPerson = dataNameDataMap.get(dataName);


            for (String roomName : dataForPerson.keySet()) {
                if (dataForPerson.get(roomName) != null) {
                    long numberOfVisits = dataForPerson.get(roomName).longValue();
                    long numberOfEdges = roomEdgeCountMapping.get(roomName);
                    double normalizedNumberOfVisits = (double) numberOfVisits / (double) numberOfEdges;
                    result.add(normalizedNumberOfVisits);
                }
            }
        }
        return result;
    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
        private final StatsDialog.AllOrOne allOrOne;


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne) {
            super(dataNames);

            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
        }

        @Override
        protected void doTasks(String dataName) {

            dataNameDataMap.put(dataName, NetworkModel.instance().getVertexDataFor(dataName, choice, phase));

        }

        @Override
        protected void summarizeAndDisplay() {
            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                HashMap<String, HashMap<String, Number>> tempDataNameDataMap = new HashMap<String, HashMap<String, Number>>();
                for (String dataName : dataNames) {
                    tempDataNameDataMap.clear();
                    tempDataNameDataMap.put(dataName, dataNameDataMap.get(dataName));

                    Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                    chartDisplay.setName(dataName);
                    chartDisplay.setTitle(dataName + ":" + choice.toString() + " :" + phase.toString());
                    chartDisplay.display(data);
                    consoleDisplay.display(data);

                }
            } else {

                Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
                chartDisplay.display(data);
                consoleDisplay.display(data);
            }
        }

    }


}
