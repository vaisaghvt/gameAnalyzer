package stats.statisticshandlers;

import com.google.common.collect.HashMultimap;
import gui.NetworkModel;
import gui.Phase;
import stats.StatisticChoice;
import stats.chartdisplays.StaircaseVisitChartDisplay;
import stats.chartdisplays.VertexChartDisplay;
import stats.consoledisplays.StaircaseVisitConsoleDisplay;
import stats.consoledisplays.VertexConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

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
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase) {
        final StatisticChoice choice = StatisticChoice.STAIRCASE_VISIT_CHANCE;
        HashMultimap<String, String> usesStaircaseUnusuallyList =  ((NetworkModel) NetworkModel.instance()).getStaircaseRelatedMotion(dataNames, phase);
        this.chartDisplay.setTitle(choice.toString()+phase.toString());
        ((StaircaseVisitChartDisplay)this.chartDisplay).setPhase(phase);
        this.chartDisplay.display(usesStaircaseUnusuallyList);
        this.consoleDisplay.display(usesStaircaseUnusuallyList);

    }




}
