package stats.chartdisplays;

import stats.StatisticsDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ChartDisplay<T> implements StatisticsDisplay<T>, ComponentListener {


    private String title;
    protected JFrame currentFrame;
    private String name;
    private static HashMap<String, Point> cachedFrameLocation = new HashMap<String, Point>();
    private HashMap<JFrame, String> frameToNameMapping = new HashMap<JFrame, String>();

    protected ChartDisplay() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String st) {
        this.title = st;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void createNewFrameAndSetLocation() {

        if (name != null) {
            if (cachedFrameLocation.containsKey(name)) {
                currentFrame = new JFrame();
                this.frameToNameMapping.put(currentFrame, name);
                currentFrame.addComponentListener(this);
                currentFrame.setLocation(cachedFrameLocation.get(name).x, cachedFrameLocation.get(name).y);
            } else {
                currentFrame = new JFrame();
                this.frameToNameMapping.put(currentFrame, name);
                currentFrame.addComponentListener(this);
                currentFrame.setLocation(100, 100);
            }
        } else {
            currentFrame = new JFrame();


            currentFrame.setLocation(100, 100);
        }
    }


    @Override
    public void componentResized(ComponentEvent componentEvent) {

    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {

        Point point = new Point(componentEvent.getComponent().getX(), componentEvent.getComponent().getY());
        if (!(point.getX() == 100 && point.getY() == 100) && this.frameToNameMapping.get(componentEvent.getSource()) != null) {

            cachedFrameLocation.put(this.frameToNameMapping.get(componentEvent.getSource()), point);
        }

    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {

    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }

    @Override
    public void display(T item) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
