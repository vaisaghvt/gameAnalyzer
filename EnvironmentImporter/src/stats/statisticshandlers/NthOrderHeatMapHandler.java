package stats.statisticshandlers;

import com.google.common.collect.HashBasedTable;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import markovmodeldata.MarkovDataOrganizer;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;
import stats.chartdisplays.PathHeatMapChartDisplay;
import stats.consoledisplays.PathHeatMapConsoleDisplay;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class NthOrderHeatMapHandler extends StatisticsHandler<PathHeatMapConsoleDisplay, PathHeatMapChartDisplay> {

    public NthOrderHeatMapHandler() {
        super(new PathHeatMapChartDisplay(),
                new PathHeatMapConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        int order=-1;
        do{
            String value = JOptionPane.showInputDialog(new JFrame(), "What order heat map would you like?",1);
            try{
                order = Integer.parseInt(value);
            }catch (NumberFormatException n){
                JOptionPane.showMessageDialog(new JFrame(),"Please enter integer greater than 0","incorrect order", JOptionPane.ERROR_MESSAGE);
            }catch (NullPointerException n){
                JOptionPane.showMessageDialog(new JFrame(),"Please enter integer greater than 0","incorrect order", JOptionPane.ERROR_MESSAGE);
            }
            if(order<1){
                JOptionPane.showMessageDialog(new JFrame(),"Please enter integer greater than 0","incorrect order", JOptionPane.ERROR_MESSAGE);
            }
        }while(order<1);

        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, phase, allOrOne, order);
        super.actualGenerateAndDisplay(task);
    }






    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final int order;
        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
        private final StatsDialog.AllOrOne allOrOne;


        public GenerateRequiredDataTask(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, int order) {
            super(dataNames);
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.order = order;

        }

        @Override
        protected void doTasks(String dataName) {
            if (!dataName.equals("random walk")) {

                dataNameDataMap.put(dataName, NetworkModel.instance().getDirectedGraphOfPlayer(dataName, Collections.singleton(phase)));

            }
        }

        @Override
        protected void summarizeAndDisplay() {
            System.out.println("Done");
            switch (allOrOne) {

                case ALL:
                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        public HashBasedTable<String, String, Double> data;

                        @Override
                        protected Void doInBackground() throws Exception {
                            data = getNthOrderMarkovData(dataNameDataMap);
                            data = subtractRandomWalk(data);
                            return null;
                        }

                        protected void done() {
                            chartDisplay.setTitle(order+"th order heat map :" + phase.toString());
                            chartDisplay.display(data);
                            consoleDisplay.display(data);
                        }
                    };
                    worker.execute();
                    break;
                case EACH:

                    if (dataNames.contains("random walk")) {
                        RandomWalkCalculator randomWalkCalculator = new RandomWalkCalculator();
                        randomWalkCalculator.execute();

                        dataNames.remove("random walk");
                    }
                    for (final String name : dataNames) {

                        final HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> tempDataNameDataMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();
                        tempDataNameDataMap.put(name, dataNameDataMap.get(name));
                        SwingWorker<Void, Void> worker2 = new SwingWorker<Void, Void>() {
                            public HashBasedTable<String, String, Double> data;

                            @Override
                            protected Void doInBackground() throws Exception {
                                data = getNthOrderMarkovData(tempDataNameDataMap);
                                data = subtractRandomWalk(data);
                                return null;
                            }

                            protected void done() {
                                chartDisplay.setTitle(order+"th order heat map :" + name + ":" + phase.toString());
                                chartDisplay.display(data);
                                consoleDisplay.display(data);

                            }
                        };
                        worker2.execute();

                        tempDataNameDataMap.clear();
                    }
                    break;
            }

        }

        private HashBasedTable<String, String, Double> getNthOrderMarkovData(HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameGraphMap) {
            Collection<String> nameList = dataNameGraphMap.keySet();
            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphList = dataNameGraphMap.values();
            assert graphList.size() == nameList.size();
            Collection<Phase> selectedPhases = Collections.singleton(phase);
            return MarkovDataOrganizer.instance().getMarkovDataStore(nameList,selectedPhases,graphList).getDirectMarkovData(order).obtainHeatMap();
        }

        private class RandomWalkCalculator extends SwingWorker<Void, Void> {


            private HashBasedTable<String, String, Double> dataToDisplay;

            @Override
            protected Void doInBackground() throws Exception {
                dataToDisplay = MarkovDataOrganizer.instance().getRandomWalkMarkovData(
                        RandomWalkOrganizer.RANDOM_WALK_TYPE.UNBIASED).getDirectMarkovData(order).obtainHeatMap();

                return null;
            }

            @Override
            protected void done() {

                dataToDisplay = subtractRandomWalk(dataToDisplay);
                chartDisplay.setTitle(order+ " order heat map random walk");
                chartDisplay.display(dataToDisplay);
                consoleDisplay.display(dataToDisplay);

            }

        }

        private HashBasedTable<String, String, Double> subtractRandomWalk(HashBasedTable<String, String, Double> data) {

            final Semaphore generatorSemaphore = new Semaphore(1);
            try {
                generatorSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            HashBasedTable<String, String, Double> randomWalkData = MarkovDataOrganizer.instance().
                    getRandomWalkMarkovData(RandomWalkOrganizer.RANDOM_WALK_TYPE.UNBIASED).getDirectMarkovData(order).
                    obtainHeatMap();

            HashBasedTable<String, String, Double> result = HashBasedTable.create();
            for (String sourceRoom : randomWalkData.rowKeySet()) {

                for (String destinationRoom : randomWalkData.columnKeySet()) {
                    if(randomWalkData.contains(sourceRoom,destinationRoom)){
                        if (data.contains(sourceRoom,destinationRoom)) {
                            result.put(sourceRoom, destinationRoom,
                                    data.get(sourceRoom, destinationRoom) - randomWalkData.get(sourceRoom, destinationRoom));
                        } else {
                            result.put(sourceRoom, destinationRoom, -randomWalkData.get(sourceRoom, destinationRoom));
                        }
                    }

                }
            }

            return result;
        }
    }
}

