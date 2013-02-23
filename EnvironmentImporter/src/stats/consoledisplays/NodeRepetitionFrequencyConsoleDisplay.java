package stats.consoledisplays;

import com.google.common.collect.Multiset;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeRepetitionFrequencyConsoleDisplay extends ConsoleDisplay<Multiset<Double>> {


    @Override
    public void display(Multiset<Double> data) {
        System.out.println(data);
    }
}
