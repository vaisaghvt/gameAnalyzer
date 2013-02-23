package modelcomponents;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 7:46 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelArea implements ModelObject {
    Point corner0;
    Point corner1;
    int id;
    String name;

    public ModelArea(int id, Point corner0, Point corner1) {
        this.id = id;
        this.corner0 = corner0;
        this.corner1 = corner1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Point getCorner0() {
        return corner0;
    }

    public Point getCorner1() {
        return corner1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelArea)) return false;

        ModelArea modelArea = (ModelArea) o;

        if (id != modelArea.id) return false;
        if (!corner0.equals(modelArea.corner0)) return false;
        if (!corner1.equals(modelArea.corner1)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = corner0.hashCode();
        result = 31 * result + corner1.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        if (name == null || name.isEmpty())
            return id + "";
        else
            return name;
    }
}
