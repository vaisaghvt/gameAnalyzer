package stats;

import gui.Phase;
import stats.statisticshandlers.StatisticsHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/19/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public enum StatisticChoice {
    VERTEX_VISIT_FREQUENCY("VertexVisitFrequencyStatisticHandler", Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_VERTEX("VertexVisitDurationStatisticHandler", Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_TIMES("", Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_FREQUENCY("PathStatisticHandler", Phase.TASK_2, Phase.TASK_3),
    DOOR_FREQUENCY("DoorFrequencyStatisticHandler", Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_HOP_RELATIONSHIP("PathHopStatisticHandler", Phase.TASK_2, Phase.TASK_3),
    ROOM_REMEMBERED("RoomRememberedStatisticHandler", Phase.EXPLORATION, Phase.TASK_1),
    STAIRCASE_VISIT_CHANCE("StaircaseVisitStatisticHandler", Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    CORRIDOR_PREFERENCE_MEASURE("CorridorPrefStatisticHandler", Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    REPETITION_FREQUENCY("RepetitionFrequencyStatisticHandler", Phase.EXPLORATION, Phase.TASK_1);

    private List<Phase> phases;
    private StatisticsHandler statisticsHandler;
    private final String packageName = "stats.statisticshandlers.";

    StatisticChoice(String handler, Phase... phases) {


        this.phases = new ArrayList<Phase>();
        for (Phase phase : phases) {
            this.phases.add(phase);
        }

        if (!handler.isEmpty()) {
            Object[] args = new Object[0];              // Change these two if you need to pass arguments to your motion plannign constructor
            Class<?>[] classes = new Class<?>[0];      // Change these two if you want to pass arguments to your motion planning constructor
            try {

                this.statisticsHandler = (StatisticsHandler) Class.forName(packageName+handler).getConstructor(classes).newInstance(args);

            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        } else{
            System.out.println("No handler for this choice yet!");
            this.statisticsHandler = null;
        }
    }

    public Collection<Phase> getPhases() {
        return this.phases;
    }

    public StatisticsHandler getStatisticsHandler() {
        return this.statisticsHandler;
    }
}
