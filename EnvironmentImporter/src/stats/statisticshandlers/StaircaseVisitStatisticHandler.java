package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import gui.YES_NO_CHOICE;
import stats.StatisticChoice;
import stats.chartdisplays.StaircaseVisitChartDisplay;
import stats.consoledisplays.StaircaseVisitConsoleDisplay;

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
public class StaircaseVisitStatisticHandler extends StatisticsHandler<StaircaseVisitConsoleDisplay, StaircaseVisitChartDisplay> {


    public StaircaseVisitStatisticHandler() {
        super(new StaircaseVisitChartDisplay(),
                new StaircaseVisitConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        final StatisticChoice choice = StatisticChoice.STAIRCASE_VISIT_CHANCE;

        if (!dataNames.isEmpty()) {


            createProgressBar();
            GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, choice, phase, allOrOne, aggregationType);
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
        HashMultimap<String,String> result = HashMultimap.create();
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
                YES_NO_CHOICE resultTemp;
                synchronized (NetworkModel.instance()) {
                    resultTemp = NetworkModel.instance().getStaircaseRelatedMotion(dataName, phase);
                }
                if (resultTemp == YES_NO_CHOICE.YES) {
                    result.put("yes", dataName);
                } else if (resultTemp == YES_NO_CHOICE.NO) {
                    result.put("no", dataName);
                } else {
                    result.put("maybe", dataName);
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
            StaircaseVisitStatisticHandler.this.chartDisplay.setTitle(choice.toString() + phase.toString());
            StaircaseVisitStatisticHandler.this.chartDisplay.setPhase(phase);
            StaircaseVisitStatisticHandler.this.chartDisplay.display(result);
            StaircaseVisitStatisticHandler.this.consoleDisplay.display(result);
        }
    }

}



