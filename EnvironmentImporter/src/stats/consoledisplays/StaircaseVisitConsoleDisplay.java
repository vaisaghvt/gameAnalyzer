package stats.consoledisplays;

import com.google.common.collect.HashMultimap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaircaseVisitConsoleDisplay extends ConsoleDisplay<HashMultimap<String, String>> {


    @Override
    public void display(HashMultimap<String, String> data) {
        System.out.print("Yes:");
        for (String name : data.get("yes")) {
            System.out.print(name + ", ");
        }
        System.out.println();
        System.out.print("No:");
        for (String name : data.get("no")) {
            System.out.print(name + ", ");
        }
        System.out.println();
        System.out.print("Maybe:");
        for (String name : data.get("maybe")) {
            System.out.print(name + ", ");
        }
    }
}
