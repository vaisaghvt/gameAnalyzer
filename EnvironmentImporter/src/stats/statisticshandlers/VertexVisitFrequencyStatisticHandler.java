package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.CompleteGraph;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;
import stats.StatisticChoice;
import stats.chartdisplays.VertexChartDisplay;
import stats.consoledisplays.VertexConsoleDisplay;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.VERTEX_VISIT_FREQUENCY, phase, allOrOne, aggregationType);
        final Semaphore generatorSemaphore = new Semaphore(1);
        try {
            generatorSemaphore.acquire();
        } catch (InterruptedException e) {

        }
        SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void> worker = new SwingWorker<Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>>, Void>(){

            @Override
            protected Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> doInBackground() throws Exception {
                return RandomWalkOrganizer.getAllRandomWalkGraphs(generatorSemaphore, RandomWalkOrganizer.RANDOM_WALK_TYPE.UNBIASED);
            }
        };
        worker.execute();
        try {
            generatorSemaphore.tryAcquire(1, 600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        super.actualGenerateAndDisplay(task);

    }

    private HashMap<String, Double> normalizeResultForRandomWalk() {
        HashMap<String, Double> result = new HashMap<String, Double>();

        int[] NRandomForFloor = new int[3];
        HashMap<String, Number> randomWalkData = RandomWalkOrganizer.calculateAverageRoomVisitFrequency(RandomWalkOrganizer.RANDOM_WALK_TYPE.UNBIASED);

        int NRandom = 0;
        for (String room : randomWalkData.keySet()) {
            int floorNumber= CompleteGraph.instance().getFloorForVertex(
                    CompleteGraph.instance().findRoomByName(room));

            NRandomForFloor[floorNumber] += randomWalkData.get(room).intValue();
            NRandom+= randomWalkData.get(room).intValue();
        }

        for (String room : randomWalkData.keySet()) {
            int floorNumber= CompleteGraph.instance().getFloorForVertex(
                    CompleteGraph.instance().findRoomByName(room));
            double originalValueForRoom = randomWalkData.get(room).doubleValue();
            double scaledValue = originalValueForRoom / NRandomForFloor[floorNumber];
//            double scaledValue = originalValueForRoom / NRandom;
//            double normalizedValue = scaledValue / randomWalkData.getValue(room).doubleValue();
            result.put(room, scaledValue);
        }

        return result;
    }


    private HashMap<String, Double> normalizeResult(HashMap<String, Double> personData) {

        HashMap<String, Double> result = new HashMap<String, Double>();
        HashMap<String, Number> randomWalkData = RandomWalkOrganizer.calculateAverageRoomVisitFrequency(RandomWalkOrganizer.RANDOM_WALK_TYPE.UNBIASED);
        int NRandom = 0, NPerson = 0;
        int[] NRandomForFloor = new int[3];
        int[] NPersonForFloor = new int[3];
        for (String room : randomWalkData.keySet()) {
            int floorNumber= CompleteGraph.instance().getFloorForVertex(
                    CompleteGraph.instance().findRoomByName(room));
//            NRandom+= randomWalkData.getValue(room).intValue();
            NRandomForFloor[floorNumber] += randomWalkData.get(room).intValue();
        }
        for (String room : personData.keySet()) {
            int floorNumber= CompleteGraph.instance().getFloorForVertex(
                    CompleteGraph.instance().findRoomByName(room));
//        NPerson+= personData.getValue(room).intValue();
            NPersonForFloor[floorNumber] += personData.get(room).intValue();

        }

        for (String room : personData.keySet()) {
            int floorNumber= CompleteGraph.instance().getFloorForVertex(
                    CompleteGraph.instance().findRoomByName(room));
            double originalValueForRoom = personData.get(room).doubleValue();
//            double scaledValue = (originalValueForRoom *NRandom) /NPerson;
            double scaledValue = (originalValueForRoom * NRandomForFloor[floorNumber]) / NPersonForFloor[floorNumber];
            double normalizedValue = scaledValue / randomWalkData.get(room).doubleValue();
            result.put(room, normalizedValue);
        }

        return result;
    }

    private HashMap<String, Double> aggregateData(HashMultimap<String, Double> data, StatsDialog.AggregationType aggregationType) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        for (String roomName : data.keySet()) {
            result.put(roomName, StatisticsHandler.aggregate(data.get(roomName), aggregationType));
        }
        return result;


    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;

        private final StatisticChoice choice;
        private HashMap<String, HashMap<String, Double>> nameToResultMapping = new HashMap<String, HashMap<String, Double>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
        }


        @Override
        protected void doTasks(String dataName) {
            HashMap<String, Number> temp;

            temp = NetworkModel.instance().getVertexDataFor(dataName, choice, phase);

            HashMap<String, Double> result = new HashMap<String, Double>();

            for (String roomName : temp.keySet()) {
                long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();

                result.put(roomName, (double) value);

            }
            if (dataName.equals("random walk")) {
                result = normalizeResultForRandomWalk();
            } else {
                result = normalizeResult(result);
            }
            synchronized (nameToResultMapping){
                nameToResultMapping.put(dataName, result);
            }
        }

        @Override
        protected void summarizeAndDisplay() {


            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for (String dataName : nameToResultMapping.keySet()) {
                    HashMap<String, Double> result = nameToResultMapping.get(dataName);
                    chartDisplay.setTitle(dataName + ":" + choice.toString() + ":" + phase.toString());
                    chartDisplay.display(result);
                    consoleDisplay.display(result);
                }
            } else {

                HashMultimap<String, Double> resultGrouped = HashMultimap.create();
                for (String dataName : nameToResultMapping.keySet()) {

                    for (String roomName : nameToResultMapping.get(dataName).keySet()) {
                        resultGrouped.put(roomName, nameToResultMapping.get(dataName).get(roomName));
                    }
                }

                HashMap<String, Double> finalResult = aggregateData(resultGrouped, type);

                chartDisplay.setTitle(choice.toString() + ":" + phase.toString());
                chartDisplay.display(finalResult);
                consoleDisplay.display(finalResult);
            }

        }
    }

}
