package gui;

import database.Database;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import modelcomponents.*;
import org.apache.commons.collections15.Transformer;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    Graph<ModelObject, ModelEdge> completeGraph;
    Graph<ModelObject, ModelEdge> currentGraph;
    HashMap<Integer, ModelArea> idAreaMapping;
    private Document document;
    private static NetworkModel instance;
    HashMap<ModelArea, Integer> areaFloorMapping;
    private String currentData;
    HashMap<Integer, ModelGroup> areaIdGroupMapping;
    private HashSet<Phase> selectedPhases;


    /**
     * Creates a new instance of SimpleGraphView
     */
    private NetworkModel() {
//        NetworkModel sgv = new NetworkModel(); // This builds the graph
//        // Layout<V, E>, VisualizationComponent<V,E>


        currentData = null;
        selectedPhases = new HashSet<Phase>();

    }


    @Override
    public void setDocument(Document current) {
        this.document = current;
        ModelFile file = document.getModelFile();
        completeGraph = new UndirectedSparseGraph<ModelObject, ModelEdge>();
        idAreaMapping = new HashMap<Integer, ModelArea>();
        areaFloorMapping = new HashMap<ModelArea, Integer>();
        areaIdGroupMapping = new HashMap<Integer, ModelGroup>();

        for (ModelFloor floor : file.getFloors()) {
            for (ModelGroup group : floor.getGroups()) {
                for (Integer areaId : group.getAreaIds()) {
                    this.areaIdGroupMapping.put(areaId, group);
                }
            }

            for (ModelArea area : floor.getRooms()) {
                idAreaMapping.put(area.getId(), area);
                areaFloorMapping.put(area, floor.getId());

                this.addNewVertex(completeGraph, area);
            }
            for (ModelArea area : floor.getStaircases()) {
                idAreaMapping.put(area.getId(), area);
                areaFloorMapping.put(area, floor.getId());

                this.addNewVertex(completeGraph, area);

            }
        }
        for (ModelFloor floor : file.getFloors()) {
            for (ModelLink link : floor.getLinks()) {
                this.addNewEdge(completeGraph,
                        idAreaMapping.get(link.getConnectingAreas().get(0)),
                        idAreaMapping.get(link.getConnectingAreas().get(1)));
            }
        }
        for (ModelStaircaseGroup group : file.getStaircaseGroups()) {
            ArrayList<Integer> staircases = new ArrayList<Integer>();
            staircases.addAll(group.getStaircaseIds());

            int areaId0 = staircases.get(0);
            int areaId1 = staircases.get(1);
            this.addNewEdge(completeGraph,
                    idAreaMapping.get(areaId0),
                    idAreaMapping.get(areaId1)
            );

        }
        currentData = "default";
        currentGraph = completeGraph;

        redrawPanel();
    }

    private void addNewVertex(Graph<ModelObject, ModelEdge> graph, ModelArea area) {
        if (areaIdGroupMapping.containsKey(area.getId())) {
            ModelGroup group = areaIdGroupMapping.get(area.getId());
            if (!completeGraph.containsVertex(group))
                completeGraph.addVertex(areaIdGroupMapping.get(area.getId()));
        } else {
            completeGraph.addVertex(area);
        }
    }

    public void setDisplay(String dataName) {
        if (dataName.equalsIgnoreCase("default")) {
            currentGraph = completeGraph;
            selectedPhases.clear();
        } else {


            selectedPhases.clear();
            selectedPhases.add(Phase.EXPLORATION);
            selectedPhases.add(Phase.TASK_1);
            selectedPhases.add(Phase.TASK_2);
            selectedPhases.add(Phase.TASK_3);
            List<HashMap<String, Number>> pathPoints = Database.getInstance().getMovementOfPlayer(dataName, selectedPhases);
            currentGraph = getDirectedGraphFromPoint(pathPoints);
        }
        currentData = dataName;
        redrawPanel();
    }

    private Graph<ModelObject, ModelEdge> getDirectedGraphFromPoint(List<HashMap<String, Number>> resultList) {
        Graph result = new DirectedSparseMultigraph<ModelObject, ModelEdge>();
        ModelObject prevVertex = null;

        for (HashMap<String, Number> row : resultList) {
            int x = (Integer) row.get("x");
            int y = (Integer) row.get("y");
            int floor = (Integer) row.get("floor");
            long time = (Long) row.get("time");
            ModelObject vertex = findVertexOfLocation(x, y, floor, prevVertex);
            if (vertex != null) {
                if (prevVertex != null && !prevVertex.equals(vertex)) {

                    if (!result.containsVertex(vertex))
                        result.addVertex(vertex);

                    ModelEdge edge = new ModelEdge();
                    edge.setTime(time);
                    result.addEdge(edge, prevVertex, vertex);

                    prevVertex = vertex;
                } else {
                    if (!result.containsVertex(vertex))
                        result.addVertex(vertex);
                    prevVertex = vertex;
                }
            } else {

                vertex = findFromScratch(x, y, floor);
                if (vertex != null) {
                    System.out.println("Area " + vertex + " not a neighbour of " + prevVertex);
//                    System.out.println(completeGraph.getNeighbors(prevVertex));
                    if (!result.containsVertex(vertex))
                        result.addVertex(vertex);

                    prevVertex = vertex;

                }

            }

        }
        return result;
    }

    private void addNewEdge(Graph result, ModelArea area0, ModelArea area1) {
        int areaId0 = area0.getId();
        int areaId1 = area1.getId();
        ModelGroup group0 = null, group1 = null;

        group0 = this.areaIdGroupMapping.get(areaId0);


        group1 = this.areaIdGroupMapping.get(areaId1);

        if (group0 == null && group1 == null) {
            result.addEdge(new ModelEdge(), area0, area1);
        } else if (group1 == null) {
            result.addEdge(new ModelEdge(), group0, area1);
        } else if (group0 == null) {
            result.addEdge(new ModelEdge(), area0, group1);
        } else {
            if (!group0.equals(group1)) {
                result.addEdge(new ModelEdge(), group0, group1);
            }
        }
    }

    private ModelObject findVertexOfLocation(int x, int y, int floor, ModelObject prevVertex) {


        if (prevVertex == null) {
            return findFromScratch(x, y, floor); // search in all locations on that floor

        }
        if (inPrevArea(x, y, floor, prevVertex)) {
            return prevVertex;  // if in previous area return that itself
        } else {
            ModelObject newVertex = findInNeighbouringVertices(x, y, floor, prevVertex);

            return newVertex; // returns null if nothing found (i.e. between areas)
        }


    }

    private ModelObject findInNeighbouringVertices(int x, int y, int floor, ModelObject prevVertex) {

        for (ModelObject room : completeGraph.getNeighbors(prevVertex)) {
            if (room instanceof ModelGroup) {
                ModelGroup group = (ModelGroup) room;
                for (Integer roomId : group.getAreaIds()) {
                    ModelArea area = this.idAreaMapping.get(roomId);
                    if (floor == areaFloorMapping.get(area) && isInArea(area, x, y)) {
                        if (!areaIdGroupMapping.containsKey(area.getId())) {
                            return area;
                        } else {
                            return areaIdGroupMapping.get(area.getId());
                        }
                    }
                }
            } else if (floor == areaFloorMapping.get((ModelArea) room) && isInArea((ModelArea) room, x, y)) {
                return room;
            }
        }
        return null;
    }

    private boolean inPrevArea(int x, int y, int floor, ModelObject prevVertex) {
        if (prevVertex instanceof ModelArea) {
            ModelArea prevArea = (ModelArea) prevVertex;
            return (this.areaFloorMapping.get(prevArea) == floor) && isInArea(prevArea, x, y);
        } else {
            ModelGroup group = (ModelGroup) prevVertex;
            for (Integer areaId : group.getAreaIds()) {
                ModelArea area = idAreaMapping.get(areaId);

                if ((this.areaFloorMapping.get(area) == floor) && isInArea(area, x, y)) {
                    return true;
                }

            }
            return false;
        }


    }

    private ModelObject findFromScratch(int x, int y, int floor) {
        ModelFloor floorObject = document.getFloor(floor);
        for (ModelArea area : floorObject.getRooms()) {
            if (isInArea(area, x, y)) {
                if (!areaIdGroupMapping.containsKey(area.getId())) {
                    return area;
                } else {
                    return areaIdGroupMapping.get(area.getId());
                }
            }
        }
        for (ModelArea area : floorObject.getStaircases()) {
            if (isInArea(area, x, y)) {
                if (!areaIdGroupMapping.containsKey(area.getId())) {
                    return area;
                } else {
                    return areaIdGroupMapping.get(area.getId());
                }
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

    private void redrawPanel() {
        this.removeAll();
        Layout<ModelObject, ModelEdge> layout = new KKLayout<ModelObject, ModelEdge>(this.currentGraph);
////        ((FRLayout2)layout).setMaxIterations(3000);
//        ((FRLayout2)layout).setAttractionMultiplier(2);
//        ((FRLayout2)layout).setRepulsionMultiplier(0.25);


        layout.setSize(new Dimension(1600, 900));
        VisualizationViewer<ModelObject, ModelEdge> vv = new VisualizationViewer<ModelObject, ModelEdge>(layout);
        vv.setPreferredSize(new Dimension(1600, 900));
        // Setup up a new vertex to paint transformer...


        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(gm);

        vv.getRenderContext().setVertexFillPaintTransformer(new SimpleFloorColoringTransformer<ModelObject, Paint>());
        vv.getRenderContext().setVertexShapeTransformer(new VertexRectangleTransformer<ModelObject, Shape>());
//        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);


        this.add(vv);
        this.revalidate();
    }


    public static MainPanel instance() {
        if (instance == null)
            instance = new NetworkModel();

        return instance;
    }

    public void switchOnPhase(Phase phase) {
        selectedPhases.add(phase);
        List<HashMap<String, Number>> pathPoints = Database.getInstance().getMovementOfPlayer(currentData, selectedPhases);
//        System.out.println(pathPoints.size());
        this.currentGraph = getDirectedGraphFromPoint(pathPoints);
        redrawPanel();
    }

    public void switchOffPhase(Phase phase) {
        selectedPhases.remove(phase);
        List<HashMap<String, Number>> pathPoints = Database.getInstance().getMovementOfPlayer(currentData, selectedPhases);
        this.currentGraph = getDirectedGraphFromPoint(pathPoints);
//        System.out.println(currentGraph.getVertexCount());

        redrawPanel();
    }

    private class SimpleFloorColoringTransformer<ModelObject, Paint> implements Transformer<ModelObject, Paint> {
        @Override
        public Paint transform(ModelObject obj) {
            ModelArea area;

            if (!(obj instanceof ModelArea)) {
                area = idAreaMapping.get(((ModelGroup) obj).getAreaIds().iterator().next());
            } else {
                area = (ModelArea) obj;
            }

            int floor = areaFloorMapping.get(area);
            switch (floor) {
                case 0:
                    return (Paint) Color.LIGHT_GRAY;
                case 1:
                    return (Paint) Color.GREEN;
                case 2:
                    return (Paint) Color.YELLOW;
            }
            return (Paint) Color.BLACK;
        }
    }

    private class VertexRectangleTransformer<ModelObject, Shape> implements Transformer<ModelObject, Shape> {

        @Override
        public Shape transform(ModelObject modelObject) {
            int width = modelObject.toString().length() * 10;
            return (Shape) new Rectangle(-width / 2, -10, width, 20);


        }
    }

    public HashMap<String, HashMap<String, Number>> getDataFor(String dataName, VertexStatisticsDialog.StatisticChoice choice) {
        HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>> phaseGraphMap;
        phaseGraphMap = new HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>>();

        HashSet<Phase> phases = new HashSet<Phase>();
        HashMap<String, HashMap<String, Number>> result = new HashMap<String, HashMap<String, Number>>();
        for (Phase phase : Phase.values()) {
            phases.clear();
            phases.add(phase);
            List<HashMap<String, Number>> pathPoints = Database.getInstance().getMovementOfPlayer(dataName, phases);
            phaseGraphMap.put(phase, (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints));

        }

        if (choice == VertexStatisticsDialog.StatisticChoice.VERTEX_VISIT_FREQUENCY) {
            for (Phase phase : Phase.values()) {
                DirectedSparseMultigraph<ModelObject, ModelEdge> graph = phaseGraphMap.get(phase);
                for (ModelObject vertex : graph.getVertices()) {
                    if (!result.containsKey(vertex.toString())) {
                        result.put(vertex.toString(), new HashMap<String, Number>());
                    }
                    HashMap<String, Number> map = result.get(vertex.toString());
                    map.put(phase.toString(), graph.inDegree(vertex));
                    result.put(vertex.toString(), map);


                }
            }
        } else if (choice == VertexStatisticsDialog.StatisticChoice.TIME_SPENT_PER_VERTEX) {

            for (Phase phase : Phase.values()) {
                DirectedSparseMultigraph<ModelObject, ModelEdge> graph = phaseGraphMap.get(phase);
                for (ModelObject vertex : graph.getVertices()) {
                    if (!result.containsKey(vertex.toString())) {
                        result.put(vertex.toString(), new HashMap<String, Number>());
                    }
                    HashMap<String, Number> map = result.get(vertex.toString());
                    TreeSet<ModelEdge> sortedEdgeSet = new TreeSet<ModelEdge>(new Comparator<ModelEdge>() {
                        @Override
                        public int compare(ModelEdge modelEdge, ModelEdge modelEdge1) {
                            return (int) (modelEdge.getTime()-modelEdge1.getTime());
                        }
                    });
                    sortedEdgeSet.addAll(graph.getIncidentEdges(vertex));
                    long time =0;
                    if(graph.inDegree(vertex)>graph.outDegree(vertex)){
                        //Ending Vertex;
                        ModelEdge lastEdge = sortedEdgeSet.pollLast();
                        time += Long.parseLong(Database.getInstance().getPhaseCompleteTime(phase, dataName))-lastEdge.getTime();
                    }else if(graph.inDegree(vertex)<graph.outDegree(vertex)){
                        //First Vertex;
                        ModelEdge firstEdge = sortedEdgeSet.pollFirst();
                        time += firstEdge.getTime()-Long.parseLong(Database.getInstance().getPhaseStartTime(phase, dataName)));
                    }

                    assert graph.inDegree(vertex)== graph.outDegree(vertex);
                    int numberOfEdges = sortedEdgeSet.size();
                    for(int i=0;i<numberOfEdges/2;i++){
                        ModelEdge incoming = sortedEdgeSet.pollFirst();
                        ModelEdge outgoing = sortedEdgeSet.pollFirst();
                        time+=outgoing.getTime()-incoming.getTime();

                    }
                    map.put(phase.toString(), time);
                    result.put(vertex.toString(), map);


                }
            }

        }
        return result;
    }

}


