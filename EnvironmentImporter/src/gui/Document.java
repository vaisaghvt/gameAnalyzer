package gui;

import modelcomponents.*;

import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.*;


public class Document {
    private int nextObjectId = 0;
    private File scenarioFile = null;

    private ModelFile content = null;
    private boolean hasUnsavedChanges = false;

//    private final HashMap<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>();
//	private HashMap<Integer,CObject> objects = new HashMap<Integer,CObject>();

    private ModelFloor currentFloor = null;

    private Integer hoveringOverRoomId = null;
    private Integer hoveringOverStaircaseId = null;
    private HashSet<ModelArea> selectedObjects;
    private ModelGroup currentGroup;


    public Document(String filename, String name, String path, int numberOfFloors) throws Exception {
        this.scenarioFile = new File(filename);

        content = new ModelFile(name, path);

        for (int i = 0; i < numberOfFloors; i++) {
            this.addFloor();
        }

        currentFloor = content.getFloors().get(0);
        selectedObjects = new HashSet<ModelArea>();
        this.save();
    }

    public Document(File file) throws Exception {
        this.scenarioFile = file;
        this.content = new ModelFile(file);
        selectedObjects = new HashSet<ModelArea>();
        HashSet<ModelObject> objects = new HashSet<ModelObject>();
        for (ModelFloor floor : content.getFloors()) {

            objects.add(floor);

            //set as current floor (if not already set)
            if (currentFloor == null) {
                currentFloor = floor;
            }

            objects.addAll(floor.getRooms());
            objects.addAll(floor.getLinks());
            objects.addAll(floor.getStaircases());


            //do some sanity checking
            //(1) rooms must not overlap
            HashSet<ModelArea> tempAreas = new HashSet<ModelArea>();
            tempAreas.addAll(floor.getRooms());
            tempAreas.addAll(floor.getStaircases());
            for (ModelArea area : tempAreas) {
                ArrayList<ModelArea> overlappingAreas = this.findOverlappingAreas(area, tempAreas);
                if (overlappingAreas.size() != 1) {
                    for (ModelArea overlappingArea : overlappingAreas) {
                        if (overlappingArea == area) continue;
                        System.out.println("WARNING: area " + overlappingArea.getId() + " is overlapping with area " + area.getId());
                    }
                }
                assert (overlappingAreas.get(0) == area); //must overlap with itself
            }

            //(2) all links have correct orientation
            HashMap<Integer, ModelArea> mapping = new HashMap<Integer, ModelArea>();
            for (ModelArea area : floor.getRooms()) mapping.put(area.getId(), area);
            for (ModelArea area : floor.getStaircases()) mapping.put(area.getId(), area);

            for (ModelLink link : floor.getLinks()) {
                if (link.getConnectingAreas().size() != 2) {
                    System.out.println("WARNING: link " + link.getId() + " has " + link.getConnectingAreas().size() + " connecting areas. Removing...");
                    objects.remove(link);
                    floor.getLinks().remove(link);
                } else {
                    ModelArea area0 = mapping.get(link.getConnectingAreas().get(0));
                    ModelArea area1 = mapping.get(link.getConnectingAreas().get(1));

                    int mnx0 = (int) Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
                    int mny0 = (int) Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
                    int mxx0 = (int) Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
                    int mxy0 = (int) Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());
                    int mnx1 = (int) Math.min(area1.getCorner0().getX(), area1.getCorner1().getX());
                    int mny1 = (int) Math.min(area1.getCorner0().getY(), area1.getCorner1().getY());
                    int mxx1 = (int) Math.max(area1.getCorner0().getX(), area1.getCorner1().getX());
                    int mxy1 = (int) Math.max(area1.getCorner0().getY(), area1.getCorner1().getY());

                    //are the rooms beside it each getOther?
                    if (mnx1 > mxx0 || mnx0 > mxx1) {

                    }
                    //or are the room on top of each getOther?
                    else if (mny1 > mxy0 || mny0 > mxy1) {

                    } else {
                        throw new AssertionError();
                    }
                }
            }


        }
        for (ModelStaircaseGroup group : content.getStaircaseGroups()) {
            objects.add(group);
            //TODO : check staircase group validity
        }


