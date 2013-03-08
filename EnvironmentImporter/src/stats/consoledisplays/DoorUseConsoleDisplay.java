package stats.consoledisplays;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoorUseConsoleDisplay extends ConsoleDisplay<HashMap<String, ? extends Number>> {


    @Override
    public void display(HashMap<String, ? extends Number> data) {
        for(String roomName: data.keySet()){
            if(data.get(roomName).doubleValue()==0){
                System.out.println(roomName);
            }
        }
    }
}
