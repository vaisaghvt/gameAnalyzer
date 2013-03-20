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
        HashMap<String, Double> sumOfProbs = new HashMap<String, Double>();
        for(String source : data.keySet()){
            System.out.print(source +"\t");

            double sum =0;
            for(String destination: data.get(source).keySet()){
                System.out.print(
                        "To "+destination+":"+data.get(source).get(destination)+"\t");
                sum+= data.get(source).get(destination);
            }
            sumOfProbs.put(source, sum);
            System.out.println();
        }


    }
}
