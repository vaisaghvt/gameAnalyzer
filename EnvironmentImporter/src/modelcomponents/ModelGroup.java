package modelcomponents;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/17/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelGroup implements ModelObject {
    private Collection<Integer> areaIds;
    private int id;
    private String name;



    public ModelGroup(int id, String name, Collection<Integer> areaIds) {
        this.id =id;
        this.name = name;
        this.areaIds = areaIds;
    }

    public void addArea(Integer areaId) {
        areaIds.add(areaId);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Collection<Integer> getAreaIds() {
        return areaIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelGroup that = (ModelGroup) o;

        if (id != that.id) return false;
        if (!areaIds.equals(that.areaIds)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = areaIds.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        if(name ==null ||name.isEmpty())
            return id + "";
        else
            return name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
