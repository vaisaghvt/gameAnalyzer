package modelcomponents;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelStaircaseGroup implements ModelObject {
    private String name;
    private int id;
    private Collection<Integer> staircaseIds = new HashSet<Integer>();

    public ModelStaircaseGroup(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Integer> getStaircaseIds() {
        return this.staircaseIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelStaircaseGroup that = (ModelStaircaseGroup) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (staircaseIds != null ? !staircaseIds.equals(that.staircaseIds) : that.staircaseIds != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (staircaseIds != null ? staircaseIds.hashCode() : 0);
        return result;
    }
}
