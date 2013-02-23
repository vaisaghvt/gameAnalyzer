package stats.consoledisplays;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphDetailsToFile extends ConsoleDisplay<HashMap<String, HashMap<String, String>>> {


    @Override
    public void display(HashMap<String, HashMap<String, String>> data) {


        try {
            File outputFile = new File("dataSummary.txt");

            PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

            ps.println(getHeaderLineForData(data));
            System.out.println(getHeaderLineForData(data));

            for (String dataName : data.keySet()) {


                HashMap<String, String> resultForName = data.get(dataName);
                ps.print(dataName);
                System.out.print(dataName);
                for (String key : resultForName.keySet()) {
                    ps.print("\t" + resultForName.get(key));
                    System.out.print("\t" + resultForName.get(key));

                }
                ps.println();
                System.out.println();


            }
            ps.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private String getHeaderLineForData(HashMap<String, HashMap<String, String>> data) {
        Collection<String> keys = data.values().iterator().next().keySet();

        StringBuilder string = new StringBuilder();
        string.append("Name");
        for (String key : keys) {
            string.append("\t").append(key);
        }

        return string.toString();
    }
}
