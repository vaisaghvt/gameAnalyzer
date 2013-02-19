package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.SignificantDoorFrequencyChartDisplay;
import stats.consoledisplays.SignificantDoorFrequencyConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SignificantDoorFrequencyStatisticHandler extends StatisticsHandler<SignificantDoorFrequencyConsoleDisplay, SignificantDoorFrequencyChartDisplay> {


    public SignificantDoorFrequencyStatisticHandler() {
        super(new SignificantDoorFrequencyChartDisplay(),
                new SignificantDoorFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        final StatisticChoice choice = StatisticChoice.DOOR_FREQUENCY;

        if (!dataNames.isEmpty()) {

            if (allOrOne == StatsDialog.AllOrOne.EACH) {

                HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    dataNameDataMap.put(dataName, NetworkModel.instance().getEdgeDataFor(dataName));

                }

                HashMap<String, HashMap<String, Number>> data = summarizeData(dataNameDataMap, phase, null);


                this.chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
                this.chartDisplay.display(data);
                this.consoleDisplay.display(data);
            } else {
                HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
                for (String dataName : dataNames) {
                    System.out.println("Processing " + dataName + "...");
                    dataNameDataMap.put(dataName, NetworkModel.instance().getEdgeDataFor(dataName));


                }

                HashMap<String, HashMap<String, Number>> data = summarizeData(dataNameDataMap, phase, aggregationType);


                this.chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
                this.chartDisplay.display(data);
                this.consoleDisplay.display(data);
                dataNameDataMap.clear();


            }

        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private HashMap<String, HashMap<String, Number>> summarizeData(HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap,
                                                                   Phase phase, StatsDialog.AggregationType aggregationType) {
        HashMap<String, HashMap<String, Number>> result = new HashMap<String, HashMap<String, Number>>();

        String summaryName = "overall";
        if (aggregationType != null) {
            HashMap<String, Number> initialMap = new HashMap<String, Number>();
            initialMap.put("firstFloorShort", 0);
            initialMap.put("firstFloorLong", 0);
            initialMap.put("secondFloorMain", 0);
            initialMap.put("secondFloorSide", 0);
            initialMap.put("interLibrary", 0);
            result.put(summaryName, initialMap);
        }
        int n = 1;
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, HashMap<String, Number>> dataForPerson = dataNameDataMap.get(dataName);


            HashMap<String, Number> firstFloorShort = dataForPerson.get("CorridorGBtoLibraryG");
            HashMap<String, Number> firstFloorLong = dataForPerson.get("CorridorGA2toLibraryG");
            HashMap<String, Number> secondFloorMain = dataForPerson.get("2CorrAMaintoLibrary2");
            HashMap<String, Number> secondFloorSide = dataForPerson.get("2CorrASidetoLibrary2");
            HashMap<String, Number> interLibrary = dataForPerson.get("Library2toLibraryG");


            HashMap<String, Number> tempMap = result.get(summaryName);
            if (aggregationType != null) {
                tempMap.put("firstFloorShort", aggregate(tempMap.get("firstFloorShort").doubleValue(), getDoubleValue(firstFloorShort, phase.toString()), aggregationType, n));
                tempMap.put("firstFloorLong", aggregate(tempMap.get("firstFloorLong").doubleValue(), getDoubleValue(firstFloorLong, phase.toString()), aggregationType, n));
                tempMap.put("secondFloorMain", aggregate(tempMap.get("secondFloorMain").doubleValue(), getDoubleValue(secondFloorMain, phase.toString()), aggregationType, n));
                tempMap.put("secondFloorSide", aggregate(tempMap.get("secondFloorSide").doubleValue(), getDoubleValue(secondFloorSide, phase.toString()), aggregationType, n));
                tempMap.put("interLibrary", aggregate(tempMap.get("interLibrary").doubleValue(), getDoubleValue(interLibrary, phase.toString()), aggregationType, n));

                result.put(summaryName, tempMap);
            } else {
                tempMap.put("firstFloorShort",getDoubleValue(firstFloorShort, phase.toString()));
                tempMap.put("firstFloorLong", getDoubleValue(firstFloorLong, phase.toString()));
                tempMap.put("secondFloorMain", getDoubleValue(secondFloorMain, phase.toString()));
                tempMap.put("secondFloorSide", getDoubleValue(secondFloorSide, phase.toString()));
                tempMap.put("interLibrary", getDoubleValue(interLibrary, phase.toString()));

                result.put(dataName, tempMap);
            }
            n++;
        }
        return result;
    }

    private double getDoubleValue(HashMap<String, Number> map, String key) {
        return (map==null || !map.containsKey(key))? 0: map.get(key).doubleValue();
    }


    private Double aggregate(Double v1, double v2, StatsDialog.AggregationType aggregationType, int n) {
        switch (aggregationType) {

            case SUM:
                return v1 + v2;

            case MEAN:
                return (v1 * (n - 1) + v2) / n;
            case MIN:
                return Math.min(v1, v2);
            case MAX:
                return Math.max(v1, v2);
            default:
                return null;
        }
    }


}
