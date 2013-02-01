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
public class VertexVisitDurationStatisticHandler extends StatisticsHandler {



    public VertexVisitDurationStatisticHandler() {
        super(new VertexChartDisplay(),
                new VertexConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.TIME_SPENT_PER_VERTEX;
        if (!dataNames.isEmpty()) {
            HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap = new HashMap<String, HashMap<String, HashMap<String, Number>>>();
            for (String dataName : dataNames) {
                System.out.println("Processing " + dataName + "...");
                dataNameDataMap.put(dataName, ((NetworkModel) NetworkModel.instance()).getVertexDataFor(dataName, choice));

            }

            HashMap<String, HashMap<String, Long>> data = summarizeData(dataNameDataMap, phase);

            System.out.println("Displaying Chart...");
            this.chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
            this.chartDisplay.display(data);
            this.consoleDisplay.display(data);

//            else {
//                HashMap<String, Long> normalizedRoomVisitFrequencies = findNormalizedRoomVisitFrequencies(dataNameDataMap);
//                displayBarChart(normalizedRoomVisitFrequencies);
//            }
        } else {

            System.out.println("No Data Name selected!");
        }
    }

    private HashMap<String, HashMap<String, Long>> summarizeData(HashMap<String, HashMap<String, HashMap<String, Number>>> dataNameDataMap, Phase phase) {
        HashMap<String, HashMap<String, Long>> result = new HashMap<String, HashMap<String, Long>>();

        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, HashMap<String, Number>> dataForPerson = dataNameDataMap.get(dataName);

            HashMap<String, Number> library2Data = dataForPerson.get("Library2");
            HashMap<String, Number> libraryGData = dataForPerson.get("LibraryG");
            HashMap<String, Number> saunaData = dataForPerson.get("Sauna");
            HashMap<String, Number> galleryData = dataForPerson.get("Gallery");
            HashMap<String, Number> startData = dataForPerson.get("StartingRoom");
            HashMap<String, Integer> roomEdgeCountMapping = ((NetworkModel) NetworkModel.instance()).getEdgesForEachRoom();
            long valueForLibrary;
            if (library2Data != null) {
                long library2Value = library2Data.get(phase.toString()) == null ? 0 : (library2Data.get(phase.toString())).longValue();
                long libraryGValue = (libraryGData.get(phase.toString()) == null ? 0 : (libraryGData.get(phase.toString())).longValue());
                valueForLibrary = (library2Value/ roomEdgeCountMapping.get("Library2")) + (libraryGValue/ roomEdgeCountMapping.get("LibraryG"));
            } else {
                valueForLibrary = 0;
            }

            long valueForSauna;
            if (saunaData != null)
                valueForSauna = saunaData.get(phase.toString()) == null ? 0 : (saunaData.get(phase.toString())).longValue();
            else
                valueForSauna = 0;
            valueForSauna/= roomEdgeCountMapping.get("Sauna") ;

            long valueForGallery;
            if (galleryData != null) {
                valueForGallery = galleryData.get(phase.toString()) == null ? 0 : (galleryData.get(phase.toString())).longValue();
            } else {
                valueForGallery = 0;
            }
            valueForGallery/= roomEdgeCountMapping.get("Gallery") ;

            long valueForStart;
            if (startData != null) {
                valueForStart = startData.get(phase.toString()) == null ? 0 : startData.get(phase.toString()).longValue();
            } else {
                valueForStart = 0;
            }
            valueForStart/= roomEdgeCountMapping.get("StartingRoom") ;

            HashMap<String, Long> tempMap = new HashMap<String, Long>();
            tempMap.put("Library", valueForLibrary);
            tempMap.put("PictureGallery", valueForGallery);
            tempMap.put("Sauna", valueForSauna);
            tempMap.put("StartingRoom", valueForStart);
            result.put(dataName, tempMap);
        }
        return result;
    }
}
