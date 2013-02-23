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
import stats.chartdisplays.ParallelCoordinatePlotChart;
import stats.consoledisplays.GraphDetailsToFile;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParallelCoordinatePlotGenerator extends StatisticsHandler<GraphDetailsToFile, ParallelCoordinatePlotChart> {


    public ParallelCoordinatePlotGenerator() {
        super(new ParallelCoordinatePlotChart(),
                new GraphDetailsToFile()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {


        if (!dataNames.isEmpty()) {
            HashSet<Phase> phases = new HashSet<Phase>();
            for (Phase tempPhase : Phase.values()) {
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


            this.chartDisplay.setTitle("Parallel Plot of data");
            this.chartDisplay.display(nameToStatMapping);
            this.consoleDisplay.display(nameToStatMapping);


        } else {
            System.out.println("No data selected!");
        }
    }


    private HashMap<String, String> getStatsForGraph(String dataName, DirectedSparseMultigraph<ModelObject, ModelEdge> graph, HashMap<String, String> results) {
        double[] inDegrees = new double[graph.getVertexCount()];
        double[] outDegrees = new double[graph.getVertexCount()];

        int i = 0;
        for (ModelObject area : graph.getVertices()) {
            inDegrees[i] = graph.getInEdges(area).size();
            outDegrees[i] = graph.getOutEdges(area).size();

            i++;
        }


        Mean meanEvaluator = new Mean();
        Variance varianceEvaluator = new Variance();


        results.put("Edges Count", Integer.toString(graph.getEdgeCount()));
        results.put("Vertex Count", Integer.toString(graph.getVertexCount()));

        results.put("Mean InDegree", String.format("%1$.3f", meanEvaluator.evaluate(inDegrees)));
        results.put("Variance InDegree", String.format("%1$.3f", varianceEvaluator.evaluate(inDegrees)));

        results.put("Mean OutDegree", String.format("%1$.3f", meanEvaluator.evaluate(outDegrees)));
        results.put("Variance OutDegree", String.format("%1$.3f", varianceEvaluator.evaluate(outDegrees)));

        results.put("Diameter", String.format("%1$.3f", DistanceStatistics.diameter(graph)));


        int totalVertexNumber = NetworkModel.instance().getTotalNumberOfVertices();
        results.put("Vertex Coverage", Integer.toString((graph.getVertexCount() * 100) / totalVertexNumber));


        double distanceTraveled = NetworkModel.instance().getDistanceTraveledExploration(Collections.singleton(dataName)).get(dataName);
        results.put("Distance Covered (Exploration)", String.format("%1$.3f", distanceTraveled));


        distanceTraveled = NetworkModel.instance().getDistanceTraveledDuringTasks(Collections.singleton(dataName)).get(dataName);
        results.put("Distance Covered (Tasks)", String.format("%1$.3f", distanceTraveled));

        double timeTaken = NetworkModel.instance().getTimeTraveledTotal(Collections.singleton(dataName)).get(dataName);
        results.put("Time Taken (Total)", String.format("%1$.3f", timeTaken));


        timeTaken = NetworkModel.instance().getTimeTraveledDuringTasks(Collections.singleton(dataName)).get(dataName);
        results.put("Time Taken (Tasks)", String.format("%1$.3f", timeTaken));

        String task2Path = NetworkModel.instance().getPathDataFor(Collections.singleton(dataName), Phase.TASK_2).keySet().iterator().next();
        results.put("Path for task 2", task2Path);

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
        return String.format("%1$.2f", centerOfMass.getX()) + ","
                + String.format("%1$.2f", centerOfMass.getY()) + "," +
                String.format("%1$.2f", centerOfMass.getZ());
    }

    private static Point3D calculateCenterOfMass(List<HashMap<String, Number>> movement) {
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;


        for (HashMap<String, Number> row : movement) {

            Point3D p = new Point3D(
                    row.get("x").doubleValue(),
                    row.get("y").doubleValue(),
                    row.get("floor").doubleValue());
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();

            n++;
        }
        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }

    private static double calculateRadiusOfGyration(List<HashMap<String, Number>> movement, Point3D centerOfMass) {


        double sum = 0;
        int n = 0;


        for (HashMap<String, Number> row : movement) {

            Point3D p = new Point3D(
                    row.get("x").doubleValue(),
                    row.get("y").doubleValue(),
                    row.get("floor").doubleValue());


            sum += Math.pow(p.distance(centerOfMass), 2.0);
            n++;
        }

        return Math.sqrt(sum / n);
    }


}
