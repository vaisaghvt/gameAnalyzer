package gui;

import database.Database;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import modelcomponents.*;

import javax.vecmath.Point3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/14/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkModel extends MainPanel {


    /**
     * @author Dr. Greg M. Bernstein
     */

    Graph<ModelArea, ModelEdge> completeGraph;
    Graph<ModelArea, ModelEdge> currentGraph;
    HashMap<Integer, ModelArea> idAreaMapping;
    private Document document;
    private static NetworkModel instance;
    HashMap<ModelArea, Integer> areaFloorMapping;
    private String currentDisplay;

    /**
     * Creates a new instance of SimpleGraphView
     */
    private NetworkModel() {
//        NetworkModel sgv = new NetworkModel(); // This builds the graph
//        // Layout<V, E>, VisualizationComponent<V,E>


        currentDisplay = null;


    }


    @Override
    public void setDocument(Document current) {
        this.document = current;
        ModelFile file = document.getModelFile();
        completeGraph = new UndirectedSparseGraph<ModelArea, ModelEdge>();
        idAreaMapping = new HashMap<Integer, ModelArea>();
        areaFloorMapping = new HashMap<ModelArea, Integer>();

        for (ModelFloor floor : file.getFloors()) {
            for (ModelArea area : floor.getRooms()) {

                completeGraph.addVertex(area);
                idAreaMapping.put(area.getId(), area);
                areaFloorMapping.put(area, floor.getId());
            }
            for (ModelArea area : floor.getStaircases()) {
                completeGraph.addVertex(area);
                idAreaMapping.put(area.getId(), area);
                areaFloorMapping.put(area, floor.getId());
            }
        }
        for (ModelFloor floor : file.getFloors()) {
            for (ModelLink link : floor.getLinks()) {
                completeGraph.addEdge(new ModelEdge(), idAreaMapping.get(link.getConnectingAreas().get(0)), idAreaMapping.get(link.getConnectingAreas().get(1)));
            }
        }
        for (ModelStaircaseGroup group : file.getStaircaseGroups()) {
            ArrayList<Integer> staircases = new ArrayList<Integer>();
            staircases.addAll(group.getStaircaseIds());
            completeGraph.addEdge(new ModelEdge(), idAreaMapping.get(staircases.get(0)), idAreaMapping.get(staircases.get(1)));
        }
        currentDisplay = "default";
        currentGraph = completeGraph;

        initializePanel();
    }

    public void setDisplay(String dataName) {
        if (dataName.equalsIgnoreCase("default")) {
            currentGraph = completeGraph;
        } else {
            ArrayList<Point3d> pathPoints = Database.getInstance().getMovementOfPlayer(dataName);
            currentGraph = getDirectedTreeFromPoint(pathPoints);
        }
        currentDisplay = dataName;
        initializePanel();
    }

    private Graph<ModelArea, ModelEdge> getDirectedTreeFromPoint(ArrayList<Point3d> pathPoints) {
        Graph result = new DirectedSparseMultigraph<ModelArea, ModelEdge>();
        ModelArea prevArea = null;
        Point3d prevPoint = null;
        for (Point3d point : pathPoints) {
            int x = (int) Math.round(point.x);
            int y = (int) Math.round(point.y);
            int floor = (int) Math.round(point.z);
            ModelArea area = findAreaOfLocation(x, y, floor, prevArea);
            if (area != null) {
                if (prevArea != null && !prevArea.equals(area)) {
                    result.addVertex(area);
                    result.addEdge(new ModelEdge(), prevArea, area);
                    prevArea = area;
                } else {
                    prevArea = area;
//                    result.addVertex(area);
                }
            }
            prevPoint = new Point3d(x,y,floor);
        }
        return result;
    }

    private ModelArea findAreaOfLocation(int x, int y, int floor, ModelArea prevArea) {
        if (prevArea == null) {
            return findFromScratch(x, y, floor); // search in all locations on that floor

        }
        if (inPrevArea(x, y, floor, prevArea)) {
            return prevArea;  // if in previous area return that itself
        } else {
            ModelArea newArea = findInNeighbouringAreas(x, y,floor, prevArea);
            if(newArea == null){
                ModelArea area = findFromScratch(x,y,floor);
                if (area!=null){
                    System.out.println("Area "+area+" not a neighbour of "+ prevArea);
//                    System.out.println(completeGraph.getNeighbors(prevArea));
                }
            }
            return newArea; // returns null if nothing found (i.e. between areas)
        }


    }

    private ModelArea findInNeighbouringAreas(int x, int y,int floor,  ModelArea prevArea) {

        for (ModelArea area : completeGraph.getNeighbors(prevArea)) {
            if (floor == areaFloorMapping.get(area)&& isInArea(area, x, y)) {
                return area;
            }
        }
        return null;
    }

    private boolean inPrevArea(int x, int y, int floor, ModelArea prevArea) {
        return (this.areaFloorMapping.get(prevArea) == floor) && isInArea(prevArea, x, y);

    }

    private ModelArea findFromScratch(int x, int y, int floor) {
        ModelFloor floorObject = document.getFloor(floor);
        for (ModelArea area : floorObject.getRooms()) {
            if (isInArea(area, x, y)) {
                return area;
            }
        }
        for (ModelArea area : floorObject.getStaircases()) {
            if (isInArea(area, x, y)) {
                return area;
            }
        }

        return null;
    }

    private boolean isInArea(ModelArea area, int x, int y) {
        int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
            return true;
        }
        return false;
    }

    private void initializePanel() {
        this.removeAll();
        Layout<ModelArea, ModelEdge> layout = new SpringLayout2<ModelArea, ModelEdge>(this.currentGraph);
        layout.setSize(new Dimension(1600, 900));
        BasicVisualizationServer<ModelArea, ModelEdge> vv = new BasicVisualizationServer<ModelArea, ModelEdge>(layout);
        vv.setPreferredSize(new Dimension(1600, 900));
        // Setup up a new vertex to paint transformer...
//        Transformer<Integer, Paint> vertexPaint = new Transformer<Integer, Paint>() {
//            public Paint transform(Integer i) {
//                return Color.GREEN;
//            }
//        };
        // Set up a new stroke Transformer for the edges
//        float dash[] = {10.0f};
//        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
//                BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
//        Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
//            public Stroke transform(String s) {
//                return edgeStroke;
//            }
//        };
//        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
//        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

        this.add(vv);
    }

    public static MainPanel instance() {
        if (instance == null)
            instance = new NetworkModel();

        return instance;
    }
}


