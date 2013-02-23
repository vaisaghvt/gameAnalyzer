package stats.statisticshandlers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
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
        final StatisticChoice choice = StatisticChoice.VERTEX_VISIT_FREQUENCY;
        if (!dataNames.isEmpty()) {
            if (allOrOne == StatsDialog.AllOrOne.ALL) {
                HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    dataNameDataMap.put(dataName, NetworkModel.instance().getVertexDataFor(dataName, choice, phase));

                }

                Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                System.out.println("Displaying Chart...");
                this.chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
                this.chartDisplay.display(data);
                this.consoleDisplay.display(data);
            } else {
                HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    dataNameDataMap.put(dataName, NetworkModel.instance().getVertexDataFor(dataName, choice, phase));

                    Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                    this.chartDisplay.setName(dataName);
                    this.chartDisplay.setTitle(dataName + ":" + choice.toString() + " :" + phase.toString());
                    this.chartDisplay.display(data);
                    this.consoleDisplay.display(data);
                    dataNameDataMap.clear();
                }


            }
        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private Multiset<Double> summarizeData(HashMap<String, HashMap<String, Number>> dataNameDataMap, Phase phase) {
        Multiset<Double> result = HashMultiset.create();
        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
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


}
