package stats.consoledisplays;

import com.google.common.collect.HashMultimap;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoverageConsoleDisplay extends ConsoleDisplay<HashMultimap<Integer, String>> {



    @Override
    public void display(HashMultimap<Integer, String> data) {
        for(int i=0;i<=100;i++){
            System.out.println(Integer.toString(i)+ "% :-"+data.get(i));
        }
    }
}
