package stats.chartdisplays;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import gui.MapImagePanel;
import gui.NetworkModel;
import javafx.geometry.Point3D;
import modelcomponents.*;
import org.apache.commons.collections15.Transformer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexChartDisplay extends ChartDisplay<HashMap<String, ? extends Number>> {


    private static final int DEFAULT_WIDTH = 40;
    private JFrame mapFrame;
    private HashMap<String, ? extends Number> data;

    @Override
    public void display(HashMap<String, ? extends Number> data) {
        final CategoryDataset dataSet = createDataSet(data);
        final JFreeChart chart = createChart(dataSet);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        createNewFrameAndSetLocation();
        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(chartPanel);
        currentFrame.setVisible(true);

        currentFrame.setSize(new Dimension(520, 300));

        mapFrame = new JFrame("Map comparison");
        this.data = data;

//        JPanel unScaledGraphPane = getGraphPanel(false);
        JPanel scaledGraphPane = getGraphPanel(true);

//        JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                unScaledGraphPane, scaledGraphPane);
//        panel.setDividerLocation(450);
//        panel.setOneTouchExpandable(true);


        mapFrame.getContentPane().add(scaledGraphPane);
        mapFrame.setVisible(true);
        mapFrame.setSize(new Dimension(1800, 900));
    }




    public CategoryDataset createDataSet(HashMap<String, ? extends Number> data) {


//        Collection<String> sortedRoomNames = NetworkModel.instance().getFloorSortedRooms();
        Collection<String> sortedRoomNames = CompleteGraph.instance().getFloorDegreeSortedRooms();

        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (String roomName : sortedRoomNames) {

            if (data.containsKey(roomName)) {
                dataSet.addValue(data.get(roomName), roomName, "");
            }else {
                dataSet.addValue(0, roomName, "");
            }
        }

        return dataSet;

    }


    public JFreeChart createChart(CategoryDataset dataSet) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                this.getTitle(),         // chart title
                "Room",               // domain axis label
                "Value",                  // range axis label
                dataSet,                  // data
                PlotOrientation.VERTICAL, // orientation
                false,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );

        return chart;
    }


    public JPanel getGraphPanel(final boolean scaled) {

        final JPanel requiredPanel= new JPanel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graph<ModelObject, ModelEdge> graph = CompleteGraph.instance().getGraph();
                Transformer<ModelObject, Point2D> areaToPointTransformer = new AreaToLocationTransformer<ModelObject, Point2D>();
                Layout<ModelObject, ModelEdge> layout = new StaticLayout<ModelObject, ModelEdge>(graph,
                        areaToPointTransformer);
//        Layout<ModelObject, ModelEdge> layout = new SpringLayout2<ModelObject, ModelEdge>(this.currentGraph);
////        ((FRLayout2)layout).setMaxIterations(3000);
//        ((FRLayout2)layout).setAttractionMultiplier(2);
//        ((FRLayout2)layout).setRepulsionMultiplier(0.25);


                layout.setSize(new Dimension(1800, 1000));

                VisualizationViewer<ModelObject, ModelEdge> vv = new VisualizationViewer<ModelObject, ModelEdge>(layout);
                vv.setPreferredSize(new Dimension(1900, 1000));
                NetworkModel.scaleToRightAmount(vv, 0.9);

                // Setup up a new vertex to paint transformer...


                // Create a graph mouse and add it to the visualization component
                DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
                gm.setMode(ModalGraphMouse.Mode.PICKING);
                vv.setGraphMouse(gm);


                if(scaled)  {
                    vv.getRenderContext().setVertexShapeTransformer(new dataBasedScaleTransformer<ModelObject, Shape>());
                    vv.getRenderContext().setVertexFillPaintTransformer(new dataBasedColorTransformer<ModelObject, Paint> ());
                }
                else{
                    vv.getRenderContext().setVertexShapeTransformer(new VertexEllipseTransformer<ModelObject, Shape>());
                }
                vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<ModelObject>());
                vv.getRenderer().getVertexLabelRenderer().setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR);


                requiredPanel.add(vv);
                vv.revalidate();
                requiredPanel.getRootPane().revalidate();

            }
        });
        return requiredPanel;
    }

