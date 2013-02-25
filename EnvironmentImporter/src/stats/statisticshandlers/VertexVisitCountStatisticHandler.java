package stats.statisticshandlers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.VertexVisitChartDisplay;
import stats.consoledisplays.VertexVisitConsoleDisplay;

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
public class VertexVisitCountStatisticHandler extends StatisticsHandler<VertexVisitConsoleDisplay, VertexVisitChartDisplay> {


    public VertexVisitCountStatisticHandler() {
        super(new VertexVisitChartDisplay(),
                new VertexVisitConsoleDisplay()
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

    private Multiset<Double> summarizeData(HashMap<String, HashMap<String, Number>> dataNameDataMap, Phase phase) {
        Multiset<Double> result = HashMultiset.create();
        HashMap<String, Integer> roomEdgeCountMapping = NetworkModel.instance().getEdgesForEachRoom();
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, Number> dataForPerson = dataNameDataMap.get(dataName);


            for (String roomName : dataForPerson.keySet()) {
                if (dataForPerson.get(roomName) != null) {
                    long numberOfVisits = dataForPerson.get(roomName).longValue();
                    long numberOfEdges = roomEdgeCountMapping.get(roomName);
                    double normalizedNumberOfVisits = (double) numberOfVisits / (double) numberOfEdges;
                    result.add(normalizedNumberOfVisits);
                }
            }
        }
        return result;
    }

    class GenerateRequiredDataTask extends SwingWorker<Void, Void> {
        private final Phase phase;
        private final Collection<String> dataNames;
        private final StatisticChoice choice;
        HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
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


                synchronized (NetworkModel.instance()) {
                    dataNameDataMap.put(dataName, NetworkModel.instance().getVertexDataFor(dataName, choice, phase));
                }


                setProgress((i * 100) / size);
                taskOutput.append("Processing " + dataName + "...\n");
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
                HashMap<String, HashMap<String, Number>> tempDataNameDataMap = new HashMap<String, HashMap<String, Number>>();
                for (String dataName : dataNames) {
                    tempDataNameDataMap.clear();
                    tempDataNameDataMap.put(dataName, dataNameDataMap.get(dataName));

                    Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                    VertexVisitCountStatisticHandler.this.chartDisplay.setName(dataName);
                    VertexVisitCountStatisticHandler.this.chartDisplay.setTitle(dataName + ":" + choice.toString() + " :" + phase.toString());
                    VertexVisitCountStatisticHandler.this.chartDisplay.display(data);
                    VertexVisitCountStatisticHandler.this.consoleDisplay.display(data);

                }
            } else {

                Multiset<Double> data = summarizeData(dataNameDataMap, phase);

                VertexVisitCountStatisticHandler.this.chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
                VertexVisitCountStatisticHandler.this.chartDisplay.display(data);
                VertexVisitCountStatisticHandler.this.consoleDisplay.display(data);
            }

        }
    }


}
