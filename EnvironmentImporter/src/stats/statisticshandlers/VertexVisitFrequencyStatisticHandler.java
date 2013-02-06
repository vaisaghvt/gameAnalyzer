package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
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
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.VERTEX_VISIT_FREQUENCY;
        if (!dataNames.isEmpty()) {

            HashMap<String, Integer> roomEdgeCountMapping = ((NetworkModel) NetworkModel.instance()).getEdgesForEachRoom();
            for (String dataName : dataNames) {
                System.out.println("Processing " + dataName + "...");
                HashMap<String, HashMap<String, Number>> temp = ((NetworkModel) NetworkModel.instance()).getVertexDataFor(dataName, choice);
                HashMap<String, Long> result = new HashMap<String, Long>();

                for (String roomName : temp.keySet()) {
                    int numberOfEdges = roomEdgeCountMapping.get(roomName);
                    long value = temp.get(roomName).get(phase.toString()) == null ? 0 : temp.get(roomName).get(phase.toString()).longValue();
                    result.put(roomName, value / numberOfEdges);
                }

                System.out.println("Displaying Chart...");
                this.chartDisplay.setTitle(dataName+":"+choice.toString());
                this.chartDisplay.display(result);
                this.consoleDisplay.display(result);

            }




//            else {
//                HashMap<String, Long> normalizedRoomVisitFrequencies = findNormalizedRoomVisitFrequencies(dataNameDataMap);
//                displayBarChart(normalizedRoomVisitFrequencies);
//            }
        } else {

            System.out.println("No Data Name selected!");
        }
    }


}
