package stats.consoledisplays;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathHeatMapConsoleDisplay extends ConsoleDisplay<HashMap<String, HashMap<String, Double>>> {


    @Override
    public void display(HashMap<String, HashMap<String, Double>> data) {
        System.out.print("Destination:\t");
        for(String roomName : data.keySet()){
            System.out.print(roomName+"\t");
        }
        System.out.println();
        for(String source : data.keySet()){
            System.out.print(source +"\t");
            for(String destination: data.get(source).keySet()){
                System.out.print(
                        data.get(source).get(destination)+"\t");
            }
            System.out.println();
        }
    }
}
