package gui;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/17/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Phase {
    TASK_1(2), TASK_2(3), TASK_3(4), EXPLORATION(1);
    private int num;

    Phase(int i) {
        this.num=i;
    }

    public int getNum() {
        return num;
    }
}
