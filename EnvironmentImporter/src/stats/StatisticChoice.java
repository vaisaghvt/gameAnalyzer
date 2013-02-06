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
    SIGNIFICANT_VERTEX_VISIT_FREQUENCY("SignificantVertexVisitFrequencyStatisticHandler","Find the visit frequency for" +
            " the \"significant\" rooms"
            , Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_SIGNIFICANT_VERTEX("SignificantVertexVisitDurationStatisticHandler",  "Time spent at the significant vertices",
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_FREQUENCY("VertexVisitFrequencyStatisticHandler","Find the visit frequency for" +
            " the rooms"
            , Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_VERTEX("VertexVisitDurationStatisticHandler",  "Time spent at the vertices",
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_TIMES("", "Time spent at the significant vertices",
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_FREQUENCY("PathStatisticHandler","Histogram of paths",
            Phase.TASK_2, Phase.TASK_3),
    DOOR_FREQUENCY("SignificantDoorFrequencyStatisticHandler","Frequency of visit to significant doors",
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_HOP_RELATIONSHIP("PathHopStatisticHandler","Total number of hops to path taken scatter plot",
            Phase.TASK_2, Phase.TASK_3),
    ROOM_REMEMBERED("RoomRememberedStatisticHandler","Correlation of experience to survey answer",
            Phase.EXPLORATION, Phase.TASK_1),
    STAIRCASE_VISIT_CHANCE("StaircaseVisitStatisticHandler","Chances of visit to a staircase",
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    CORRIDOR_PREFERENCE_MEASURE("CorridorPrefStatisticHandler","Chances of preference for a corridor",
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DOOR_REPETITION_FREQUENCY("DoorRepetitionFrequencyStatisticHandler","the histogram of door usage repetition frequency",
            Phase.EXPLORATION, Phase.TASK_1),
    NODE_REPETITION_FREQUENCY("NodeRepetitionFrequencyStatisticHandler","The histogram of node usage repetition frequency",
            Phase.EXPLORATION, Phase.TASK_1),
    COVERAGE("CoverageStatisticHandler","The coverage of each agent",
            Phase.EXPLORATION),
    DISTANCE_TIME_STATISTIC("DistanceTimeStatisticHandler","Display distance and time taken by each agent");

    private List<Phase> phases;
    private StatisticsHandler statisticsHandler;
    private final String packageName = "stats.statisticshandlers.";
    private final String description ;

    StatisticChoice(String handler,String description, Phase... phases) {

        this.description =description;

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
