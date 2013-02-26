package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import randomwalk.RandomWalk;
import stats.StatisticChoice;
import stats.chartdisplays.VertexChartDisplay;
import stats.consoledisplays.VertexConsoleDisplay;

import javax.swing.*;
import java.awt.*;
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


            createProgressBar();
            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, choice, phase, allOrOne, aggregationType);
            task.addPropertyChangeListener(this);
            task.execute();


        } else {

            System.out.println("No Data Names selected!");
        }
    }



    private HashMap<String, Double> normalizeResult(HashMap<String, Double> personData) {

        HashMap<String, Double> result = new HashMap<String, Double>();
        HashMap<String, Number> randomWalkData = RandomWalk.instance().calculateAverageRoomVisitFrequency();
        int NRandom = 0, NPerson = 0;
        for (String room : randomWalkData.keySet()) {
            NRandom += randomWalkData.get(room).intValue();
        }
        for (String room : personData.keySet()) {
            NPerson += personData.get(room).intValue();
        }

        for (String room : personData.keySet()) {
            double originalValueForRoom = personData.get(room).doubleValue();
            double scaledValue = (originalValueForRoom * NRandom) / NPerson;
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

    class GenerateRequiredDataTask extends SwingWorker<Void, Void> {
        private final Phase phase;
        private final Collection<String> dataNames;
        private final StatisticChoice choice;
        private HashMap<String, HashMap<String, Double>> nameToResultMapping = new HashMap<String, HashMap<String, Double>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            this.dataNames = dataNames;
            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
        }

        @Override
        public Void doInBackground() {


            setProgress(0);
            int size = dataNames.size();
            int i = 1;
            for (String dataName : dataNames) {
                taskOutput.append("Processing " + dataName + "...\n");
                HashMap<String, Number> temp;
                synchronized (NetworkModel.instance()) {
                    temp = NetworkModel.instance().getVertexDataFor(dataName, choice, phase);
                }
                HashMap<String, Double> result = new HashMap<String, Double>();

                for (String roomName : temp.keySet()) {
                    long value = temp.get(roomName) == null ? 0 : temp.get(roomName).longValue();

                    result.put(roomName, (double) value);

                }

                result = normalizeResult(result);


                nameToResultMapping.put(dataName, result);
                setProgress((i * 100) / size);

                i++;

            }
            return null;

        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            frame.dispose();
            taskOutput.append("Done.");
            frame.dispose();
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

                HashMap<String, Double> finalResult = VertexVisitFrequencyStatisticHandler.this.aggregateData(resultGrouped, type);

                chartDisplay.setTitle(choice.toString() + ":" + phase.toString());
                chartDisplay.display(finalResult);
                consoleDisplay.display(finalResult);
            }

        }
    }

}
