package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import stats.StatisticChoice;
import stats.chartdisplays.CoverageChartDisplay;
import stats.consoledisplays.CoverageConsoleDisplay;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoverageStatisticHandler extends StatisticsHandler<CoverageConsoleDisplay, CoverageChartDisplay> {


    public CoverageStatisticHandler() {
        super(new CoverageChartDisplay(),
                new CoverageConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne all, StatsDialog.AggregationType itemAt) {
        final StatisticChoice choice = StatisticChoice.COVERAGE;
        HashMultimap<Integer, String> coverageLevel = NetworkModel.instance().getCoverage(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString() + phase.toString());

        this.chartDisplay.display(coverageLevel);
        this.consoleDisplay.display(coverageLevel);

    }


}
