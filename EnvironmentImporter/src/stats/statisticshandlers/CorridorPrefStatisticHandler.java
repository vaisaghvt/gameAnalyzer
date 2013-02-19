package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.CorridorPrefChartDisplay;
import stats.consoledisplays.CorridorPrefConsoleDisplay;


import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorridorPrefStatisticHandler extends StatisticsHandler<CorridorPrefConsoleDisplay,CorridorPrefChartDisplay> {



    public CorridorPrefStatisticHandler() {
        super(new CorridorPrefChartDisplay(),
                new CorridorPrefConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.CORRIDOR_PREFERENCE_MEASURE;
        HashMultimap<String, String> prefersCorridors =  NetworkModel.instance().getCorridorRelatedMotion(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());
        this.chartDisplay.setPhase(phase);
        this.chartDisplay.display(prefersCorridors);
        this.consoleDisplay.display(prefersCorridors);

    }





}
