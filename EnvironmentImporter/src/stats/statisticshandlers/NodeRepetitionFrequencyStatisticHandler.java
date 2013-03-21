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
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne, aggregationType);
        super.actualGenerateAndDisplay(task);
    }

    private Multiset<Double> aggregateFromData(HashMap<String, Multiset<Double>> nameToData, StatsDialog.AggregationType aggregationType) {
        Multiset<Double> result = HashMultiset.create();
        HashMap<Double, Integer> valueFrequencyMapping = new HashMap<Double, Integer>();
        int n = 1;
        for (String name : nameToData.keySet()) {

            Multiset<Double> valueForSet = nameToData.get(name);
            for (Double value : valueForSet.elementSet()) {
                if (valueFrequencyMapping.containsKey(value)) {
                    valueFrequencyMapping.put(value,
                            aggregateData(valueFrequencyMapping.get(value), valueForSet.count(value), aggregationType, n));
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

                        result.add((double) (difference / 1000));

//                    }else if(difference/1000 >=300){
//                        result.add((Math.log(300)));
                    }
//                    else {
////                        System.out.println(second + "," + first);
//                    }
                    first = second;

                }
            }
        }
        return result;

    }


    class GenerateRequiredDataTask extends AbstractTask {

        private HashMap<String, HashMultimap<String, Long>> nameToResultMapping = new HashMap<String, HashMultimap<String, Long>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;
        private final Phase phase;

        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);
            this.allOrOne = allOrOne;
            this.type = aggregationType;
            this.phase = phase;
        }

        @Override
        protected void doTasks(String dataName) {
            HashMultimap<String, Long> temp = NetworkModel.instance().getVertexInTimesFor(dataName, phase);
            nameToResultMapping.put(dataName, temp);
        }

        @Override
        protected void summarizeAndDisplay() {
            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for (String dataName : dataNames) {
//                String dataName = dataNames.iterator().next();
                    Multiset<Double> deltaTToFrequencyMapping = findDeltaTToFrequencyMapping(nameToResultMapping.get(dataName));

                    chartDisplay.setName(dataName);
                    chartDisplay.setTitle(dataName);
                    chartDisplay.display(deltaTToFrequencyMapping);
                    consoleDisplay.display(deltaTToFrequencyMapping);
                }
            } else {
                HashMap<String, Multiset<Double>> nameToData = new HashMap<String, Multiset<Double>>();
                for (String dataName : dataNames) {
//                String dataName = dataNames.iterator().next();
                    Multiset<Double> deltaTToFrequencyMapping =
                            findDeltaTToFrequencyMapping(nameToResultMapping.get(dataName));

                    nameToData.put(dataName, deltaTToFrequencyMapping);
                }
                Multiset<Double> result = aggregateFromData(nameToData, type);
                chartDisplay.setTitle("Time Between Revisits (Room)");
                chartDisplay.display(result);
                consoleDisplay.display(result);
            }

        }

    }

}
