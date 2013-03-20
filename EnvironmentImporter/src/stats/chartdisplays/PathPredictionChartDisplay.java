package stats.chartdisplays;


import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathPredictionChartDisplay extends ChartDisplay<Collection<String>> {



    @Override
    public void display(Collection<String> dataNames) {
        PathPredictionFrame frame = new PathPredictionFrame(dataNames);



    }


}