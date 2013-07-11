package modelcomponents;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 4/10/13
 * Time: 9:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompleteGraph {

    private static CompleteGraph instance;
    private UndirectedSparseGraph<ModelObject, ModelEdge> completeGraph;
    private HashMap<Integer, ModelArea> idAreaMapping;
    private HashMap<ModelArea, Integer> areaFloorMapping;
    private HashMap<Integer, ModelGroup> areaIdGroupMapping;
    private ModelObject startingNode;
    private Collection<String> sortedRoomNames;

    private CompleteGraph() {
    }

    public ModelObject getStartingNode() {
        return startingNode;
    }

    public static CompleteGraph instance() {
        if (instance == null) {
            instance = new CompleteGraph();
        }
        return instance;
    }

    public void initialize(ModelFile file) {

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


        startingNode = this.findRoomByName("Start");
        if (startingNode == null) {
            startingNode = this.findRoomByName("StartingRoom");
        }


        sortedRoomNames = new TreeSet<String>();
        for (ModelObject area : this.completeGraph.getVertices()) {
            sortedRoomNames.add(area.toString());
        }


    }

    private void addNewVertex(Graph<ModelObject, ModelEdge> graph, ModelArea area) {
        if (areaIdGroupMapping.containsKey(area.getId())) {
            ModelGroup group = areaIdGroupMapping.get(area.getId());
            if (!graph.containsVertex(group))
                graph.addVertex(areaIdGroupMapping.get(area.getId()));
        } else {
            graph.addVertex(area);
        }
    }

    private void addNewEdge(Graph<ModelObject, ModelEdge> result, ModelArea area0, ModelArea area1) {
        int areaId0 = area0.getId();
        int areaId1 = area1.getId();
        ModelGroup group0, group1;

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

    public ModelArea getRoomForId(int id) {
        return this.idAreaMapping.get(id);
    }

    public ModelObject findRoomByName(String roomName) {

        for (ModelObject vertex : completeGraph.getVertices()) {
            if (vertex.toString().equalsIgnoreCase(roomName)) {
                return vertex;
            }
        }
        System.out.println("WARNING! COULDN'T FIND ROOM WITH NAME:" + roomName);
//        assert false;
        return null;
    }

    public int getFloorForVertex(ModelObject vertex) {
        int floor;
        if (vertex instanceof ModelArea) {
            floor = getFloorForArea((ModelArea) vertex);
        } else {
            ModelArea room = getRoomForId(((ModelGroup) vertex).getAreaIds().iterator().next());
            floor = getFloorForArea(room);
        }
        return floor;
    }

    public int getFloorForArea(ModelArea area) {
        return this.areaFloorMapping.get(area);
    }

    public Graph<ModelObject, ModelEdge> getGraph() {
        return this.completeGraph;
    }

    public Collection<ModelObject> getNeighbors(ModelObject vertex) {
        return completeGraph.getNeighbors(vertex);
    }

    public boolean isArea(int id) {
        return areaIdGroupMapping.containsKey(id);
    }

    public ModelObject getGroupForId(int id) {
        return areaIdGroupMapping.get(id);
    }

    public HashMap<String, Integer> getEdgesForEachRoom() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (ModelObject vertex : completeGraph.getVertices()) {
            result.put(vertex.toString(), completeGraph.degree(vertex));
        }
        return result;
    }

    public int findCoverage(DirectedSparseMultigraph<ModelObject, ModelEdge> localGraph) {
        int count = 0;
        for (ModelObject vertex : completeGraph.getVertices()) {
            if (localGraph.containsVertex(vertex)) {
                count++;
            }
        }
        return (count * 100) / completeGraph.getVertexCount();
    }

    public Collection<String> getSortedRooms() {

        if (completeGraph != null) {
            return sortedRoomNames;
        } else {
            return null;
        }
    }

    public Collection<String> getFloorDegreeSortedRooms() {
        TreeSet<String> degreeSortedRoomNames = new TreeSet<String>(new Comparator<String>() {

            @Override
            public int compare(String roomName1, String roomName2) {
                ModelObject room1 = findRoomByName(roomName1);
                ModelObject room2 = findRoomByName(roomName2);
                if (completeGraph.degree(room1) != completeGraph.degree(room2)) {
                    return completeGraph.degree(room1) - completeGraph.degree(room2);
                } else {
                    return roomName1.compareTo(roomName2);
                }
            }
        });
        degreeSortedRoomNames.addAll(sortedRoomNames);

        if (completeGraph != null) {

            ArrayList<String>[] floorRooms = new ArrayList[3];
            for (int i = 0; i < 3; i++) {
                floorRooms[i] = new ArrayList<String>();
            }


            for (String name : degreeSortedRoomNames) {
                ModelObject vertex = findRoomByName(name);
                int floor;
                if (vertex instanceof ModelArea) {
                    floor = getFloorForArea((ModelArea) vertex);
                } else {
                    ModelArea room = getRoomForId(((ModelGroup) vertex).getAreaIds().iterator().next());
                    floor = getFloorForArea(room);
                }


                floorRooms[floor].add(name);

            }
            ArrayList<String> finalList = new ArrayList<String>();
            for (ArrayList<String> list : floorRooms) {
                finalList.addAll(list);
            }

            return finalList;
        } else {
            return null;
        }
    }

    public Collection<String> getFloorSortedRooms() {

        if (completeGraph != null) {

            ArrayList<String>[] floorRooms = new ArrayList[3];
            for (int i = 0; i < 3; i++) {
                floorRooms[i] = new ArrayList<String>();
            }


            for (String name : sortedRoomNames) {
                ModelObject vertex = findRoomByName(name);
                int floor;
                if (vertex instanceof ModelArea) {
                    floor = getFloorForArea((ModelArea) vertex);
                } else {
                    ModelArea room = getRoomForId(((ModelGroup) vertex).getAreaIds().iterator().next());
                    floor = getFloorForArea(room);
                }


                floorRooms[floor].add(name);

            }
            ArrayList<String> finalList = new ArrayList<String>();
            for (ArrayList<String> list : floorRooms) {
                finalList.addAll(list);
            }

            return finalList;
        } else {
            return null;
        }
    }

    public int getTotalNumberOfVertices() {
        if (completeGraph != null) {
            return completeGraph.getVertexCount();
        } else
            return -1;

    }

    public Collection<String> getFloorNeighbourSortedRooms() {


        if (completeGraph != null) {

            ArrayList<String>[] floorRooms = new ArrayList[3];
            for (int i = 0; i < 3; i++) {
                floorRooms[i] = new ArrayList<String>();
            }


            for (String name : sortedRoomNames) {
                ModelObject vertex = findRoomByName(name);
                int floor;
                if (vertex instanceof ModelArea) {
                    floor = getFloorForArea((ModelArea) vertex);
                } else {
                    ModelArea room = getRoomForId(((ModelGroup) vertex).getAreaIds().iterator().next());
                    floor = getFloorForArea(room);
                }


                floorRooms[floor].add(name);

            }
            for (int i = 0; i < floorRooms.length; i++) {
                floorRooms[i] = sortByConnections(floorRooms[i]);
            }

            ArrayList<String> finalList = new ArrayList<String>();
            for (ArrayList<String> list : floorRooms) {
                finalList.addAll(list);
            }

            return finalList;
        } else {
            return null;
        }
    }

    private ArrayList<String> sortByConnections(ArrayList<String> floorRoom) {
        ArrayList<String> connectionSortedRooms = new ArrayList<String>();
        ArrayList<String> listOfRooms = new ArrayList<String>();
        ArrayList<String> toBeProcessed = new ArrayList<String>();

        listOfRooms.addAll(floorRoom);

        while (!listOfRooms.isEmpty()) {
            String room = listOfRooms.remove(0);

            if (!connectionSortedRooms.contains(room)) {
                connectionSortedRooms.add(room);

            }
            for (ModelObject neighbour : completeGraph.getNeighbors(findRoomByName(room))) {
                if (!connectionSortedRooms.contains(neighbour.toString())) {
                    toBeProcessed.add(neighbour.toString());
                }
            }
            while (!toBeProcessed.isEmpty()) {
                room = toBeProcessed.remove(0);
                boolean removalStatus = listOfRooms.remove(room);

                if (!removalStatus) {
                    if (floorRoom.contains(floorRoom.contains(room))) {
                        System.out.println("in trouble");
                    } else {
                        continue;
                    }
                }

                if (!connectionSortedRooms.contains(room)) {
                    connectionSortedRooms.add(room);

                }
                for (ModelObject neighbour : completeGraph.getNeighbors(findRoomByName(room))) {
                    if (!connectionSortedRooms.contains(neighbour.toString())) {
                        toBeProcessed.add(neighbour.toString());
                    }
                }


            }
        }
        if (connectionSortedRooms.size() != floorRoom.size()) {
            System.out.println("Size mismatch");
        }
        return connectionSortedRooms;


    }

    public int getDegreeOfRoom(ModelObject vertex) {
        return completeGraph.degree(vertex);
    }

    public int getVertexCount() {
        return completeGraph.getVertexCount();
    }

    public Collection<ModelObject> getVertices() {
        return completeGraph.getVertices();
    }
}