        //update next object id...
        for (ModelObject obj : objects) {
            if (obj.getId() >= nextObjectId) {
                nextObjectId = obj.getId() + 1;
            }
        }

        System.out.println("Next object id is " + nextObjectId);
    }


    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public String name() {
        return content.getName();
    }

    public int numberOfFloors() {
        return content.getFloors().size();
    }

    public void selectFloor(int floorIdx) {
        this.currentFloor = content.getFloors().get(floorIdx);
    }

    public void addFloor() throws Exception {
        ModelFloor floor = new ModelFloor();
        floor.setId(nextObjectId++);


        content.getFloors().add(floor);
        currentFloor = floor;


        hasUnsavedChanges = true;
    }

    public void addRoom(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        Point p0 = new Point();
        p0.x = mnx;
        p0.y = mny;

        Point p1 = new Point();
        p1.x = mxx;
        p1.y = mxy;

        ModelRoom room = new ModelRoom(nextObjectId++, p0, p1);


        currentFloor.getRooms().add(room);
        hasUnsavedChanges = true;
    }

    public void addLink(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        Point p0 = new Point();
        p0.x = mnx;
        p0.y = mny;

        Point p1 = new Point();
        p1.x = mxx;
        p1.y = mxy;


        HashSet<ModelArea> areas = new HashSet<ModelArea>();
        areas.addAll(currentFloor.getRooms());
        areas.addAll(currentFloor.getStaircases());
        ModelRoom tempRoom = new ModelRoom(-1, p0, p1);

        ArrayList<ModelArea> overlapping = this.findOverlappingAreas(tempRoom, areas);
        if (overlapping.size() == 2) {
            //determine the orientation of the link
            ModelArea area0 = overlapping.get(0);
            ModelArea area1 = overlapping.get(1);

            ArrayList<Integer> connectingAreaIds = new ArrayList<Integer>();
            connectingAreaIds.add(area0.getId());
            connectingAreaIds.add(area1.getId());

            ModelLink link = new ModelLink(nextObjectId++, p0, p1, connectingAreaIds);
            currentFloor.getLinks().add(link);
            hasUnsavedChanges = true;
        } else {
            System.out.println("INFO: cannot create link with" + overlapping.size() + " overlapping areas!");
        }
    }


    public void addStaircase(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        Point p0 = new Point();
        p0.x = mnx;
        p0.y = mny;

        Point p1 = new Point();
        p1.x = mxx;
        p1.y = mxy;

        ModelStaircase staircase = new ModelStaircase(nextObjectId++, p0, p1);


        currentFloor.getStaircases().add(staircase);
        hasUnsavedChanges = true;
    }


    public Map<String, ModelStaircaseGroup> getStaircaseGroups() {
        HashMap<String, ModelStaircaseGroup> mapping = new HashMap<String, ModelStaircaseGroup>();

        for (ModelStaircaseGroup group : content.getStaircaseGroups()) {
            mapping.put(group.getName(), group);
        }

        return mapping;
    }

    public void updateStaircaseGroups(Map<String, Set<ModelStaircase>> mapping) {
        content.getStaircaseGroups().clear();

        for (String groupName : mapping.keySet()) {
            ModelStaircaseGroup group = new ModelStaircaseGroup(groupName);
            group.setId(nextObjectId++);


            for (ModelStaircase staircase : mapping.get(groupName)) {
                group.getStaircaseIds().add(staircase.getId());
            }

            content.getStaircaseGroups().add(group);
        }

        hasUnsavedChanges = true;
    }


    public Integer isHoveringOverRoom(int x, int y) {
        hoveringOverRoomId = null;

        if (currentFloor != null) {
            for (ModelRoom area : currentFloor.getRooms()) {
                int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    hoveringOverRoomId = area.getId();
                    return hoveringOverRoomId;
                }
            }
        }

        return null;
    }

    public Integer isHoveringOverStaircase(int x, int y) {
        hoveringOverStaircaseId = null;

        if (currentFloor != null) {
            for (ModelRoom area : currentFloor.getStaircases()) {
                int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    hoveringOverStaircaseId = area.getId();
                    return hoveringOverStaircaseId;
                }
            }
        }

        return null;
    }


    public Graphics image(Graphics g, boolean viewLinks) {
        try {
            if (currentFloor != null) {


                for (ModelArea area : currentFloor.getRooms()) {

                    if ((selected() != null && selected().contains(area))) {
                        this.drawArea(g, area, Color.RED, true);

                    } else if (hoveringOverRoomId != null && hoveringOverRoomId == area.getId()) {
                        this.drawArea(g, area, Color.GREEN, false);
                    } else {
                        this.drawArea(g, area, Color.BLUE, false);
                    }
                }

                if (viewLinks) {
                    for (ModelArea link : currentFloor.getLinks()) {
                        if ((selected() != null && selected().contains(link))) {
                            this.fillArea(g, link, Color.RED);
                            g.setColor(Color.RED);
                        } else {
                            this.fillArea(g, link, Color.CYAN);
                            g.setColor(Color.CYAN);
                        }


                    }
                }


                for (ModelArea area : currentFloor.getStaircases()) {
                    if ((selected() != null && selected().contains(area))) {
                        this.drawArea(g, area, Color.RED, true);
                    } else if (hoveringOverStaircaseId != null && hoveringOverStaircaseId == area.getId()) {
                        this.drawArea(g, area, Color.GREEN, false);
                    } else {
                        this.drawArea(g, area, Color.MAGENTA, false);
                    }
                }


                return g;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void drawArea(Graphics g, ModelArea area, Color color, boolean drawLabel) {
        int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        Point drawingCorner0 = MapImagePanel.convertToDrawingCoordinate(new Point(mnx, mny), currentFloor.getId());
        Point drawingCorner1 = MapImagePanel.convertToDrawingCoordinate(new Point(mxx, mxy), currentFloor.getId());
        g.setColor(color);
        g.drawRect(drawingCorner0.x, drawingCorner0.y,
                drawingCorner1.x - drawingCorner0.x, drawingCorner1.y - drawingCorner0.y);
        if (drawLabel) {
//            completeGraph.setColor(Color.BLACK);
            g.drawString(area.toString(), (drawingCorner0.x + drawingCorner1.x) / 2 - ((area.toString().length()*5) / 2), (drawingCorner0.y + drawingCorner1.y) / 2);
//            System.out.println("Here");
        }
    }

    private void fillArea(Graphics g, ModelArea area, Color color) {
        int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        Point drawingCorner0 = MapImagePanel.convertToDrawingCoordinate(new Point(mnx, mny), currentFloor.getId());
        Point drawingCorner1 = MapImagePanel.convertToDrawingCoordinate(new Point(mxx, mxy), currentFloor.getId());
        g.setColor(color);
        g.fillRect(drawingCorner0.x, drawingCorner0.y,
                drawingCorner1.x - drawingCorner0.x, drawingCorner1.y - drawingCorner0.y);

//        completeGraph.setColor(Color.BLACK);
//        completeGraph.drawString(label, mnx + 4, mxy - 4);
    }

    public void save() throws Exception {
        String xml = content.convertToXML();

        PrintStream out = new PrintStream(scenarioFile);
        out.println(xml);
        out.close();
    }

    private ArrayList<ModelArea> findOverlappingAreas(ModelArea area, Set<ModelArea> areas) {
        int mnx1 = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny1 = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx1 = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy1 = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        ArrayList<ModelArea> overlapping = new ArrayList<ModelArea>();
        for (ModelArea a : areas) {
            int mnx2 = (int) Math.min(a.getCorner0().getX(), a.getCorner1().getX());
            int mny2 = (int) Math.min(a.getCorner0().getY(), a.getCorner1().getY());
            int mxx2 = (int) Math.max(a.getCorner0().getX(), a.getCorner1().getX());
            int mxy2 = (int) Math.max(a.getCorner0().getY(), a.getCorner1().getY());

            if (!(mnx2 > mxx1 || mxx2 < mnx1 || mny2 > mxy1 || mxy2 < mny1)) {
                overlapping.add(a);
            }
        }

        return overlapping;
    }

    public ModelFloor getFloor(int floorNumber) {
        return this.content.getFloors().get(floorNumber);
    }

    public int getCurrentFloor() {
        return currentFloor.getId();
    }

    public Collection<ModelArea> selected() {
        return this.selectedObjects;
    }

    public void removeSelected() {
        if (selectedObjects != null && !selectedObjects.isEmpty()) {
            for (ModelArea selectedObject : selectedObjects) {
                currentFloor.getRooms().remove(selectedObject);
                currentFloor.getStaircases().remove(selectedObject);
                currentFloor.getLinks().remove(selectedObject);
            }
            selectedObjects.clear();
            hasUnsavedChanges = true;
        }
    }

    public HashSet<ModelArea> selectObject(Integer x, Integer y, boolean ctrlDown) {
        hoveringOverRoomId = null;
        if (ctrlDown == false) {
//            System.out.println("Not controlled!!");
            selectedObjects.clear();
        }
        if (currentFloor != null) {
            for (ModelLink link : currentFloor.getLinks()) {
                int mnx = (int) Math.min(link.getCorner0().getX(), link.getCorner1().getX());
                int mny = (int) Math.min(link.getCorner0().getY(), link.getCorner1().getY());
                int mxx = (int) Math.max(link.getCorner0().getX(), link.getCorner1().getX());
                int mxy = (int) Math.max(link.getCorner0().getY(), link.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    this.selectedObjects.add(link);
                    return selectedObjects;
                }
            }
            for (ModelRoom area : currentFloor.getRooms()) {
                int mnx = (int) Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = (int) Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = (int) Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = (int) Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {

                    this.selectedObjects.add(area);
                    return selectedObjects;
                }
            }
            for (ModelStaircase staircase : currentFloor.getStaircases()) {
                int mnx = (int) Math.min(staircase.getCorner0().getX(), staircase.getCorner1().getX());
                int mny = (int) Math.min(staircase.getCorner0().getY(), staircase.getCorner1().getY());
                int mxx = (int) Math.max(staircase.getCorner0().getX(), staircase.getCorner1().getX());
                int mxy = (int) Math.max(staircase.getCorner0().getY(), staircase.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {

                    this.selectedObjects.add(staircase);
                    return selectedObjects;
                }
            }
        }

        return selectedObjects;
    }


    public ModelFile getModelFile() {
        return this.content;
    }

    public void renameSelected(String newName) {
        assert selected() != null;
        if (selectedObjects.size() == 1) {
            if (newName != null && !newName.isEmpty()) {
                this.selectedObjects.iterator().next().setName(newName);
                hasUnsavedChanges = true;
            }
        }
    }

    public boolean isGroupCreatePossible() {
        if (selected() != null && selected().size() > 1) {
            for (ModelArea area : selected()) {
                if (area instanceof ModelLink) {
                    return false;    // No links in groups
                }
                for (ModelGroup group : currentFloor.getGroups()) {
                    if (group.getAreaIds().contains(area.getId())) {
                        return false;   // No area can be part of groups
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean isGroupRemovePossible() {

        currentGroup = null;
        if (selected() != null && !selected().isEmpty()) {


            for (ModelGroup group : currentFloor.getGroups()) {


                for (ModelArea area : selected()) {
                    if (area instanceof ModelLink) {
                        return false;    // No links in groups
                    }
                    if (group.getAreaIds().contains(area.getId())) {
                        if (currentGroup == null) {
                            currentGroup = group;
                            break;
                        } else {
                            currentGroup = null;
                            return false;
                        }

                    }
                }
            }
            return currentGroup != null;


        }
        return false;
    }

    public void createGroup(String name) {
        if (name != null && !name.isEmpty()) {
            int id = nextObjectId++;
            Collection<Integer> areaIds = new HashSet<Integer>();
            for (ModelArea area : selected()) {
                if (!(area instanceof ModelLink))
                    areaIds.add(area.getId());

            }
            ModelGroup group = new ModelGroup(id, name, areaIds);
            currentFloor.getGroups().add(group);
            currentGroup = null;
            hasUnsavedChanges = true;

        } else {
            System.out.println("Group creation failed");
        }


    }

    public void removeGroup() {
        currentFloor.getGroups().remove(currentGroup);
        currentGroup = null;
        hasUnsavedChanges = true;
    }
}
