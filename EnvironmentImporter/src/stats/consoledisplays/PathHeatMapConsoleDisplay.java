package stats.consoledisplays;

import gui.NetworkModel;

import java.util.ArrayList;
import java.util.Collection;
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
        HashMap<String, Integer> roomToCodeMapping = generateNewRoomToCodeMapping(data);
        ArrayList<String> higherInData = new ArrayList<String>();
        ArrayList<String> lowerInData = new ArrayList<String>();
        for (String source : roomToCodeMapping.keySet()) {
            Integer sourceId = roomToCodeMapping.get(source);


            for (String destination : roomToCodeMapping.keySet()) {

                Integer destinationId = roomToCodeMapping.get(destination);
                if (!data.containsKey(source) || !data.get(source).containsKey(destination)) {

                } else {
                    double value =  (data.get(source).get(destination)+1.0)/2;
                    if(value>0.6){
                        higherInData.add(source +" to "+ destination+" : "+data.get(source).get(destination));
                    } else if(value<0.4){
                        lowerInData.add(source+" to "+destination+" : "+data.get(source).get(destination));
                    }
                }
            }

        }

        System.out.println(" ********* HIgher in Data **********");
        for(String value : higherInData){
            System.out.println(value);
        }

        System.out.println(" ********* Higher in Random Walk ***********");
        for(String value : lowerInData){
            System.out.println(value);
        }



    }

    private HashMap<String, Integer> generateNewRoomToCodeMapping(HashMap<String, HashMap<String, Double>> data) {
        int i = 0;
        //Graph<ModelObject, ModelEdge> graph = NetworkModel.instance().getCompleteGraph();
        Collection<String> rooms= NetworkModel.instance().getFloorNeighbourSortedRooms();
        HashMap<String, Integer> nameToCodeMapping = new HashMap<String, Integer>();
        for (String name : rooms) {
//            System.out.println(i+":"+name);
            nameToCodeMapping.put(name, i++);
        }

        return nameToCodeMapping;
    }
}