//    private JPanel scaledGraphPanel() {
//    }


    public class AreaToLocationTransformer<ModelObject, Point2D> implements Transformer<ModelObject, Point2D> {

        @Override
        public Point2D transform(ModelObject modelObject) {


            return getCenterOfArea(modelObject);
        }


        private Point2D getCenterOfArea(ModelObject area) {
            if (area instanceof ModelArea) {
                ModelArea room = (ModelArea) area;
                Point3D tempPoint = getCenterOfRoom(room);
                double x = tempPoint.getX();
                double y = tempPoint.getY();
                int floor = (int) tempPoint.getZ();


                Point p = MapImagePanel.convertToDrawingCoordinate(new Point((int) x, (int) y), floor);


                p = new Point((int) (p.getX() +
                        (floor * 700)), (int) p.getY());


                Point2D point = (Point2D) (new java.awt.geom.Point2D.Double(p.x*1.2, p.y*1.2));


                return point;
            }

            ModelGroup group = (ModelGroup) area;
            double sumX = 0;
            double sumY = 0;
            int n = 0;
            int floor = 0;

            for (int areaId : group.getAreaIds()) {


                ModelArea tempArea = CompleteGraph.instance().getRoomForId(areaId);
                Point3D tempPoint = getCenterOfRoom(tempArea);
                double x = tempPoint.getX();
                double y = tempPoint.getY();

                sumX += x;
                sumY += y;

                floor = (int) tempPoint.getZ();
                n++;
            }

            Point p = MapImagePanel.convertToDrawingCoordinate(
                    new Point((int) (sumX / n), (int) (sumY / n)), floor);

            p = new Point((int) (p.getX() + (floor * 700)), (int) p.getY());

            Point2D point = (Point2D) (new java.awt.geom.Point2D.Double(p.x*1.2, p.getY()*1.2));


            return point;
        }

        private Point3D getCenterOfRoom(ModelArea room) {
            Point p1 = room.getCorner0();
            Point p2 = room.getCorner1();

            double x = (p1.getX() + p2.getX()) / 2;
            double y = (p1.getY() + p2.getY()) / 2;
            double z = (double) CompleteGraph.instance().getFloorForArea(room);
            return new Point3D(x, y, z);

        }
    }

    private class VertexEllipseTransformer<ModelObject, Shape> implements Transformer<ModelObject, Shape> {

        @Override
        public Shape transform(ModelObject modelObject) {

            int width = DEFAULT_WIDTH;


            return (Shape) new Ellipse2D.Double(-width/2,-width/2, width, width);


        }
    }

    private class dataBasedScaleTransformer<ModelObject, Shape> implements Transformer<ModelObject, Shape> {
        @Override
        public Shape transform(ModelObject modelObject) {

            int width = DEFAULT_WIDTH;
            if(data.containsKey(modelObject.toString()))
                width *= data.get(modelObject.toString()).doubleValue();
            else
                width *= 0;

            return (Shape) new Ellipse2D.Double(-width/2,-width/2, width, width);


        }
    }

    private class dataBasedColorTransformer<ModelObject, Paint> implements Transformer<ModelObject, Paint> {

        @Override
        public Paint transform(ModelObject modelObject) {
            if(!data.containsKey(modelObject.toString())){
                return (Paint) Color.black;
            }








            if(modelObject.toString().equalsIgnoreCase("Study3")||
                    modelObject.toString().equalsIgnoreCase("B2 Stairs")||
                    modelObject.toString().equalsIgnoreCase("BToDown")||
                    modelObject.toString().equalsIgnoreCase("Start")||
                    modelObject.toString().equalsIgnoreCase("2DormCorr")||
                    modelObject.toString().equalsIgnoreCase("2CorrA2")||
                    modelObject.toString().equalsIgnoreCase("2CorrMain")||
                    modelObject.toString().equalsIgnoreCase("Exit")||
                    modelObject.toString().equalsIgnoreCase("SLP")||
                    modelObject.toString().equalsIgnoreCase("SpecLounge")||
                    modelObject.toString().equalsIgnoreCase("L Study")||
                    modelObject.toString().equalsIgnoreCase("left 2 stair")||
                    modelObject.toString().equalsIgnoreCase("right 2 stair")||
                    modelObject.toString().equalsIgnoreCase("2CorrB2")||
                    modelObject.toString().equalsIgnoreCase("2CorrB1")||
                    modelObject.toString().equalsIgnoreCase("Dine 1")||
                    modelObject.toString().equalsIgnoreCase("Pool")||
                    modelObject.toString().equalsIgnoreCase("Disco")||
                    modelObject.toString().equalsIgnoreCase("Conf")) {


            double value = data.get(modelObject.toString()).doubleValue();
            if(value >1.05){

                return  (Paint) Color.red;
////                return (Paint)new Color((int)Math.min(255, value*255),255, 255);
            } else if(value <0.95){
//
                return  (Paint) Color.green;
            } else {
                return (Paint)Color.white;
            }
            }else{
                return (Paint)Color.white;
            }
        }
    }
}