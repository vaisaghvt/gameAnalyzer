package stats.chartdisplays;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import stats.StatisticsDisplay;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ChartDisplay<T> implements StatisticsDisplay<T> {


    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String st){
        this.title = st;
    }



}
