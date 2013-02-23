package stats.consoledisplays;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceTimeConsoleDisplay extends ConsoleDisplay<HashMap<String, HashMap<String, Double>>> {


    @Override
    public void display(HashMap<String, HashMap<String, Double>> data) {
        TreeMap<Double, String> distanceToNameMapping = new TreeMap<Double, String>();
        TreeMap<Double, String> timeToNameMapping = new TreeMap<Double, String>();

        for (String name : data.keySet()) {
            distanceToNameMapping.put(data.get(name).get("distance"), name);
            timeToNameMapping.put(data.get(name).get("time"), name);

        }

        System.out.println("**************** Distances *****************");
        for (Double value : distanceToNameMapping.keySet()) {
            System.out.println(value + "\t:\t" + distanceToNameMapping.get(value));
        }


        System.out.println("**************** Times *****************");
        for (Double value : timeToNameMapping.keySet()) {
            System.out.println(value + "\t:\t" + timeToNameMapping.get(value));
        }

    }
}
