package gui;

import com.google.common.collect.HashMultimap;
import database.Database;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import modelcomponents.*;
import org.apache.commons.collections15.Transformer;
import stats.StatisticChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import static stats.StatisticChoice.TIME_SPENT_PER_VERTEX;
import static stats.StatisticChoice.VERTEX_VISIT_FREQUENCY;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/14/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkModel extends MainPanel implements ActionListener {


    /**
     * @author Dr. Greg M. Bernstein
     */

    private final JMenu menu = new JMenu("Select Path");
    Graph<ModelObject, ModelEdge> completeGraph;
    Graph<ModelObject, ModelEdge> currentGraph;
    HashMap<Integer, ModelArea> idAreaMapping;
    private Document document;
    private static NetworkModel instance;
    HashMap<ModelArea, Integer> areaFloorMapping;
    private String currentData;
    HashMap<Integer, ModelGroup> areaIdGroupMapping;
    private HashSet<Phase> selectedPhases;
    private JMenuItem mi3Shortest = new JMenuItem("3Shortest");
    private JMenuItem mi3Norm = new JMenuItem("3Norm");
    private JMenuItem mi2Perfect = new JMenuItem("2Perfect");
    private JMenuItem mi2Confuse = new JMenuItem("2Confuse");
    private JMenuItem mi2MoreConfuse = new JMenuItem("2MoreConfuse");
    private JMenuItem mi2Weird = new JMenuItem("2Weird");
    private JMenuItem miPath1 = new JMenuItem("Shortest");
    private JMenuItem miPath2 = new JMenuItem("Path2");
    private JMenuItem miPath3 = new JMenuItem("Path3");
    private JMenuItem miPath4 = new JMenuItem("Path4");
    private JMenuItem miPath5 = new JMenuItem("Path5");
    private HashMap<AbstractButton, String[]> nameToPathMapping;
    private AbstractButton miPath6 = new JMenuItem("Path6");
    private HashSet<String> highlightedVertices;
    private VisualizationViewer<ModelObject, ModelEdge> vv;
    private HashMap<String, HashMap<Collection<Phase>, List<HashMap<String, Number>>>> nameToPhaseToMovementMap = new HashMap<String, HashMap<Collection<Phase>, List<HashMap<String, Number>>>>();


    /**
     * Creates a new instance of SimpleGraphView
     */
    private NetworkModel() {
//        NetworkModel sgv = new NetworkModel(); // This builds the graph
//        // Layout<V, E>, VisualizationComponent<V,E>

        highlightedVertices = new HashSet<String>();
        nameToPathMapping = new HashMap<AbstractButton, String[]>();
        nameToPathMapping.put(mi3Shortest, new String[]{"gallery", "Study1", "3Corr", "S3StaircaseRoom", "s3to2"});
        nameToPathMapping.put(mi3Norm, new String[]{"gallery", "3Corr", "S3StaircaseRoom", "s3to2"});

        nameToPathMapping.put(miPath1, new String[]{"LibraryG", "CorridorGB", "1MainJunc"});
        nameToPathMapping.put(miPath2, new String[]{"LibraryG", "CorridorGA2", "Conf 2", "SPCorr"});
        nameToPathMapping.put(miPath3, new String[]{"LibraryG", "CorridorGA2", "CorridorGA1", "1MainJunc"});
        nameToPathMapping.put(miPath4, new String[]{"LibraryG", "CorridorGA2", "CorridorGA1", "Conf 2", "SPCorr"});
        nameToPathMapping.put(miPath5, new String[]{"2CorrAMain", "92", "ACToDown", "left 2 stair", "left G main", "CorridorGA1", "1MainJunc"});
        nameToPathMapping.put(miPath6, new String[]{"2CorrAMain", "92", "ACToDown", "right 2 stair", "right G main", "CorridorGB", "1MainJunc"});

        nameToPathMapping.put(mi2Perfect, new String[]{"s2to3", "WayToFlr3", "2CorrC", "92", "2CorrAMain", "Library2"});
        nameToPathMapping.put(mi2Confuse, new String[]{"s2to3", "WayToFlr3", "2CorrC", "92", "ACToDown", "2CorrAMain", "Library2", "2DormCorr", "2nowhereCorr", "2CorrC"});
        nameToPathMapping.put(mi2MoreConfuse, new String[]{"s2to3", "WayToFlr3", "2CorrC", "92", "ACToDown", "2CorrAMain", "Library2", "2DormCorr", "2nowhereCorr", "2CorrC", "Dorm 1", "Dorm 2"});
        nameToPathMapping.put(mi2Weird, new String[]{"s2to3", "WayToFlr3", "2CorrC", "2PassB", "DB2", "MB1", "MB3", "TheLounge", "2CorrASide"});


        currentData = null;
        selectedPhases = new HashSet<Phase>();
        this.recreateContextMenu();

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

        this.recreateContextMenu();
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

    public void setDisplay(String dataName, boolean b) {
        if (!b)
            highlightedVertices.clear();
        if (dataName.equalsIgnoreCase("default")) {
            currentGraph = completeGraph;
            this.recreateContextMenu();
            selectedPhases.clear();
        } else {


            selectedPhases.clear();
            selectedPhases.add(Phase.EXPLORATION);
            selectedPhases.add(Phase.TASK_1);
            selectedPhases.add(Phase.TASK_2);
            selectedPhases.add(Phase.TASK_3);
            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, selectedPhases);
            currentGraph = getDirectedGraphFromPoint(pathPoints);
        }
        currentData = dataName;
        redrawPanel();
    }




    private double findDistanceTravelled(List<HashMap<String, Number>> pathPoints) {
        TreeMap<Long, Point> routeWalked = new TreeMap<Long, Point>();
        for (HashMap<String, Number> row : pathPoints) {
            int x = (Integer) row.get("x");
            int y = (Integer) row.get("y");
            Long time = (Long) row.get("time");

            routeWalked.put(time, new Point(x, y));


        }
        double distance =0;
        Iterator<Point> pointIterator = routeWalked.values().iterator();
        if (pointIterator.hasNext()) {
            Point firstPoint = pointIterator.next();
            while (pointIterator.hasNext()) {
                Point secondPoint = pointIterator.next();
                distance+=secondPoint.distance(firstPoint);
                firstPoint= secondPoint;
            }

        }

        return distance;

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
        Layout<ModelObject, ModelEdge> layout = new SpringLayout2<ModelObject, ModelEdge>(this.currentGraph);
////        ((FRLayout2)layout).setMaxIterations(3000);
//        ((FRLayout2)layout).setAttractionMultiplier(2);
//        ((FRLayout2)layout).setRepulsionMultiplier(0.25);


        layout.setSize(new Dimension(1600, 900));
        vv = new VisualizationViewer<ModelObject, ModelEdge>(layout);
        vv.setPreferredSize(new Dimension(1600, 900));
        // Setup up a new vertex to paint transformer...


        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(gm);
//        PluggableGraphMouse gm = new PluggableGraphMouse();
//        gm.add(new PopupVertexEdgeMenuMousePlugin<ModelObject, ModelEdge>());


        vv.getRenderContext().setVertexFillPaintTransformer(new SimpleFloorColoringTransformer<ModelObject, Paint>());
        vv.getRenderContext().setVertexShapeTransformer(new VertexRectangleTransformer<ModelObject, Shape>());
//        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);


        this.add(vv);
        vv.revalidate();
        this.revalidate();
    }


    public static MainPanel instance() {
        if (instance == null)
            instance = new NetworkModel();

        return instance;
    }

    public void switchOnPhase(Phase phase) {
        selectedPhases.add(phase);
        List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(currentData, selectedPhases);
//        System.out.println(pathPoints.size());
        this.currentGraph = getDirectedGraphFromPoint(pathPoints);
        redrawPanel();
    }

    public void switchOffPhase(Phase phase) {
        selectedPhases.remove(phase);
        List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(currentData, selectedPhases);
        this.currentGraph = getDirectedGraphFromPoint(pathPoints);
//        System.out.println(currentGraph.getVertexCount());

        redrawPanel();
    }


    @Override
    public void actionPerformed(ActionEvent event) {

        highlightedVertices.clear();
        highlightedVertices.addAll(Arrays.asList(nameToPathMapping.get(event.getSource())));

        this.setDisplay(currentData, true);
    }

    public HashMap<String, HashMap<String, Integer>> getHopDataFor(Collection<String> dataNames) {


        HashSet<Phase> phases = new HashSet<Phase>();

        HashMap<String, HashMap<String, Integer>> result = new HashMap<String, HashMap<String, Integer>>();
        for (String dataName : dataNames) {
            System.out.println("Processing " + dataName + "...");
            result.put(dataName, new HashMap<String, Integer>());
            for (Phase phase : Phase.values()) {
                phases.clear();
                phases.add(phase);
                List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
                DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);
                HashMap<String, Integer> map = result.get(dataName);
                map.put(phase.toString(), localGraph.getEdgeCount());
                result.put(dataName, map);

            }

        }

        return result;
    }

    private List<HashMap<String, Number>> getMovementOfPlayer(String dataName, Collection<Phase> phases) {
        List<HashMap<String, Number>> result;
        if (!this.nameToPhaseToMovementMap.containsKey(dataName) || !this.nameToPhaseToMovementMap.get(dataName).containsKey(phases)) {
            result = Database.getInstance().getMovementOfPlayer(dataName, phases);
            HashMap<Collection<Phase>, List<HashMap<String, Number>>> dataForName = this.nameToPhaseToMovementMap.get(dataName);
            if (dataForName == null) {
                dataForName = new HashMap<Collection<Phase>, List<HashMap<String, Number>>>();
            }
            dataForName.put(phases, result);
            this.nameToPhaseToMovementMap.put(dataName, dataForName);
        } else {

            result = this.nameToPhaseToMovementMap.get(dataName).get(phases);

        }
        return result;
    }

    public HashMultimap<String, String> getStaircaseRelatedMotion(Collection<String> dataNames, Phase phase) {
        HashSet<Phase> phases = new HashSet<Phase>();
        HashMultimap<String, String> result = HashMultimap.create();
        phases.clear();
        phases.add(phase);
        for (String dataName : dataNames) {
            System.out.println("Processing " + dataName + "...");


            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);

            YES_NO_CHOICE resultTemp = usesStaircaseUnusually(localGraph, phase);
            if (resultTemp == YES_NO_CHOICE.YES) {
                result.put("yes", dataName);
            } else if (resultTemp == YES_NO_CHOICE.NO) {
                result.put("no", dataName);
            } else {
                result.put("maybe", dataName);
            }

        }

        return result;
    }

    private YES_NO_CHOICE usesStaircaseUnusually(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, Phase phase) {

        if (phase == Phase.TASK_1) {
            ModelObject b2staircaseRoom = findRoomByName(completeGraph, "BToDown");
            ModelObject ac2staircaseRoom = findRoomByName(completeGraph, "ACToDown");
            ModelObject secondFloorLib = findRoomByName(completeGraph, "Library2");
//            ModelObject l3staircaseRoom = findRoomByName(localGraph, "S3StaircaseRoom");
            boolean beenHere = false;

            if (localGraph.containsVertex(ac2staircaseRoom)) {
                beenHere = true;
                if (localGraph.getSuccessors(ac2staircaseRoom).contains(findRoomByName(completeGraph, "left 2 stair")) || localGraph.getSuccessors(ac2staircaseRoom).contains(findRoomByName(completeGraph, "right 2 stair"))) {
                    return YES_NO_CHOICE.YES;
                }
            }
            if (localGraph.containsVertex(b2staircaseRoom)) {
                beenHere = true;
                if (localGraph.getSuccessors(b2staircaseRoom).contains(findRoomByName(completeGraph, "B2 Stairs"))) {
                    return YES_NO_CHOICE.YES;
                }
            }

            if (localGraph.containsVertex(secondFloorLib)) {
                beenHere = true;
                if (localGraph.getSuccessors(secondFloorLib).contains(findRoomByName(completeGraph, "LibraryG"))) {
                    return YES_NO_CHOICE.YES;
                }
            }
//          TODO : l3staircase Room implementation.
// else if(localGraph.containsVertex(l3staircaseRoom)) {
//                if(localGraph.getNeighbors(l3staircaseRoom).contains(findRoomByName(localGraph, "s3to2"))){
//                    return true;
//                }
//            }
            if (!beenHere) {
                System.out.println("Not even come here");
                return YES_NO_CHOICE.MAYBE;
            }
            return YES_NO_CHOICE.NO;


        } else if (phase == Phase.TASK_2) {
            ModelObject b2staircaseRoom = findRoomByName(completeGraph, "BToDown");
            ModelObject ac2staircaseRoom = findRoomByName(completeGraph, "ACToDown");
            ModelObject secondFloorLib = findRoomByName(completeGraph, "Library2");
            boolean beenHere = false;
            if (localGraph.containsVertex(ac2staircaseRoom)) {
                beenHere = true;
                if (localGraph.getSuccessors(ac2staircaseRoom).contains(findRoomByName(completeGraph, "left 2 stair")) || localGraph.getSuccessors(ac2staircaseRoom).contains(findRoomByName(completeGraph, "right 2 stair"))) {
                    return YES_NO_CHOICE.YES;
                }
            }
            if (localGraph.containsVertex(b2staircaseRoom)) {
                beenHere = true;
                if (localGraph.getSuccessors(b2staircaseRoom).contains(findRoomByName(completeGraph, "B2 Stairs"))) {
                    return YES_NO_CHOICE.YES;
                }
            }
            if (localGraph.containsVertex(secondFloorLib)) {
                beenHere = true;
                if (localGraph.getSuccessors(secondFloorLib).contains(findRoomByName(completeGraph, "LibraryG"))) {
                    return YES_NO_CHOICE.YES;
                }
            }
            //          TODO : l3staircase Room implementation.
// else if(localGraph.containsVertex(l3staircaseRoom)) {
//                if(localGraph.getNeighbors(l3staircaseRoom).contains(findRoomByName(localGraph, "s3to2"))){
//                    return true;
//                }
//            }
            if (!beenHere) {
                System.out.println("Not even come here");
                return YES_NO_CHOICE.MAYBE;
            }
            return YES_NO_CHOICE.NO;

        } else if (phase == Phase.TASK_3) {
            ModelObject secondFloorLib = findRoomByName(completeGraph, "Library2");

            if (localGraph.containsVertex(secondFloorLib)) {
                if (!localGraph.getSuccessors(secondFloorLib).contains(findRoomByName(completeGraph, "LibraryG"))) {
                    return YES_NO_CHOICE.YES;
                } else {
                    return YES_NO_CHOICE.NO;
                }
            } else {
                System.out.println("What?????");
                return YES_NO_CHOICE.MAYBE;
            }

        }
        System.out.println("INVALID!!!!");
        return null;

    }

    public HashMultimap<String, Long> getVertexInTimesFor(String dataName) {
        HashMultimap<String, Long> result = HashMultimap.create();

        HashSet<Phase> phases = new HashSet<Phase>();
        phases.clear();
        phases.add(Phase.EXPLORATION);
        List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);
        for (ModelObject vertex : localGraph.getVertices()) {
            for (ModelEdge edge : localGraph.getInEdges(vertex)) {
                result.put(vertex.toString(), edge.getTime());

            }
        }
        return result;

    }

    public HashMultimap<String, Long> getDoorInTimesFor(String dataName) {


        HashMultimap<String, Long> result = HashMultimap.create();

        HashSet<Phase> phases = new HashSet<Phase>();
        phases.clear();
        phases.add(Phase.EXPLORATION);
        List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
        DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);
        for (ModelEdge edge : localGraph.getEdges()) {
            String edgeStringRepresentation = edgeToString(localGraph.getEndpoints(edge));


            result.put(edgeStringRepresentation, edge.getTime());


        }
        return result;
    }

    public HashMap<String, Integer> getEdgesForEachRoom() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (ModelObject vertex : completeGraph.getVertices()) {
            result.put(vertex.toString(), completeGraph.degree(vertex));
        }
        return result;
    }

    public HashMultimap<Integer, String> getCoverage(Collection<String> dataNames, Phase phase) {
        HashSet<Phase> phases = new HashSet<Phase>();
        HashMultimap<Integer, String> result = HashMultimap.create();
        phases.clear();
        phases.add(phase);
        for (String dataName : dataNames) {
            System.out.println("Processing " + dataName + "...");


            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);

            int percentageCoverage = findCoverage(localGraph);

            result.put(percentageCoverage, dataName);

        }


        return result;
    }

    private int findCoverage(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
        int count = 0;
        for (ModelObject vertex : completeGraph.getVertices()) {
            if (localGraph.containsVertex(vertex)) {
                count++;
            }
        }
        return (count * 100) / completeGraph.getVertexCount();
    }

    public HashMap<String, Double> getDistanceTraveledDuringTasks(Collection<String> dataNames) {
        HashSet<Phase> phases = new HashSet<Phase>();
        phases.clear();
        phases.add(Phase.TASK_2);
        phases.add(Phase.TASK_3);

        HashMap<String, Double> result = new HashMap<String, Double>();
        for (String dataName : dataNames) {
            System.out.println("Calculating Distance for "+dataName);
            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);


            double distance = findDistanceTravelled(pathPoints);
            result.put(dataName, distance);
        }


        return result;


    }

    public HashMap<String, Long> getTimeTraveledDuringTasks(Collection<String> dataNames) {

        HashMap<String, Long> result = new HashMap<String, Long>();
        for (String dataName : dataNames) {

            System.out.println("Calculating time for "+ dataName);
            long time1 = Long.parseLong(Database.getInstance().getPhaseCompleteTime(Phase.TASK_1, dataName));
            long time2 = Long.parseLong(Database.getInstance().getPhaseCompleteTime(Phase.TASK_3, dataName));

            result.put(dataName, time2-time1);
        }


        return result;
    }

    public HashMap<String, Long> getTimeTraveledTotal(Collection<String> dataNames) {

        HashMap<String, Long> result = new HashMap<String, Long>();
        for (String dataName : dataNames) {

            long time1 = Long.parseLong(Database.getInstance().getPhaseStartTime(Phase.EXPLORATION, dataName));
            long time2 = Long.parseLong(Database.getInstance().getPhaseCompleteTime(Phase.TASK_3, dataName));

            result.put(dataName, time2-time1);
        }


        return result;
    }

    public HashMap<String, Double> getDistanceTraveledTotal(Collection<String> dataNames) {

        HashMap<String, Double> result = new HashMap<String, Double>();
        for (String dataName : dataNames) {
            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, Arrays.asList(Phase.values()));


            double distance = findDistanceTravelled(pathPoints);
            result.put(dataName, distance);
        }


        return result;


    }


    public HashMultimap<String, String> getCorridorRelatedMotion(Collection<String> dataNames, Phase phase) {
        HashSet<Phase> phases = new HashSet<Phase>();
        HashMultimap<String, String> result = HashMultimap.create();
        phases.clear();
        phases.add(phase);
        for (String dataName : dataNames) {
            System.out.println("Processing " + dataName + "...");


            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph = (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints);

            YES_NO_CHOICE resultTemp = prefersCorridors(localGraph, phase);
            if (resultTemp == YES_NO_CHOICE.YES) {
                result.put("yes", dataName);
            } else if (resultTemp == YES_NO_CHOICE.NO) {
                result.put("no", dataName);
            } else {
                result.put("maybe", dataName);
            }

        }

        return result;
    }

    private YES_NO_CHOICE prefersCorridors(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph, Phase phase) {
        ModelObject db2 = findRoomByName(completeGraph, "DB2");
        ModelObject mb1 = findRoomByName(completeGraph, "MB1");
        ModelObject mb3 = findRoomByName(completeGraph, "MB3");
        ModelObject theLounge = findRoomByName(completeGraph, "TheLounge");
        ModelObject gallery = findRoomByName(completeGraph, "Gallery");
        ModelObject study1 = findRoomByName(completeGraph, "Study1");
        ModelObject mr = findRoomByName(completeGraph, "MR");
        ModelObject study3 = findRoomByName(completeGraph, "Study3");

        if (localGraph.containsVertex(db2) && localGraph.containsVertex(mb1) &&
                localGraph.isNeighbor(db2, mb1)) {
            return YES_NO_CHOICE.NO;
        }
        if (localGraph.containsVertex(mb3) && localGraph.containsVertex(theLounge) &&
                localGraph.isNeighbor(mb3, theLounge)) {
            return YES_NO_CHOICE.NO;
        }
        if (localGraph.containsVertex(mr) && localGraph.containsVertex(study3) &&
                localGraph.isNeighbor(mr, study3)) {
            return YES_NO_CHOICE.NO;
        }
        if (localGraph.containsVertex(gallery) && localGraph.containsVertex(study1) &&
                localGraph.isNeighbor(gallery, study1)) {
            return YES_NO_CHOICE.NO;
        }
        return YES_NO_CHOICE.YES;


    }




    private class SimpleFloorColoringTransformer<ModelObject, Paint> implements Transformer<ModelObject, Paint> {
        @Override
        public Paint transform(ModelObject obj) {
            ModelArea area;

            if (highlightedVertices != null) {
                if (highlightedVertices.contains(obj.toString())) {
                    return (Paint) Color.RED;
                }

            }

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

    public HashMultimap<String, String> getPathDataFor(Collection<String> dataNames, Phase phase, StatisticChoice choice) {

        HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>> dataNameGraphMap;
        dataNameGraphMap = new HashMap<String, DirectedSparseMultigraph<ModelObject, ModelEdge>>();

        HashSet<Phase> phases = new HashSet<Phase>();
        phases.clear();
        phases.add(phase);
        HashMultimap<String, String> result = HashMultimap.create();
        for (String dataName : dataNames) {
            System.out.println("Processing " + dataName + "...");

            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            dataNameGraphMap.put(dataName, (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints));


        }


        if (phase == Phase.TASK_3) {
            HashMap<String, String[]> paths = new HashMap<String, String[]>();
            paths.put(miPath1.getText(), nameToPathMapping.get(miPath1));
            paths.put(miPath2.getText(), nameToPathMapping.get(miPath2));
            paths.put(miPath3.getText(), nameToPathMapping.get(miPath3));
            paths.put(miPath4.getText(), nameToPathMapping.get(miPath4));
            paths.put(miPath5.getText(), nameToPathMapping.get(miPath5));
            paths.put(miPath6.getText(), nameToPathMapping.get(miPath6));

            for (String name : dataNames) {
                assert dataNameGraphMap.containsKey(name);
                boolean added = false;
                DirectedSparseMultigraph<ModelObject, ModelEdge> graph = dataNameGraphMap.get(name);
                ModelObject startingRoom = findRoomByName(graph, "Library2");
                for (String path : paths.keySet()) {
                    if (graphIsPath(startingRoom, graph, Arrays.asList(paths.get(path)))) {
                        result.put(path, name);
                        added = true;
                    }
                }
                if (!added) {
                    result.put("lost", name);
                }
            }

            return result;
        } else if (phase == Phase.TASK_2) {

            HashMap<String, String[]> pathForFloor3 = new HashMap<String, String[]>();
            pathForFloor3.put(mi3Shortest.getText(), nameToPathMapping.get(mi3Shortest));
            pathForFloor3.put(mi3Norm.getText(), nameToPathMapping.get(mi3Norm));
            for (String name : dataNames) {
                assert dataNameGraphMap.containsKey(name);
                boolean added = false;
                DirectedSparseMultigraph<ModelObject, ModelEdge> graph = dataNameGraphMap.get(name);
                ModelObject startingRoom = findRoomByName(graph, "Gallery");
                for (String path : pathForFloor3.keySet()) {
                    if (checkFloorPath(startingRoom, graph, Arrays.asList(pathForFloor3.get(path)), 2)) {
                        result.put(path, name);
                        added = true;
                    }
                }
                if (!added) {
                    result.put("3Lost", name);
                }
            }

            HashMap<String, String[]> pathForFloor2 = new HashMap<String, String[]>();
            pathForFloor2.put(mi2Perfect.getText(), nameToPathMapping.get(mi2Perfect));
            pathForFloor2.put(mi2Confuse.getText(), nameToPathMapping.get(mi2Confuse));
            pathForFloor2.put(mi2MoreConfuse.getText(), nameToPathMapping.get(mi2MoreConfuse));
            pathForFloor2.put(mi2Weird.getText(), nameToPathMapping.get(mi2Weird));

            for (String name : dataNames) {
                assert dataNameGraphMap.containsKey(name);
                boolean added = false;
                DirectedSparseMultigraph<ModelObject, ModelEdge> graph = dataNameGraphMap.get(name);
                ModelObject startingRoom = findRoomByName(graph, "WayToFlr3");
                for (String path : pathForFloor2.keySet()) {
                    if (checkFloorPath(startingRoom, graph, Arrays.asList(pathForFloor2.get(path)), 1)) {
                        result.put(path, name);
                        added = true;
                    }
                }
                if (!added) {
                    result.put("2Lost", name);
                }
            }
            result.get(mi2MoreConfuse.getText()).removeAll(result.get(mi2Confuse.getText()));
            result.get(mi2Confuse.getText()).removeAll(result.get(mi2Perfect.getText()));


            return result;
        } else {

            return null;
        }
    }

    private boolean checkFloorPath(ModelObject startingRoom, DirectedSparseMultigraph<ModelObject, ModelEdge> passedGraph, List<String> pathDescription, int level) {

        Graph<ModelObject, ModelEdge> graph = new DirectedSparseMultigraph<ModelObject, ModelEdge>();

        for (ModelObject v : passedGraph.getVertices())
            graph.addVertex(v);

        for (ModelEdge e : passedGraph.getEdges())
            graph.addEdge(e, passedGraph.getIncidentVertices(e));

        Collection<ModelObject> vertices = new HashSet<ModelObject>(graph.getVertices());
        for (ModelObject vertex : vertices) {
            ModelArea area;
            if (vertex instanceof ModelGroup) {
                area = (ModelArea) idAreaMapping.get(((ModelGroup) (vertex)).getAreaIds().iterator().next());
            } else {
                area = (ModelArea) vertex;
            }

            if (this.areaFloorMapping.get(area) == 0) {
                return false;
            } else if (this.areaFloorMapping.get(area) != level) {
                graph.removeVertex(vertex);
            }
        }

        List<String> pathVertices = new ArrayList<String>(pathDescription);
        for (String vertexName : pathDescription) {
            ModelObject vertex = findRoomByName(completeGraph, vertexName);
//            System.out.println("Removing " + vertex + "from" + graph);
            if (graph.removeVertex(vertex)) {
                pathVertices.remove(vertexName);
            }


        }
        if (level == 1) {
            if (graph.getVertexCount() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            if (graph.getVertexCount() == 0 && pathVertices.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean graphIsPath(ModelObject currentRoom, DirectedSparseMultigraph<ModelObject, ModelEdge> graph, List<String> pathDescription) {
        if (pathDescription.isEmpty()) {
            return true;
        }
        for (ModelObject vertex : graph.getSuccessors(currentRoom)) {

            if (vertex.toString().equalsIgnoreCase(pathDescription.get(0))) {
                if (graph.getSuccessors(vertex).contains(currentRoom)) {
                    // there is a loop. Go out or come in first?

                    // He didn't really know what he was doing!
                    // comes in first;
                    return false;

                }

                return graphIsPath(vertex, graph, pathDescription.subList(1, pathDescription.size()));
            }
        }
        return false;

    }

    private ModelObject findRoomByName(Graph<ModelObject, ModelEdge> graph, String roomName) {
        for (ModelObject vertex : graph.getVertices()) {
            if (vertex.toString().equalsIgnoreCase(roomName)) {
                return vertex;
            }
        }
        System.out.println("WARNING! COULDN'T FIND ROOM WITH NAME:" + roomName);
        assert false;
        return null;
    }


    public HashMap<String, HashMap<String, Number>> getVertexDataFor(String dataName, StatisticChoice choice) {
        HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>> phaseGraphMap;
        phaseGraphMap = new HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>>();

        HashSet<Phase> phases = new HashSet<Phase>();
        HashMap<String, HashMap<String, Number>> result = new HashMap<String, HashMap<String, Number>>();
        for (Phase phase : Phase.values()) {
            phases.clear();
            phases.add(phase);
            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            phaseGraphMap.put(phase, (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints));

        }

        if (choice == VERTEX_VISIT_FREQUENCY) {
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
        } else if (choice == TIME_SPENT_PER_VERTEX) {

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
                            return (int) (modelEdge.getTime() - modelEdge1.getTime());
                        }
                    });
                    sortedEdgeSet.addAll(graph.getIncidentEdges(vertex));
                    long time = 0;
                    if (graph.inDegree(vertex) > graph.outDegree(vertex)) {
                        //Ending Vertex;
                        ModelEdge lastEdge = sortedEdgeSet.pollLast();
                        time += Long.parseLong(Database.getInstance().getPhaseCompleteTime(phase, dataName)) - lastEdge.getTime();
                    } else if (graph.inDegree(vertex) < graph.outDegree(vertex)) {
                        //First Vertex;
                        ModelEdge firstEdge = sortedEdgeSet.pollFirst();
                        time += firstEdge.getTime() - Long.parseLong(Database.getInstance().getPhaseStartTime(phase, dataName));
                    }

                    assert graph.inDegree(vertex) == graph.outDegree(vertex);
                    int numberOfEdges = sortedEdgeSet.size();
                    for (int i = 0; i < numberOfEdges / 2; i++) {
                        ModelEdge incoming = sortedEdgeSet.pollFirst();
                        ModelEdge outgoing = sortedEdgeSet.pollFirst();
                        time += outgoing.getTime() - incoming.getTime();

                    }
                    map.put(phase.toString(), time);
                    result.put(vertex.toString(), map);


                }
            }

        }
        return result;
    }

    public HashMap<String, HashMap<String, Number>> getEdgeDataFor(String dataName) {

        HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>> phaseGraphMap;
        phaseGraphMap = new HashMap<Phase, DirectedSparseMultigraph<ModelObject, ModelEdge>>();

        HashSet<Phase> phases = new HashSet<Phase>();
        HashMap<String, HashMap<String, Number>> result = new HashMap<String, HashMap<String, Number>>();
        for (Phase phase : Phase.values()) {
            phases.clear();
            phases.add(phase);
            List<HashMap<String, Number>> pathPoints = this.getMovementOfPlayer(dataName, phases);
            phaseGraphMap.put(phase, (DirectedSparseMultigraph<ModelObject, ModelEdge>) getDirectedGraphFromPoint(pathPoints));

        }


        for (Phase phase : Phase.values()) {
            DirectedSparseMultigraph<ModelObject, ModelEdge> graph = phaseGraphMap.get(phase);
            for (ModelEdge edge : graph.getEdges()) {
                String edgeStringRepresentation = edgeToString(graph.getEndpoints(edge));
                if (!result.containsKey(edgeStringRepresentation)) {
                    result.put(edgeStringRepresentation, new HashMap<String, Number>());
                }

                HashMap<String, Number> map = result.get(edgeStringRepresentation);
                Integer previousNumber = (Integer) map.get(phase.toString());
                if (previousNumber == null) {
                    map.put(phase.toString(), 0);
                } else {
                    map.put(phase.toString(), previousNumber + 1);
                }
                result.put(edgeStringRepresentation, map);


            }
        }


        return result;
    }

    private String edgeToString(Pair<ModelObject> endpoints) {
        TreeSet<String> set = new TreeSet<String>();
        set.add(endpoints.getFirst().toString());
        set.add(endpoints.getSecond().toString());
        return set.pollFirst() + "to" + set.pollLast();

    }

    public JMenu getContextMenu() {
        return this.menu;
    }

    void recreateContextMenu() {
        //context menu
        menu.removeAll();


        menu.add(mi3Shortest);
        menu.add(mi3Norm);

        menu.add(mi2Perfect);
        menu.add(mi2Confuse);
        menu.add(mi2MoreConfuse);
        menu.add(mi2Weird);

        mi3Shortest.addActionListener(this);
        mi3Norm.addActionListener(this);
        mi2Perfect.addActionListener(this);
        mi2Confuse.addActionListener(this);
        mi2MoreConfuse.addActionListener(this);
        mi2Weird.addActionListener(this);


        menu.add(miPath1);
        menu.add(miPath2);
        menu.add(miPath3);
        menu.add(miPath4);
        menu.add(miPath5);
        menu.add(miPath6);

        miPath1.addActionListener(this);
        miPath2.addActionListener(this);
        miPath3.addActionListener(this);
        miPath4.addActionListener(this);
        miPath5.addActionListener(this);
        miPath6.addActionListener(this);


    }


}


