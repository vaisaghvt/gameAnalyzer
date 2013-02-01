package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import stats.StatisticChoice;
import stats.chartdisplays.CorridorPrefChartDisplay;
import stats.chartdisplays.PathChartDisplay;
import stats.chartdisplays.StaircaseVisitChartDisplay;
import stats.consoledisplays.CorridorPrefConsoleDisplay;
import stats.consoledisplays.PathConsoleDisplay;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathStatisticHandler extends StatisticsHandler {



    public PathStatisticHandler() {
        super(new PathChartDisplay(),
                new PathConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.PATH_FREQUENCY;
        HashMultimap<String, String> data = ((NetworkModel) NetworkModel.instance()).getPathDataFor(dataNames, phase, choice);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());
        ((PathChartDisplay)this.chartDisplay).setPhase(phase);
        this.chartDisplay.display(data);
        this.consoleDisplay.display(data);

    }




}
