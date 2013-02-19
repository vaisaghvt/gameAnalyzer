package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.PathChartDisplay;
import stats.consoledisplays.PathConsoleDisplay;

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
        HashMultimap<String, String> data = NetworkModel.instance().getPathDataFor(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());
        this.chartDisplay.setPhase(phase);
        this.chartDisplay.display(data);
        this.consoleDisplay.display(data);

    }




}
