package modelcomponents;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelFloor implements ModelObject {
    private Collection<ModelRoom> rooms;
    private Collection<ModelLink> links;
    private Collection<ModelStaircase> staircases;
    private int id;
    private Collection<ModelGroup> groups;

    public ModelFloor(int id, Collection<ModelRoom> rooms, Collection<ModelLink> links, Collection<ModelStaircase> staircases, Collection<ModelGroup> groups) {
        this.id = id;
        this.rooms = rooms;
        this.links = links;
        this.staircases = staircases;
        this.groups = groups;


    }

    public ModelFloor() {
        this.rooms = new HashSet<ModelRoom>();
        this.links = new HashSet<ModelLink>();
        this.staircases = new HashSet<ModelStaircase>();
        this.groups = new HashSet<ModelGroup>();
    }

    public Collection<ModelRoom> getRooms() {
        return rooms;
    }

    public Collection<ModelLink> getLinks() {
        return links;
    }

    public Collection<ModelStaircase> getStaircases() {
        return staircases;
    }


    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelFloor that = (ModelFloor) o;

        if (id != that.id) return false;
        if (!links.equals(that.links)) return false;
        if (!rooms.equals(that.rooms)) return false;
        if (!staircases.equals(that.staircases)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rooms.hashCode();
        result = 31 * result + links.hashCode();
        result = 31 * result + staircases.hashCode();
        result = 31 * result + id;
        return result;
    }

    public Collection<ModelGroup> getGroups() {
        return this.groups;
    }
}
