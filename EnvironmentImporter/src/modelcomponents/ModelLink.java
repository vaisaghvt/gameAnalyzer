package modelcomponents;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelLink extends ModelArea {
    private ArrayList<Integer> connectingAreas;

    public ModelLink(int id, Point corner0, Point corner1, Collection<Integer> connectingRooms) {
        super(id, corner0, corner1);
        this.connectingAreas = new ArrayList<Integer>();
        this.connectingAreas.addAll(connectingRooms);
    }

    public ArrayList<Integer> getConnectingAreas() {
        return connectingAreas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ModelLink modelLink = (ModelLink) o;

        if (!connectingAreas.equals(modelLink.connectingAreas)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + connectingAreas.hashCode();
        return result;
    }
}
