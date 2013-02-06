package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import stats.chartdisplays.DoorRepetitionFrequencyChartDisplay;
import stats.chartdisplays.NodeRepetitionFrequencyChartDisplay;
import stats.consoledisplays.DoorRepetitionFrequencyConsoleDisplay;
import stats.consoledisplays.NodeRepetitionFrequencyConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoorRepetitionFrequencyStatisticHandler extends StatisticsHandler<DoorRepetitionFrequencyConsoleDisplay, DoorRepetitionFrequencyChartDisplay> {



    public DoorRepetitionFrequencyStatisticHandler() {
        super(new DoorRepetitionFrequencyChartDisplay(),
                new DoorRepetitionFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        for (String dataName : dataNames) {
//                String dataName = dataNames.iterator().next();
            HashMultimap<String, Long> roomVisitTimeMap = ((NetworkModel) NetworkModel.instance()).getDoorInTimesFor(dataName);

            HashMap<String, Integer> roomDegree = ((NetworkModel) NetworkModel.instance()).getEdgesForEachRoom();
            Multiset<Double> deltaTToFrequencyMapping = findDeltaTToFrequencyMapping(roomVisitTimeMap, roomDegree);

            this.chartDisplay.setTitle(dataName);
            this.chartDisplay.display(deltaTToFrequencyMapping);
            this.consoleDisplay.display(deltaTToFrequencyMapping);
        }
    }

    private Multiset<Double> findDeltaTToFrequencyMapping(HashMultimap<String, Long> roomVisitTimeMap, HashMap<String, Integer> roomDegree) {
        TreeSet<Long> sortedVisitTimes = new TreeSet<Long>();
        Multiset<Double> result = HashMultiset.create();
        for (String roomName : roomVisitTimeMap.keySet()) {
            sortedVisitTimes.clear();
            sortedVisitTimes.addAll(roomVisitTimeMap.get(roomName));

            Iterator<Long> iterator = sortedVisitTimes.iterator();
            Long first;
            Long second;
            if (iterator.hasNext()) {

                first = iterator.next();
                while (iterator.hasNext()) {
                    second = iterator.next();
                    long difference = second - first;
//                   System.out.println(roomName+","+roomDegree.get(roomName));
                    if (difference / 1000 > 0) {
//                            && difference/1000 <300){

                        result.add((double) (difference / 10000));

//                    }else if(difference/1000 >=300){
//                        result.add((Math.log(300)));
                    } else {
                        System.out.println(second + "," + first);
                    }
                    first = second;

                }
            }
        }
        return result;

    }


}
