package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.RoomRepetitionChartDisplay;
import stats.consoledisplays.RoomRepetitionConsoleDisplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoomRememberedStatisticHandler extends StatisticsHandler<RoomRepetitionConsoleDisplay, RoomRepetitionChartDisplay> {


    public RoomRememberedStatisticHandler() {
        super(new RoomRepetitionChartDisplay(),
                new RoomRepetitionConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.ROOM_REMEMBERED;
        if (!dataNames.isEmpty()) {
            HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
            for (String dataName : dataNames) {
                System.out.println("Processing " + dataName + "...");
                dataNameDataMap.put(dataName, ((NetworkModel) NetworkModel.instance()).getVertexDataFor(dataName, StatisticChoice.TIME_SPENT_PER_VERTEX, phase));

            }
            HashMap<String, HashMap<String, Long>> roomVisitData = summarizeData(dataNameDataMap, phase);
            HashMultimap<String, String> rememberedRooms = getRememberedRooms();

            this.chartDisplay.setTitle(choice.toString());


            HashMap<String, HashMap<String, HashMap<String, String>>> summarizedData = summarizeToHashMap(roomVisitData, rememberedRooms);
            this.chartDisplay.display(summarizedData);
            this.consoleDisplay.display(summarizedData);
        } else {
            System.out.println("Nothing selected!");
        }
    }

    private HashMap<String, HashMap<String, Long>> summarizeData(HashMap<String, HashMap<String, Number>> dataNameDataMap, Phase phase) {
        HashMap<String, HashMap<String, Long>> result = new HashMap<String, HashMap<String, Long>>();

        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, Number> dataForPerson = dataNameDataMap.get(dataName);
            long valueForLibrary;
            long library2Value = dataForPerson.get("Library2") == null ? 0 : dataForPerson.get("Library2").longValue();
            long libraryGValue = dataForPerson.get("LibraryG") == null ? 0 : dataForPerson.get("LibraryG").longValue();
            valueForLibrary = library2Value + libraryGValue;

            long valueForSauna;
            valueForSauna = dataForPerson.get("Sauna") == null ? 0 : dataForPerson.get("Sauna").longValue();
            long valueForGallery;
            valueForGallery = dataForPerson.get("Gallery") == null ? 0 : dataForPerson.get("Gallery").longValue();
            long valueForStart;
            valueForStart = dataForPerson.get("StartingRoom") == null ? 0 : dataForPerson.get("StartingRoom").longValue();
            HashMap<String, Long> tempMap = new HashMap<String, Long>();
            tempMap.put("Library", valueForLibrary);
            tempMap.put("PictureGallery", valueForGallery);
            tempMap.put("Sauna", valueForSauna);
            tempMap.put("StartingPosition", valueForStart);
            result.put(dataName, tempMap);
        }
        return result;
    }

    private HashMultimap<String, String> getRememberedRooms() {
        Scanner sc = null;
        HashMultimap<String, String> results = HashMultimap.create();
        try {
            sc = new Scanner(new File("C:\\Users\\vaisagh\\Documents\\Name rememberList.txt"));


            while (sc.hasNext()) {
                String readLine = sc.nextLine();
                String[] parts = readLine.split("\t");
                String name = parts[0];
                String rememberedRooms = parts[1];
                rememberedRooms = rememberedRooms.replaceAll("\"", "").replaceAll(" ", "").trim();
                String[] roomsAsArray = rememberedRooms.split(",");
                for (String room : roomsAsArray) {
                    results.put(name, room);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return results;
    }

    private HashMap<String, HashMap<String, HashMap<String, String>>> summarizeToHashMap(HashMap<String, HashMap<String, Long>> roomVisitData,
                                                                                         HashMultimap<String, String> rememberedRooms) {


        List<String> significantRooms = new ArrayList<String>();
        significantRooms.add("PictureGallery");
        significantRooms.add("Library");
        significantRooms.add("StartingPosition");
        significantRooms.add("Sauna");

        HashMap<String, HashMap<String, HashMap<String, String>>> result = new HashMap<String, HashMap<String, HashMap<String, String>>>();

        for (String dataName : rememberedRooms.keySet()) {
            HashMap<String, Long> roomVisitFrequencyForName = roomVisitData.get(dataName);
            Set<String> rememberedRoomsForName = rememberedRooms.get(dataName);
            HashMap<String, HashMap<String, String>> mapForName = new HashMap<String, HashMap<String, String>>();
            if (rememberedRoomsForName != null && roomVisitFrequencyForName != null) {
                for (String significantRoom : significantRooms) {
                    HashMap<String, String> mapForRoom = mapForName.get(significantRoom);
                    if (mapForRoom == null)
                        mapForRoom = new HashMap<String, String>();
                    Long visitFrequency = roomVisitFrequencyForName.get(significantRoom) != null ?
                            roomVisitFrequencyForName.get(significantRoom) : 0;
                    mapForRoom.put("frequency", visitFrequency.toString());
                    if (rememberedRoomsForName.contains(significantRoom)) {        // Remembers the room. So add Dataname to Yes List
                        mapForRoom.put("remembers", "yes");


//                        result[1] = visitFrequency;
//                        result[0] = significantRooms.indexOf(significantRoom) * 2;
//                        System.out.println(significantRoom + ":" + visitFrequency + ", yes");
                    } else {
                        mapForRoom.put("remembers", "no");
//                        result[1] = visitFrequency;
//                        result[0] = significantRooms.indexOf(significantRoom) * 2 + 1;
//                        System.out.println(significantRoom + ":" + visitFrequency + ", no");
                    }
                    mapForName.put(significantRoom, mapForRoom);
//                    series.add(result[0], (result[1]) / 1000000);
                }

            }
            result.put(dataName, mapForName);

        }


        return result;

    }

}
