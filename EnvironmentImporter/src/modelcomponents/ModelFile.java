package modelcomponents;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 7:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelFile {
    private String name;
    private String path;
    private ArrayList<ModelFloor> floors;
    private HashSet<ModelStaircaseGroup> staircaseGroups;
    private final String headerText = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";

    public ModelFile(String name, String path) {
        this.name = name;
        this.path = path;
        floors = new ArrayList<ModelFloor>();
        staircaseGroups = new HashSet<ModelStaircaseGroup>();
    }

    public ModelFile(File file) {
        floors = new ArrayList<ModelFloor>();
        staircaseGroups = new HashSet<ModelStaircaseGroup>();
        try {
            Scanner sc = new Scanner(file);

            String firstLine = sc.nextLine().trim();
            assert verifyFirstLine(firstLine);
            String modelTag = sc.nextLine().trim();

            assert getTagName(modelTag).equals("BuildingModel");
            HashMap<String, String> attribMap = getAttributes(modelTag);
            this.name = attribMap.get("name");
            this.path = attribMap.get("path");

            while (true) {
                String nextTag = sc.nextLine().trim();
//                System.out.println(nextTag);
                String tagName = getTagName(nextTag);
                if (isEndingTag(nextTag) && tagName.equals("BuildingModel")) {
                    break;
                } else if (tagName.equals("floor")) {
                    floors.add(initializeFloorInfo(sc));

                } else if (tagName.equals("staircaseGroups")) {
                    attribMap = getAttributes(modelTag);
                    String staircaseGroupName = attribMap.get("name");
                    staircaseGroups.add(initializeStaircaseGroupInfo(sc,
                            staircaseGroupName));

                } else {
                    assert false;
                }

            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private ModelStaircaseGroup initializeStaircaseGroupInfo(Scanner sc, String staircaseGroupName) {
        ModelStaircaseGroup result = new ModelStaircaseGroup(staircaseGroupName);

        String tag = sc.nextLine().trim();
        result.setId(getIdFromIdTag(tag));


        for (int i = 0; i < 2; i++) {
            tag = sc.nextLine().trim();
            result.getStaircaseIds().add(parseInt(valueOfTag("staircaseId", tag)));
        }


        tag = sc.nextLine().trim();
        assert isEndingTag(tag) && getTagName(tag).equals("staircaseGroups");


        return result;
    }

    private ModelFloor initializeFloorInfo(Scanner sc) {

        String tag = sc.nextLine().trim();

        int id = getIdFromIdTag(tag);
        Collection<ModelRoom> rooms = new HashSet<ModelRoom>();
        Collection<ModelLink> links = new HashSet<ModelLink>();
        Collection<ModelStaircase> staircases = new HashSet<ModelStaircase>();
        while (sc.hasNext()) {
            String nextTag = sc.nextLine().trim();
            String tagName = getTagName(nextTag);
            if (isEndingTag(nextTag) && tagName.equals("floor")) {
                return new ModelFloor(id, rooms, links, staircases);
            } else if (tagName.equals("room")) {
                rooms.add(getRoomInfo(sc));
            } else if (tagName.equals("link")) {
                links.add(getLinkInfo(sc));
            } else if (tagName.equals("staircase")) {
                staircases.add(getStaircaseInfo(sc));
            } else {
                assert false;
            }
        }
        assert false;
        return null;

    }

    private ModelStaircase getStaircaseInfo(Scanner sc) {
        String tag = sc.nextLine().trim();
        int id = getIdFromIdTag(tag);

        tag = sc.nextLine().trim();

        String tagName = getTagName(tag);
        String name= "";
        if("name".equals(tagName)){

            name = valueOfTag("name", tag);
            tag = sc.nextLine().trim();
        }

        tagName = getTagName(tag);
        HashMap<String, String> attribs = getAttributes(tag);
        assert tagName.equals("corner0");
        Point corner0 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        tag = sc.nextLine().trim();
        tagName = getTagName(tag);
        attribs = getAttributes(tag);
        assert tagName.equals("corner1");
        Point corner1 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        tag = sc.nextLine().trim();
        assert isEndingTag(tag) && getTagName(tag).equals("staircase");
        ModelStaircase staircase = new ModelStaircase(id, corner0, corner1);
        staircase.setName(name);
        return staircase;

    }

    private ModelLink getLinkInfo(Scanner sc) {
        String tag = sc.nextLine().trim();
        int id = getIdFromIdTag(tag);

        tag = sc.nextLine().trim();
        String tagName = getTagName(tag);
        HashMap<String, String> attribs = getAttributes(tag);
        assert tagName.equals("corner0");
        Point corner0 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        tag = sc.nextLine().trim();
        tagName = getTagName(tag);
        attribs = getAttributes(tag);
        assert tagName.equals("corner1");
        Point corner1 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        Collection<Integer> connectingRooms = new HashSet<Integer>();
        for (int i = 0; i < 2; i++) {
            tag = sc.nextLine().trim();
            connectingRooms.add(getConnectingAreaId(tag));
        }


        tag = sc.nextLine().trim();
        assert isEndingTag(tag) && getTagName(tag).equals("link");


        return new ModelLink(id, corner0, corner1, connectingRooms);

    }


    private ModelRoom getRoomInfo(Scanner sc) {
        String tag = sc.nextLine().trim();
        int id = getIdFromIdTag(tag);

        tag = sc.nextLine().trim();

//        System.out.println(id);
        String tagName = getTagName(tag);

        String name= "";
//        System.out.println(tagName);
        if("name".equals(tagName)){

            name = valueOfTag("name", tag);
            tag = sc.nextLine().trim();
        }
//        System.out.println(tag);

        tagName = getTagName(tag);
        HashMap<String, String> attribs = getAttributes(tag);
//        System.out.println(attribs);
        assert "corner0".equals(tagName);
        Point corner0 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        tag = sc.nextLine().trim();
        tagName = getTagName(tag);
        attribs = getAttributes(tag);
        assert tagName.equals("corner1");
        Point corner1 = new Point(
                parseInt(attribs.get("x")),
                parseInt(attribs.get("y")));

        tag = sc.nextLine().trim();
        assert isEndingTag(tag) && getTagName(tag).equals("room");
        ModelRoom result = new ModelRoom(id, corner0, corner1);
        result.setName(name);
        return result;
    }

    private int getIdFromIdTag(String tag) {
        return parseInt(valueOfTag("id", tag));

    }

    private Integer getConnectingAreaId(String tag) {
        return parseInt(valueOfTag("connectingArea", tag));
    }

    private String valueOfTag(String tagName, String tag) {
        return tag
                .replace("<" + tagName + ">", "")
                .replace("</" + tagName + ">", "");
    }


    private HashMap<String, String> getAttributes(String tag) {
        String pureString = removeAngularBrackets(tag);
        String[] brokenTag = pureString.split("\\s");
        HashMap<String, String> result = new HashMap<String, String>();
        for (int i = 1; i < brokenTag.length; i++) {
            String[] parts = brokenTag[i].split("=");
            result.put(parts[0], parts[1].substring(1, parts[1].length() - 1));
        }
        return result;
    }

    private boolean isEndingTag(String tag) {
        return tag.charAt(1) == '/';

    }

    private String getTagName(String tag) {
        String pureString = removeAngularBrackets(tag);
        String[] brokenTag = pureString.split("\\s");
        return brokenTag[0];

    }

    private String removeAngularBrackets(String st) {
//        String result = st.substring(1, st.length() - 1);
//        if (result.charAt(result.length() - 1) == '/') {
//            return result.substring(0, result.length() - 1);
//        } else if (result.charAt(0) == '/') {
//            return result.substring(1, result.length());
//        } else {
//            return result;
//        }
        return st.replaceAll("</"," ").replaceAll("<"," ").replaceAll("/>","").replaceAll(">"," ").trim();
    }

    private boolean verifyFirstLine(String firstLine) {
        firstLine = firstLine + "\n";
        return firstLine.equals(headerText);
    }

    public ArrayList<ModelFloor> getFloors() {
        return floors;
    }

    public String getName() {
        return name;
    }

    public Collection<ModelStaircaseGroup> getStaircaseGroups() {
        return staircaseGroups;
    }

    public String convertToXML() {
        StringBuilder result = new StringBuilder();
        result.append(headerText);
        result.append(getDescriptorText());
        result.append(writeFloors());
        result.append(writeStaircaseGroups());
        result.append(endTag("BuildingModel"));

        return result.toString();
    }

    private String writeStaircaseGroups() {
        StringBuilder result = new StringBuilder();
        for (ModelStaircaseGroup group : this.getStaircaseGroups()) {
            HashMap<String, String> attribMap = new HashMap<String, String>();
            attribMap.put("name", group.getName());
            result.append("\t").append(startTag("staircaseGroups", attribMap, false));

            result.append(writeId(2, group.getId()));
            for (Integer id : group.getStaircaseIds()) {
                result.append(writeTag(2, "staircaseId", id.toString(), null));
            }
            result.append("\t").append(endTag("staircaseGroups"));
        }
        return result.toString();

    }

    private String writeTag(int level, String tag, String value, HashMap<String, String> attributeMap) {
        StringBuilder result = new StringBuilder();
        while (level > 0) {
            result.append("\t");
            level--;
        }

        result.append(startTag(tag, attributeMap, value == null));
        if (value != null) {
            result.append(value);
            result.append(endTag(tag));
        }
        return result.toString();
    }

    private String startTag(String tag, HashMap<String, String> attributeMap, boolean isEmpty) {
        StringBuilder result = new StringBuilder();
        result.append("<").append(tag);
        if (attributeMap != null) {
            for (String key : attributeMap.keySet()) {
                result.append(" ").append(key).append("=\"");
                result.append(attributeMap.get(key)).append("\"");
            }
        }
        if (isEmpty) {
            result.append("/");
        }
        result.append(">");
        if (attributeMap != null) {
            result.append("\n");
        }
        return result.toString();
    }

    private String endTag(String tag) {
        return "</" + tag + ">\n";
    }


    private String writeId(int level, Integer id) {
        return writeTag(level, "id", id.toString(), null);

    }

    private String writeName(int level, String name) {
        return writeTag(level, "name", name, null);
    }

    private String writeFloors() {
        StringBuilder result = new StringBuilder();
        for (ModelFloor floor : this.getFloors()) {
            result.append("\t");
            result.append(startTag("floor", null, false)).append("\n");
            result.append(writeId(2, floor.getId()));

            result.append(writeRooms(floor, 2));
            result.append(writeLinks(floor, 2));
            result.append(writeStaircases(floor, 2));
            result.append("\t");
            result.append(endTag("floor"));
        }
        return result.toString();
    }

    private String writeRooms(ModelFloor floor, int level) {
        StringBuilder result = new StringBuilder();

        for (ModelRoom room : floor.getRooms()) {
            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(startTag("room", null, false)).append("\n");
            result.append(writeId(level + 1, room.getId()));
            if(room.getName()!=null && !room.getName().isEmpty())
                result.append(writeName(level+1, room.getName()));

            HashMap<String, String> point = getPointAsMap(room.getCorner0());
            result.append(writeTag(level + 1, "corner0", null, point));

            point = getPointAsMap(room.getCorner1());
            result.append(writeTag(level + 1, "corner1", null, point));

            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(endTag("room"));
        }
        return result.toString();
    }



    private HashMap<String, String> getPointAsMap(Point corner0) {
        Long x = Math.round(corner0.getX());
        Long y = Math.round(corner0.getY());
        HashMap<String, String> pointMap = new HashMap<String, String>();
        pointMap.put("x", x.toString());
        pointMap.put("y", y.toString());
        return pointMap;
    }

    private String writeLinks(ModelFloor floor, int level) {
        StringBuilder result = new StringBuilder();

        for (ModelLink link : floor.getLinks()) {
            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(startTag("link", null, false)).append("\n");
            result.append(writeId(level + 1, link.getId()));

            HashMap<String, String> point = getPointAsMap(link.getCorner0());
            result.append(writeTag(level + 1, "corner0", null, point));

            point = getPointAsMap(link.getCorner1());
            result.append(writeTag(level + 1, "corner1", null, point));

            for (Integer id : link.getConnectingAreas()) {
                result.append(writeTag(level + 1, "connectingArea", id.toString(), null));
            }

            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(endTag("link"));
        }
        return result.toString();

    }

    private String writeStaircases(ModelFloor floor, int level) {
        StringBuilder result = new StringBuilder();

        for (ModelStaircase staircase : floor.getStaircases()) {
            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(startTag("staircase", null, false)).append("\n");
            result.append(writeId(level + 1, staircase.getId()));
            if(staircase.getName()!=null && !staircase.getName().isEmpty())
                result.append(writeName(level+1, staircase.getName()));

            HashMap<String, String> point = getPointAsMap(staircase.getCorner0());
            result.append(writeTag(level + 1, "corner0", null, point));

            point = getPointAsMap(staircase.getCorner1());
            result.append(writeTag(level + 1, "corner1", null, point));

            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(endTag("staircase"));
        }
        return result.toString();

    }


    private String getDescriptorText() {
        return "<BuildingModel " +
                "path=\"" + this.path
                + "\" name=\"" + this.name +
                "\">\n";
    }
}
