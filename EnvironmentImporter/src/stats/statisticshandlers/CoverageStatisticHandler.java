package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import stats.StatisticChoice;
import stats.chartdisplays.CoverageChartDisplay;
import stats.chartdisplays.StaircaseVisitChartDisplay;
import stats.consoledisplays.CoverageConsoleDisplay;
import stats.consoledisplays.StaircaseVisitConsoleDisplay;

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
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.COVERAGE;
        HashMultimap<Integer, String> coverageLevel =  ((NetworkModel) NetworkModel.instance()).getCoverage(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());

        this.chartDisplay.display(coverageLevel);
        this.consoleDisplay.display(coverageLevel);

    }




}
