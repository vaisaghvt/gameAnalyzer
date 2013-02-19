package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.chartdisplays.NodeRepetitionFrequencyChartDisplay;
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
public class NodeRepetitionFrequencyStatisticHandler extends StatisticsHandler<NodeRepetitionFrequencyConsoleDisplay, NodeRepetitionFrequencyChartDisplay> {


    public NodeRepetitionFrequencyStatisticHandler() {
        super(new NodeRepetitionFrequencyChartDisplay(),
                new NodeRepetitionFrequencyConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {

        if (allOrOne == StatsDialog.AllOrOne.EACH) {
            for (String dataName : dataNames) {
//                String dataName = dataNames.iterator().next();
                HashMultimap<String, Long> roomVisitTimeMap = NetworkModel.instance().getVertexInTimesFor(dataName);

                HashMap<String, Integer> roomDegree = NetworkModel.instance().getEdgesForEachRoom();
                Multiset<Double> deltaTToFrequencyMapping = findDeltaTToFrequencyMapping(roomVisitTimeMap);

                this.chartDisplay.setName(dataName);
                this.chartDisplay.setTitle(dataName);
                this.chartDisplay.display(deltaTToFrequencyMapping);
                this.consoleDisplay.display(deltaTToFrequencyMapping);
            }
        } else {
            HashMap<String, Multiset<Double>> nameToData = new HashMap<String, Multiset<Double>>();
            for (String dataName : dataNames) {
//                String dataName = dataNames.iterator().next();
                HashMultimap<String, Long> roomVisitTimeMap = NetworkModel.instance().getVertexInTimesFor(dataName);


                Multiset<Double> deltaTToFrequencyMapping = findDeltaTToFrequencyMapping(roomVisitTimeMap);

                nameToData.put(dataName, deltaTToFrequencyMapping);
            }
            Multiset<Double> result = aggregateFromData(nameToData, aggregationType);
            this.chartDisplay.setTitle("Time Between Revisits (Room)");
            this.chartDisplay.display(result);
            this.consoleDisplay.display(result);
        }
    }

    private Multiset<Double> aggregateFromData(HashMap<String, Multiset<Double>> nameToData, StatsDialog.AggregationType aggregationType) {
        Multiset<Double> result = HashMultiset.create();
        HashMap<Double, Integer> valueFrequencyMapping = new HashMap<Double, Integer>();
        int n=1;
        for (String name : nameToData.keySet()) {

            Multiset<Double> valueForSet = nameToData.get(name);
            for (Double value : valueForSet.elementSet()) {
                if (valueFrequencyMapping.containsKey(value)) {
                    valueFrequencyMapping.put(value,
                            aggregateData(valueFrequencyMapping.get(value), valueForSet.count(value), aggregationType,n));
                } else {
                    valueFrequencyMapping.put(value,
                            valueForSet.count(value));
                }
            }
            n++;
        }
        for (Double value : valueFrequencyMapping.keySet()) {

            result.add(value, valueFrequencyMapping.get(value));

        }
        return result;
    }

    private Integer aggregateData(int v1, int v2, StatsDialog.AggregationType aggregationType, int n) {
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

    private Multiset<Double> findDeltaTToFrequencyMapping(HashMultimap<String, Long> roomVisitTimeMap) {
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
