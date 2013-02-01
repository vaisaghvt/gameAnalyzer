package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
import stats.StatisticChoice;
import stats.chartdisplays.DoorFrequencyChartDisplay;
import stats.chartdisplays.VertexChartDisplay;
import stats.consoledisplays.DoorFrequencyConsoleDisplay;
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
public class DoorFrequencyStatisticHandler extends StatisticsHandler {



    public DoorFrequencyStatisticHandler() {
        super(new DoorFrequencyChartDisplay(),
                new DoorFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.DOOR_FREQUENCY;


        if (!dataNames.isEmpty()) {
            HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
            for (String dataName : dataNames) {
                System.out.println("Processing " + dataName + "...");
                dataNameDataMap.put(dataName, ((NetworkModel) NetworkModel.instance()).getEdgeDataFor(dataName));

            }
            HashMap<String, HashMap<String, Long>> data = summarizeDataForEdges(dataNameDataMap, phase);
            this.chartDisplay.setTitle(choice.toString()+":"+phase.toString());
            this.chartDisplay.display(data);
            this.consoleDisplay.display(data);

        }
        else {

            System.out.println("No Data Name selected!");
        }
    }

    private HashMap<String, HashMap<String, Long>> summarizeDataForEdges(HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap, Phase phase) {
        HashMap<String, HashMap<String, Long>> result = new HashMap<String, HashMap<String, Long>>();

        assert phase != null;
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, HashMap<String, Number>> dataForPerson = dataNameDataMap.get(dataName);

            HashMap<String, Number> firstFloorShort = dataForPerson.get("CorridorGBtoLibraryG");
            HashMap<String, Number> firstFloorLong = dataForPerson.get("CorridorGA2toLibraryG");
            HashMap<String, Number> secondFloorMain = dataForPerson.get("2CorrAMaintoLibrary2");
            HashMap<String, Number> secondFloorSide = dataForPerson.get("2CorrASidetoLibrary2");
            HashMap<String, Number> interLibrary = dataForPerson.get("Library2toLibraryG");

            long valueForShort;
            if (firstFloorShort != null) {

                valueForShort = firstFloorShort.get(phase.toString()) == null ? 0 : (firstFloorShort.get(phase.toString())).longValue();

            } else {
                valueForShort = 0;
            }
            long valueForLong;
            if (secondFloorMain != null)
                valueForLong = firstFloorLong.get(phase.toString()) == null ? 0 : (firstFloorLong.get(phase.toString())).longValue();
            else
                valueForLong = 0;
            long valueForSecondMain;
            if (secondFloorSide != null) {
                valueForSecondMain = secondFloorMain.get(phase.toString()) == null ? 0 : (secondFloorMain.get(phase.toString())).longValue();
            } else {
                valueForSecondMain = 0;
            }
            long valueForSecondSide;
            if (secondFloorSide != null) {
                valueForSecondSide = secondFloorSide.get(phase.toString()) == null ? 0 : (secondFloorSide.get(phase.toString())).longValue();
            } else {
                valueForSecondSide = 0;
            }

            long interLibraryValue;
            if (secondFloorSide != null) {
                interLibraryValue = interLibrary.get(phase.toString()) == null ? 0 : (interLibrary.get(phase.toString())).longValue();
            } else {
                interLibraryValue = 0;
            }

            HashMap<String, Long> tempMap = new HashMap<String, Long>();
            tempMap.put("firstFloorShort", valueForShort);
            tempMap.put("firstFloorLong", valueForLong);
            tempMap.put("secondFloorMain", valueForSecondMain);
            tempMap.put("secondFloorSide", valueForSecondSide);
            tempMap.put("interLibraryValue", interLibraryValue);
            result.put(dataName, tempMap);
        }
        return result;
    }
}
