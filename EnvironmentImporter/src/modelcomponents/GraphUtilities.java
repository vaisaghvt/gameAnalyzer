package modelcomponents;

import ec.util.MersenneTwister;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import gui.NetworkModel;
import javafx.geometry.Point3D;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/21/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphUtilities {

    private final static MersenneTwister random = new MersenneTwister();
    private static final double EPSILON = 0.0000001;

    public static HashMap<String, HashMap<String, HashMap<String, Double>>> calculateSecondOrderProbabilities(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection) {
        HashMap<String, HashMap<String, HashMap<String, Double>>> result = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
        for (String room : NetworkModel.instance().getSortedRooms()) {
            result.put(room,
                    calculateSecondOrderProbForNeighbouringRooms(graphCollection, room));


        }

        return result;

    }

    private static HashMap<String, HashMap<String, Double>> calculateSecondOrderProbForNeighbouringRooms(
            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, String startRoom) {
        ModelObject mainNode = NetworkModel.instance().findRoomByName(startRoom);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

//        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph : graphCollection) {


            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }

            TreeSet<ModelEdge> edgeCollection = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                @Override
                public int compare(ModelEdge o1, ModelEdge o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });

            edgeCollection.addAll(tempGraph.getIncidentEdges(mainNode));

            ModelEdge lonelyEdge = null;
            if (tempGraph.inDegree(mainNode) > tempGraph.outDegree(mainNode)) {
                //Ending Vertex;
                edgeCollection.pollLast();

//                localGraph.addVertex(mainNode);
//                localGraph.addEdge(new ModelEdge(),
//                        tempGraph.getOpposite(mainNode, lonelyEdge),
//                        mainNode
//                );

            } else if (tempGraph.inDegree(mainNode) < tempGraph.outDegree(mainNode)) {
                //First Vertex;
                edgeCollection.pollFirst();


//                localGraph.addVertex(mainNode);
//                localGraph.addEdge(new ModelEdge(),
//                        mainNode,
//                        tempGraph.getOpposite(mainNode, lonelyEdge)
//                );

            }

            for (int i = 0; i < edgeCollection.size() / 2; i++) {
                ModelEdge incoming = edgeCollection.pollFirst();
                ModelEdge outgoing = edgeCollection.pollFirst();
                ModelObject from = tempGraph.getOpposite(mainNode, incoming);
                ModelObject to = tempGraph.getOpposite(mainNode, outgoing);
                localGraph.addEdge(new ModelEdge(), from, to);
            }
        }


        return convertToSecondOrderProbabilities(localGraph);
    }

    private static HashMap<String, HashMap<String, Double>> convertToSecondOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
        HashMap<String, HashMap<String, Integer>> nodeToNodeTravelFrequency = new HashMap<String, HashMap<String, Integer>>();

        for (ModelEdge edge : localGraph.getEdges()) {
            String source = localGraph.getSource(edge).toString();
            String destination = localGraph.getDest(edge).toString();

            if (!nodeToNodeTravelFrequency.containsKey(source)) {
                nodeToNodeTravelFrequency.put(source, new HashMap<String, Integer>());
            }
            if (!nodeToNodeTravelFrequency.get(source).containsKey(destination)) {
                nodeToNodeTravelFrequency.get(source).put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(source).get(destination).intValue();
            nodeToNodeTravelFrequency.get(source).put(destination, currentValue + 1);

        }

        HashMap<String, HashMap<String, Double>> nodeToNodeProbabilities = new HashMap<String, HashMap<String, Double>>();

        for (String source : nodeToNodeTravelFrequency.keySet()) {
            nodeToNodeProbabilities.put(source, new HashMap<String, Double>());
            double totalNumberOfOutEdges = 0.0;
            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {
                totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(source).get(dest);
            }
            for (String dest : nodeToNodeTravelFrequency.get(source).keySet()) {

                nodeToNodeProbabilities.get(source).put(dest, (double) nodeToNodeTravelFrequency.get(source).get(dest) / totalNumberOfOutEdges);
            }


        }


        return nodeToNodeProbabilities;
    }


    public static HashMap<String, HashMap<String, Double>> calculateFirstOrderProbabilities(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection) {
        HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
        for (String room : NetworkModel.instance().getSortedRooms()) {
            result.put(room,
                    calculateFirstOrderProbForNeighbouringRooms(graphCollection, room));


        }

        return result;
    }

    private static HashMap<String, Double> calculateFirstOrderProbForNeighbouringRooms(
            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection, String room) {
        ModelObject mainNode = NetworkModel.instance().findRoomByName(room);
        Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(mainNode);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

        localGraph.addVertex(mainNode);
        for (ModelObject node : neighbours) {
            localGraph.addVertex(node);
        }

        for (DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph : graphCollection) {


            if (!tempGraph.containsVertex(mainNode)) {
                continue;
            }


            for (ModelEdge edge : tempGraph.getOutEdges(mainNode)) {

                localGraph.addEdge(new ModelEdge(), mainNode, tempGraph.getOpposite(mainNode, edge));
            }

        }


        return convertToFirstOrderProbabilities(localGraph, mainNode);
    }

    private static HashMap<String, Double> convertToFirstOrderProbabilities(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, ModelObject source) {
        HashMap<String, Integer> nodeToNodeTravelFrequency = new HashMap<String, Integer>();

        for (ModelEdge edge : localGraph.getEdges()) {

            String destination = localGraph.getDest(edge).toString();


            if (!nodeToNodeTravelFrequency.containsKey(destination)) {
                nodeToNodeTravelFrequency.put(destination, 0);
            }

            int currentValue = nodeToNodeTravelFrequency.get(destination).intValue();
            nodeToNodeTravelFrequency.put(destination, currentValue + 1);

        }

        HashMap<String, Double> nodeToNodeProbabilities = new HashMap<String, Double>();


        double totalNumberOfOutEdges = 0.0;
        for (String destination : nodeToNodeTravelFrequency.keySet()) {
            totalNumberOfOutEdges += nodeToNodeTravelFrequency.get(destination);
        }
        for (String destination : nodeToNodeTravelFrequency.keySet()) {

            nodeToNodeProbabilities.put(destination,
                    (double) nodeToNodeTravelFrequency.get(destination) / totalNumberOfOutEdges);
        }


        return nodeToNodeProbabilities;
    }

    public static Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> generatePaths(
            HashMap<String, HashMap<String, Double>> firstOrderProbs,
            HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbs,
            String startingRoom, int pathLength) {

        try {
            Graph<ModelObject, ModelEdge> completeGraph = NetworkModel.instance().getCompleteGraph();
            ModelObject startingLocation = null;


            if (completeGraph != null) {
                startingLocation = NetworkModel.instance().findRoomByName(startingRoom);
                if (startingLocation == null) {
                    System.out.println("No starting location");
                    throw new NullPointerException();
                }

            } else {
                System.out.println("No graph to load.");
                throw new NullPointerException();
            }


            CircularFifoBuffer<Double> varianceList = new CircularFifoBuffer<Double>(5);

            Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> resultSet =
                    new HashSet<DirectedSparseMultigraph<ModelObject, ModelEdge>>();
            List<Double> listOfGyrationRadius = new ArrayList<Double>();
            ProgressVisualizer progressVisualizer = new ProgressVisualizer();
            int count = 0;
            while (true) {

                DirectedSparseMultigraph<ModelObject, ModelEdge> tempGraph = generatePath(completeGraph, startingLocation, pathLength, firstOrderProbs, secondOrderProbs);
                resultSet.add(tempGraph);
                listOfGyrationRadius.add(calculateRadiusOfGyration(tempGraph, startingLocation));
                if (isStable(listOfGyrationRadius, varianceList, progressVisualizer)) {

                    break;
                } else {
                    count++;
                    if (count % 50 == 0) {

                        progressVisualizer.displayStability(count);

                    }
                }


//            System.out.println(resultSet.size());
            }
            progressVisualizer.finish();


            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DirectedSparseMultigraph<ModelObject, ModelEdge> generatePath(
            Graph<ModelObject, ModelEdge> completeGraph,
            ModelObject startingLocation, int pathLength,
            HashMap<String, HashMap<String, Double>> firstOrderProbabilities,
            HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities) {


        DirectedSparseMultigraph<ModelObject, ModelEdge> path = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        path.addVertex(startingLocation);
        String startRoom = startingLocation.toString();

        ModelObject currentNode = startingLocation;
        String destination = getDestination(firstOrderProbabilities.get(startRoom));
        if(destination.equals(startRoom)){
            System.out.println("Source same as destination");
        }


        currentNode = addNewVertexToPath(currentNode, destination, path, 0);

        if (pathLength == 1) {
            return path;

        }

        ModelObject lastNode = startingLocation;


        for (int hops = 1; hops < pathLength; hops++) {
            ModelObject tempNode = currentNode;
            currentNode = addOneMoreHop(currentNode, lastNode, path, firstOrderProbabilities, secondOrderProbabilities, hops);
            lastNode = tempNode;
            if(currentNode == lastNode){
                System.out.println("Error!!");
            }
        }

        return path;

    }

    private static ModelObject addOneMoreHop(ModelObject lastNode, ModelObject lastToLastNode,
                                             DirectedSparseMultigraph<ModelObject, ModelEdge> path,
                                             HashMap<String, HashMap<String, Double>> firstOrderProbabilities,
                                             HashMap<String, HashMap<String, HashMap<String, Double>>> secondOrderProbabilities, int hop) {


        HashMap<String, Double> nextLocationProbabilities = secondOrderProbabilities.get(lastNode.toString()).get(lastToLastNode.toString());
        if (nextLocationProbabilities == null) {
            nextLocationProbabilities = firstOrderProbabilities.get(lastNode);
        }
        if(nextLocationProbabilities==null || nextLocationProbabilities.isEmpty()){
            System.out.println("Mess up!!");
        }

        String destination = getDestination(nextLocationProbabilities);
        if(destination.equals(lastNode.toString())){
            System.out.println("Source and destination are same!!");
        }

        return addNewVertexToPath(lastNode, destination, path, hop);


    }


    private static ModelObject addNewVertexToPath(ModelObject lastNode, String destination, DirectedSparseMultigraph<ModelObject, ModelEdge> result, int currentLength) {
        ModelObject newNode = NetworkModel.instance().findRoomByName(destination);
        result.addVertex(newNode);
        ModelEdge edge = new ModelEdge();
        edge.setTime(currentLength);
        result.addEdge(edge, lastNode, newNode);

        return newNode;

    }

    private static String getDestination(HashMap<String, Double> roomProbabilities) {
        double value = 0.0;

        double randomDouble = random.nextDouble();

        for (String possibleDestination : roomProbabilities.keySet()) {
            value += roomProbabilities.get(possibleDestination);

            if (randomDouble < value) {

                return possibleDestination;
            }
        }

        System.out.println("TROUBLE!!!!");
        return null;
    }

    private static boolean isStable(List<Double> listOfGyrationRadius, CircularFifoBuffer<Double> varianceList, ProgressVisualizer progressVisualizer) {

        double[] primitiveNumbers = new double[listOfGyrationRadius.size()];
        int i = 0;
        for (Double number : listOfGyrationRadius) {
            primitiveNumbers[i] = number;
            i++;
        }
        Mean meanEvaluator = new Mean();
        double mean = meanEvaluator.evaluate(primitiveNumbers);


        Variance varianceEvaluator = new Variance();
        double var = varianceEvaluator.evaluate(primitiveNumbers, mean);

        varianceList.add(var);

        if (varianceList.size() < 5) {
            return false;
        }

        double[] primitiveVariance = new double[5];
        i = 0;
        for (Double number : varianceList) {
            primitiveVariance[i] = number;
            i++;
        }

        mean = meanEvaluator.evaluate(primitiveVariance);

        double stabilityValue = varianceEvaluator.evaluate(primitiveVariance, mean);
        progressVisualizer.setStability(stabilityValue);
        if (stabilityValue < EPSILON) {
            return true;
        } else {
            return false;
        }
    }


    private static Double calculateRadiusOfGyration(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
        Point3D centerOfMass = calcCenterOfMass(graph, start);

        TreeSet<ModelEdge> setOfEdges = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge modelEdge1, ModelEdge modelEdge2) {
                return (int) (modelEdge1.getTime() - modelEdge2.getTime());
            }
        });
        setOfEdges.addAll(graph.getEdges());
        double sum = 0;

        ModelObject current = start;
        sum += Math.pow(getCenterOfArea(current).distance(centerOfMass), 2.0);
        int n = 0;
        for (ModelEdge edge : setOfEdges) {
            current = graph.getOpposite(current, edge);
            sum += Math.pow(getCenterOfArea(current).distance(centerOfMass), 2.0);
            n++;
        }

        return Math.sqrt(sum / n);
    }

    private static Point3D calcCenterOfMass(DirectedGraph<ModelObject, ModelEdge> graph, ModelObject start) {
        ModelObject current = start;
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;

        TreeSet<ModelEdge> setOfEdges = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
            @Override
            public int compare(ModelEdge modelEdge1, ModelEdge modelEdge2) {
                return (int) (modelEdge1.getTime() - modelEdge2.getTime());
            }
        });
        setOfEdges.addAll(graph.getEdges());
        for (ModelEdge edge : setOfEdges) {

            current = graph.getOpposite(current, edge);

            Point3D p = getCenterOfArea(current);
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();

            n++;
        }
        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }

    private static Point3D getCenterOfArea(ModelObject area) {
        if (area instanceof ModelArea) {
            ModelArea room = (ModelArea) area;
            return getCenterOfRoom(room);
        }

        ModelGroup group = (ModelGroup) area;
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int n = 0;
        for (int areaId : group.getAreaIds()) {

            ModelArea tempArea = NetworkModel.instance().getRoomForId(areaId);
            Point3D tempPoint = getCenterOfRoom(tempArea);
            sumX += tempPoint.getX();
            sumY += tempPoint.getY();
            sumZ += tempPoint.getZ();
            n++;
        }


        return new Point3D(sumX / n, sumY / n, sumZ / n);
    }

    private static Point3D getCenterOfRoom(ModelArea room) {
        Point p1 = room.getCorner0();
        Point p2 = room.getCorner1();

        double x = (p1.getX() + p2.getX()) / 2;
        double y = (p1.getY() + p2.getY()) / 2;
        double z = (double) NetworkModel.instance().getFloorForArea(room);
        return new Point3D(x, y, z);

    }

    public static Double calculateAverageCoverage(Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection) {
        int totalNumberOfVertices = NetworkModel.instance().getCompleteGraph().getVertexCount();
        double[] coverageValues = new double[graphCollection.size()];
        int i = 0;
        for (DirectedSparseMultigraph graph : graphCollection) {
            int verticesInGraph = graph.getVertexCount();
            coverageValues[i] = (double) verticesInGraph / (double) totalNumberOfVertices;
            i++;
        }

        Mean meanEvaluator = new Mean();
        return meanEvaluator.evaluate(coverageValues);


    }


    public static class ProgressVisualizer {
        private JFrame progressFrame;
        private JTextArea taskOutput;
        private double stability;

        public ProgressVisualizer() {
            SwingUtilities.invokeLater(new Runnable() {


                @Override
                public void run() {
                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);


                    taskOutput = new JTextArea(5, 20);
                    taskOutput.setMargin(new Insets(5, 5, 5, 5));
                    taskOutput.setEditable(false);
                    DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
                    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                    progressFrame = new JFrame("Generating path data");
                    progressFrame.setLayout(new BorderLayout());
                    progressFrame.add(progressBar, BorderLayout.NORTH);
                    progressFrame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
                    progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                    progressFrame.setLocation(25, 25);
                    progressFrame.setSize(400, 200);
                    progressFrame.setVisible(true);
                }
            });
        }



        public void finish() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    progressFrame.dispose();
                }
            });
        }

        public void displayStability(int count) {
            NumberFormat doubleFormat = new DecimalFormat("##.000000");
            final String lastStabilityValue = doubleFormat.format(stability);
            NumberFormat integerFormat = new DecimalFormat("0000");
            final String lastCount = integerFormat.format(count);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {


                    taskOutput.append((lastCount) + " : " + lastStabilityValue + "\n");
                    progressFrame.revalidate();
                }
            });
        }

        public void setStability(double stability) {
            this.stability = stability;
        }
    }
}
