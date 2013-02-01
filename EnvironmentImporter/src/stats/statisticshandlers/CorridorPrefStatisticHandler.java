package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import stats.StatisticChoice;
import stats.chartdisplays.CorridorPrefChartDisplay;
import stats.chartdisplays.StaircaseVisitChartDisplay;
import stats.consoledisplays.CorridorPrefConsoleDisplay;
import stats.consoledisplays.StaircaseVisitConsoleDisplay;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorridorPrefStatisticHandler extends StatisticsHandler {



    public CorridorPrefStatisticHandler() {
        super(new CorridorPrefChartDisplay(),
                new CorridorPrefConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.CORRIDOR_PREFERENCE_MEASURE;
        HashMultimap<String, String> prefersCorridors =  ((NetworkModel) NetworkModel.instance()).getCorridorRelatedMotion(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());
        ((CorridorPrefChartDisplay)this.chartDisplay).setPhase(phase);
        this.chartDisplay.display(prefersCorridors);
        this.consoleDisplay.display(prefersCorridors);

    }




}
