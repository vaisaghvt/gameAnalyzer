package stats.consoledisplays;

import com.google.common.collect.Multiset;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoomDurationTotalFrequencyConsoleDisplay extends ConsoleDisplay<Multiset<Long>> {



    @Override
    public void display(Multiset<Long> data) {
        System.out.println(data);
    }
}
