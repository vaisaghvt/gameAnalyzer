package stats.consoledisplays;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphDetailsConsoleDisplay extends ConsoleDisplay<HashMap<String, String>> {


    @Override
    public void display(HashMap<String, String> data) {
        for (String key : data.keySet()) {
            System.out.println(key + " = " + data.get(key));
        }

    }
}
