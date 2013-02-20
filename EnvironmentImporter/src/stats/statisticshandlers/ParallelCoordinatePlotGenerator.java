package stats.statisticshandlers;

import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import javafx.geometry.Point3D;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import stats.chartdisplays.GraphDetailsChartDisplay;
import stats.consoledisplays.GraphDetailsConsoleDisplay;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParallelCoordinatePlotGenerator extends StatisticsHandler<GraphDetailsConsoleDisplay, GraphDetailsChartDisplay> {


    public ParallelCoordinatePlotGenerator() {
        super(new GraphDetailsChartDisplay(),
                new GraphDetailsConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {


        if (!dataNames.isEmpty()) {
            HashSet<Phase> phases = new HashSet<Phase>();
            for(Phase tempPhase: Phase.values()){
                phases.add(tempPhase);
            }

            HashMap<String, HashMap<String, String>> nameToStatMapping = new HashMap<String, HashMap<String, String>>();

            for (String dataName : dataNames) {

                List<HashMap<String, Number>> movementOfPlayer = NetworkModel.instance().getMovementOfPlayer(dataName, phases);
                DirectedSparseMultigraph<ModelObject, ModelEdge> graphForPlayer =
                        NetworkModel.instance().getDirectedGraphOfPlayer(dataName, phases);

                HashMap<String, String> results = new HashMap<String, String>();
                results = getStatsForMovement(movementOfPlayer, results);
                results = getStatsForGraph(dataName, graphForPlayer, results);

                nameToStatMapping.put(dataName, results);
            }



            if (allOrOne == StatsDialog.AllOrOne.EACH) {
                for(String dataName : dataNames){
                    this.chartDisplay.setName(dataName);
                    this.chartDisplay.setTitle(dataName+" : Stats Summary");
                    this.chartDisplay.display(nameToStatMapping.get(dataName));

                    System.out.println("*************"+dataName+"*************");
                    this.consoleDisplay.display(nameToStatMapping.get(dataName));
                }


            } else {
               HashMap<String, String> results = summarizeResults(nameToStatMapping, aggregationType);
                this.chartDisplay.setTitle("Total Stats Summary - "+aggregationType);
                this.chartDisplay.display(results);
                this.consoleDisplay.display(results);
            }


        } else {
            System.out.println("No data selected!");
        }
    }


    private HashMap<String, String> summarizeResults(HashMap<String, HashMap<String, String>> nameToStatMapping, StatsDialog.AggregationType aggregationType) {
        HashMap<String, String> results = new HashMap<String, String>();
        int n=0;
        for (String name: nameToStatMapping.keySet()){
            HashMap<String, String> resultForName = nameToStatMapping.get(name);
            for(String key: resultForName.keySet()){
                n++;
                if (NumberUtils.isNumber(resultForName.get(key))){
                    if(results.containsKey(key)){
                        results.put(key,
                            aggregate(
                                    Double.parseDouble(results.get(key)),
                                    Double.parseDouble(resultForName.get(key)),
                                    aggregationType,
                                    n));
                    }else {
                        results.put(key, resultForName.get(key));
                    }
                }else if("Center of Mass".equals(key) && aggregationType!= StatsDialog.AggregationType.MAX
                        && aggregationType != StatsDialog.AggregationType.MIN ){
                    if(results.containsKey(key)){
                        Point3D existingPoint = getPointFromString(results.get(key));
                        Point3D newPoint = getPointFromString(results.get(key));
                        double newX, newY, newZ;

                        switch (aggregationType){
                            case SUM:
                                newX = existingPoint.getX() + newPoint.getX();
                                newY = existingPoint.getY() + newPoint.getY();
                                newZ = existingPoint.getZ() + newPoint.getZ();
                                break;
                            case MEAN:

                                newX = ((existingPoint.getX()*(n-1)) + newPoint.getX())/n;
                                newY = ((existingPoint.getY()*(n-1))  + newPoint.getY())/n;
                                newZ = ((existingPoint.getZ()*(n-1))  + newPoint.getZ())/n;
                                break;
                            default:
                                newX=newY=newZ=0;
                                break;

                        }
                        results.put(key,pointAsString(new Point3D(newX, newY, newZ)));
                    }else{
                        results.put(key, resultForName.get(key));
                    }
                }else{
                    //TODO : Add point
                    System.out.println(resultForName.get(key));
                }

            }
        }


        return results;
    }

    private Point3D getPointFromString(String st) {
        String[] parts = st.split(",");
        return new Point3D(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));



    }

    private String aggregate(double v1, double v2, StatsDialog.AggregationType aggregationType, int n) {
        switch (aggregationType) {

            case SUM:
                return String.format("%1$.3f",v1 + v2);

            case MEAN:
                return String.format("%1$.3f",(v1 * (n - 1) + v2) / n);
            case MIN:
                return String.format("%1$.3f",Math.min(v1, v2));
            case MAX:
                return String.format("%1$.3f",Math.max(v1, v2));
            default:
                return null;
        }
    }


    private HashMap<String, String> getStatsForGraph(String dataName, DirectedSparseMultigraph<ModelObject, ModelEdge> graph, HashMap<String, String> results) {
        double[] inDegrees = new double[graph.getVertexCount()];
        double[] outDegrees = new double[graph.getVertexCount()];

        int i=0;
        for(ModelObject area: graph.getVertices()){
            inDegrees[i] = graph.getInEdges(area).size();
            outDegrees[i] = graph.getOutEdges(area).size();

            i++;
        }


        Mean meanEvaluator = new Mean();
        Variance varianceEvaluator = new Variance();


        results.put("Edges Count", Integer.toString(graph.getEdgeCount()));
        results.put("Vertex Count",Integer.toString(graph.getVertexCount()));

        results.put("Mean InDegree", String.format("%1$.3f", meanEvaluator.evaluate(inDegrees)));
        results.put("Variance InDegree", String.format("%1$.3f", varianceEvaluator.evaluate(inDegrees)));

        results.put("Mean OutDegree", String.format("%1$.3f", meanEvaluator.evaluate(outDegrees)));
        results.put("Variance OutDegree", String.format("%1$.3f", varianceEvaluator.evaluate(outDegrees)));

        results.put("Diameter", String.format("%1$.3f", DistanceStatistics.diameter(graph)));


        int totalVertexNumber = NetworkModel.instance().getTotalNumberOfVertices();
        results.put("Vertex Coverage", Integer.toString((graph.getVertexCount()*100) /totalVertexNumber));


        double distanceTraveled = NetworkModel.instance().getDistanceTraveledTotal(Collections.singleton(dataName)).get(dataName);
        results.put("Distance Covered (Total)", String.format("%1$.3f", distanceTraveled));


        distanceTraveled = NetworkModel.instance().getDistanceTraveledDuringTasks(Collections.singleton(dataName)).get(dataName);
        results.put("Distance Covered (Tasks)", String.format("%1$.3f", distanceTraveled));

        double timeTaken = NetworkModel.instance().getTimeTraveledTotal(Collections.singleton(dataName)).get(dataName);
        results.put("Time Taken (Total)", String.format("%1$.3f", timeTaken));


        timeTaken = NetworkModel.instance().getTimeTraveledDuringTasks(Collections.singleton(dataName)).get(dataName);
        results.put("Time Taken (Tasks)", String.format("%1$.3f", timeTaken));

        String task2Path = NetworkModel.instance().getPathDataFor(Collections.singleton(dataName), Phase.TASK_2).keySet().iterator().next();
        results.put("Path for task 2",task2Path);

        String task3Path = NetworkModel.instance().getPathDataFor(Collections.singleton(dataName), Phase.TASK_3).keySet().iterator().next();
        results.put("Path for task 3", task3Path);





        return results;
    }

    private HashMap<String, String> getStatsForMovement(List<HashMap<String, Number>> movement, HashMap<String, String> results) {
        Point3D centerOfMass = calculateCenterOfMass(movement);
        double radiusOfGyration = calculateRadiusOfGyration(movement, centerOfMass);

        results.put("Center of Mass", pointAsString(centerOfMass));
        results.put("Radius of Gyration", String.format("%1$.3f", radiusOfGyration));


        return results;

    }

    private String pointAsString(Point3D centerOfMass) {
        return String.format("%1$.2f",centerOfMass.getX())+","
                +String.format("%1$.2f",centerOfMass.getY())+","+
                String.format("%1$.2f", centerOfMass.getZ());
    }

    private static Point3D calculateCenterOfMass(List<HashMap<String, Number>> movement) {
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;




        for (HashMap<String, Number> row: movement) {

            Point3D p = new Point3D(
                    row.get("x").doubleValue(),
                    row.get("y").doubleValue(),
                    row.get("floor").doubleValue());
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();

            n++;
        }
        return new Point3D(sumX / n, sumY / n, sumZ /n);
    }

    private static double calculateRadiusOfGyration(List<HashMap<String, Number>> movement, Point3D centerOfMass) {


        double sum = 0;
        int n=0;



        for (HashMap<String, Number> row: movement) {

            Point3D p = new Point3D(
                    row.get("x").doubleValue(),
                    row.get("y").doubleValue(),
                    row.get("floor").doubleValue());


            sum += Math.pow(p.distance(centerOfMass),2.0);
            n++;
        }

        return Math.sqrt(sum / n);
    }



}
