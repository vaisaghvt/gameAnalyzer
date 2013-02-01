package stats.statisticshandlers;

import gui.Phase;
import stats.chartdisplays.ChartDisplay;
import stats.consoledisplays.ConsoleDisplay;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StatisticsHandler<T extends ConsoleDisplay, V extends ChartDisplay> {

    T consoleDisplay;
    V chartDisplay;

    protected StatisticsHandler(V chartDisplay,T consoleDisplay) {
        this.chartDisplay = chartDisplay;

        this.consoleDisplay = consoleDisplay;
    }

    public abstract void generateAndDisplayStats(Collection<String> dataNames, Phase phase);

}
