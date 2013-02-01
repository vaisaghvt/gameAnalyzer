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
public abstract class StatisticsHandler {

    ConsoleDisplay consoleDisplay;
    ChartDisplay chartDisplay;

    protected StatisticsHandler(ChartDisplay chartDisplay,ConsoleDisplay consoleDisplay) {
        this.chartDisplay = chartDisplay;

        this.consoleDisplay = consoleDisplay;
    }

    public abstract void generateAndDisplayStats(Collection<String> dataNames, Phase phase);

}
