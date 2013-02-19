package gui; /**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */

import database.Database;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MapImagePanel extends MainPanel {
    private static int scale = 6;
    private static int generalOffset = 5;
    private static final long serialVersionUID = 1L;
    private int currentFloor;
    private Document document = null;
    private boolean hasSelection = false;
    private Integer x1 = null;
    private Integer y1 = null;
    private Integer x2 = null;
    private Integer y2 = null;
    private ArrayList<BufferedImage> floorImages = new ArrayList<BufferedImage>();
    private boolean networkView = false;
    private boolean viewImages = true;
    private boolean viewRooms = true;
    private static boolean zoomChanged = false;
    private static MapImagePanel instance;

    private MapImagePanel() {
        currentFloor = 0;
        for (int i = 0; i < Database.getInstance().getNumberOfFloors(); i++) {

            floorImages.add(createFloorImage(Database.getInstance().
                    getImageValuesOfFloor(i), i));

        }

    }


    public void setDocument(Document document) {
        this.document = document;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);


        if (document == null)
            return;


            if (zoomChanged && viewImages) {
                floorImages.clear();
                for (int i = 0; i < Database.getInstance().getNumberOfFloors(); i++) {

                    floorImages.add(createFloorImage(Database.getInstance().
                            getImageValuesOfFloor(i), i));

                }
            }
            assert floorImages != null;

            if (viewImages) {

                g.drawImage(floorImages.get(currentFloor), 0, 0, null);

            }
            if (viewRooms) {
                g = document.image(g);
            }

            if (hasSelection) {
                int minX = Math.min(x1, x2);
                int minY = Math.min(y1, y2);
                int maxX = Math.max(x1, x2);
                int maxY = Math.max(y1, y2);

                g.setColor(Color.RED);
                g.drawRect(minX, minY, maxX - minX, maxY - minY);

            }



        g.dispose();
    }


    private BufferedImage createFloorImage(ArrayList<Point> floorPoints, int floor) {
        int width = Database.getInstance().getWidthOfFloor(floor);
        int height = Database.getInstance().getHeightOfFloor(floor);
        BufferedImage image = new BufferedImage(width * scale + 10, height * scale + 10, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.black);
        for (Point point : floorPoints) {
            Point drawingPoint = MapImagePanel.convertToDrawingCoordinate(point, floor);
            g.drawOval(drawingPoint.x - 1, drawingPoint.y - 1, 2, 2);
        }


        return image;
    }





    public void enableImageView() {
        this.viewImages = true;
    }

    public void disableImageView() {
        this.viewImages = false;
    }



    public void enableRoomView() {
        this.viewRooms = true;
    }

    public void disableRoomView() {
        this.viewRooms = false;
    }

    public void selectFloor(int idx) {
        this.currentFloor = idx;
    }

    public static Point convertToDrawingCoordinate(Point point, int floor) {
        int minx = Database.getInstance().getMinXOfFloor(floor);
        int miny = Database.getInstance().getMinYOfFloor(floor);
        return new Point((point.x - minx) * scale + generalOffset, (point.y - miny) * scale + generalOffset);
    }

    public static Point convertToActualCoordinate(Point point, int floor) {
        int minx = Database.getInstance().getMinXOfFloor(floor);
        int miny = Database.getInstance().getMinYOfFloor(floor);
        return new Point((point.x - generalOffset) / scale + minx,
                (point.y - generalOffset) / scale + miny);
    }

    public static void setZoom(int value) {
        scale = value;
        zoomChanged = true;

    }

    public void setSelection(Integer x1, Integer y1, Integer x2, Integer y2) {
        Point p1 = MapImagePanel.convertToDrawingCoordinate((MapImagePanel.convertToActualCoordinate(new Point(x1, y1), document.getCurrentFloor())), document.getCurrentFloor());
        Point p2 = MapImagePanel.convertToDrawingCoordinate((MapImagePanel.convertToActualCoordinate(new Point(x2, y2), document.getCurrentFloor())), document.getCurrentFloor());
        this.x1 = p1.x;
        this.y1 = p1.y;
        this.x2 = p2.x;
        this.y2 = p2.y;
        hasSelection = true;

    }

    public void unsetSelection() {
        hasSelection = false;
    }

    public static MapImagePanel instance() {
        if(instance==null)
            instance = new MapImagePanel();

        return instance;
    }
}

