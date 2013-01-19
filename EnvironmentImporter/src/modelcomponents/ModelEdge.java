package modelcomponents;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/14/13
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelEdge {


    private long time;

    public ModelEdge() {

    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return  time+"";
    }
}
