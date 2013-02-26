package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.PathChartDisplay;
import stats.consoledisplays.PathConsoleDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathStatisticHandler extends StatisticsHandler<PathConsoleDisplay, PathChartDisplay> {


    public PathStatisticHandler() {
        super(new PathChartDisplay(),
                new PathConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.PATH_FREQUENCY;

        if (!dataNames.isEmpty()) {
            createProgressBar();
            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, choice, phase);
            task.addPropertyChangeListener(this);
            task.execute();
        } else {
            System.out.println("No Data Names selected!");
        }




    }

    class GenerateRequiredDataTask extends SwingWorker<Void, Void> {
        private final Phase phase;
        private final Collection<String> dataNames;
        private final StatisticChoice choice;
        HashMultimap<String, String> result = HashMultimap.create();


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase) {
            this.dataNames = dataNames;
            this.choice = choice;
            this.phase = phase;

        }

        @Override
        public Void doInBackground() {
            setProgress(0);
            int size = dataNames.size();
            int i = 1;
            for (String dataName : dataNames) {
                taskOutput.append("Processing " + dataName + "...\n");
                String paths;
                synchronized (NetworkModel.instance()) {
                    paths =  NetworkModel.instance().getPathDataFor(dataName, phase);
                }





                if(phase ==Phase.TASK_3){
                    result.put(paths, dataName);
                }else if(phase == Phase.TASK_2){
                    String[] parts = paths.split(",");
                    result.put(parts[0],dataName);
                    result.put(parts[1],dataName);
                }


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
            chartDisplay.setTitle(choice.toString() + phase.toString());
            chartDisplay.setPhase(phase);
            chartDisplay.display(result);
            consoleDisplay.display(result);

        }
    }


}
