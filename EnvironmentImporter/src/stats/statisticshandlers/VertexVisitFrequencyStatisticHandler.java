package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
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
public class VertexVisitFrequencyStatisticHandler extends StatisticsHandler<VertexConsoleDisplay, VertexChartDisplay> {


    public VertexVisitFrequencyStatisticHandler() {
        super(new VertexChartDisplay(),
                new VertexConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        final StatisticChoice choice = StatisticChoice.VERTEX_VISIT_FREQUENCY;
        if (!dataNames.isEmpty()) {

            HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();

            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, Number> temp = NetworkModel.instance().getVertexDataFor(dataName, choice, phase);
                    HashMap<String, Double> result = new HashMap<String, Double>();

                    for (String roomName : temp.keySet()) {
                        int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();

                        result.put(roomName, (double) value
                                / (double) numberOfEdges);
                    }

                    this.chartDisplay.setName(dataName);


                    this.chartDisplay.setTitle(dataName + ":" + choice.toString() + ":" + phase.toString());
                    this.chartDisplay.display(result);
                    this.consoleDisplay.display(result);

                }
            } else {
                HashMultimap<String, Double> result = HashMultimap.create();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    HashMap<String, Number> temp = NetworkModel.instance().getVertexDataFor(dataName, choice, phase);


                    for (String roomName : temp.keySet()) {
                        int numberOfEdges = roomEdgeCountMapping.get(roomName);
                        long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();


                        result.put(roomName, (double) value
                                / (double) numberOfEdges);
                    }


                }

                HashMap<String, Double> finalResult = aggregateData(result, aggregationType);
                System.out.println("Displaying Chart...");
                this.chartDisplay.setTitle(choice.toString() + ":" + phase.toString());
                this.chartDisplay.display(finalResult);
                this.consoleDisplay.display(finalResult);


            }


        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private HashMap<String, Double> aggregateData(HashMultimap<String, Double> data, StatsDialog.AggregationType aggregationType) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        for (String roomName : data.keySet()) {
            result.put(roomName, StatisticsHandler.aggregate(data.get(roomName), aggregationType));
        }
        return result;


    }


}
