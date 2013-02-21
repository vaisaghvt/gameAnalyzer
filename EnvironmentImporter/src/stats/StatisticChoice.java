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
    SIGNIFICANT_VERTEX_VISIT_FREQUENCY("SignificantVertexVisitFrequencyStatisticHandler","Frequency of " +
            " \"significant\" room visits" ,true, true,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_SIGNIFICANT_VERTEX("SignificantVertexVisitDurationStatisticHandler",  "Time spent at \"significant\" vertices", true, true,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_FREQUENCY("VertexVisitFrequencyStatisticHandler","Frequency of visit for each room"
            ,true, true, Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_VERTEX("VertexVisitDurationStatisticHandler",  "Time spent in each room", true,true,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_TIMES("", "Time at which each vertex was visited - Only for internal use",false, false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_FREQUENCY("PathStatisticHandler","Frequency of each path",false, false,
            Phase.TASK_2, Phase.TASK_3),
    DOOR_FREQUENCY("SignificantDoorFrequencyStatisticHandler","Frequency of use of significant doors",true, true,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_HOP_RELATIONSHIP("PathHopStatisticHandler","Total number of hops to path taken scatter plot",false, false,
            Phase.TASK_2, Phase.TASK_3),
    ROOM_REMEMBERED("RoomRememberedStatisticHandler","Correlation of vertex visit to survey answer",false, false,
            Phase.EXPLORATION, Phase.TASK_1),
    STAIRCASE_VISIT_CHANCE("StaircaseVisitStatisticHandler","Frequency of taking a staircase in opp direction",false, false,
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    CORRIDOR_PREFERENCE_MEASURE("CorridorPrefStatisticHandler","Frequency of exploring through a non corridor",false, false,
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DOOR_REPETITION_FREQUENCY("DoorRepetitionFrequencyStatisticHandler","Frequency of deltaT = time diff b/w door usages",true, true,
            Phase.EXPLORATION, Phase.TASK_1),
    NODE_REPETITION_FREQUENCY("NodeRepetitionFrequencyStatisticHandler","Frequency of deltaT = time diff b/w room visits",true, true,
            Phase.EXPLORATION, Phase.TASK_1),
    COVERAGE("CoverageStatisticHandler","Frequency of overall coverage",false, false,
            Phase.EXPLORATION),
    VERTEX_VISIT_HISTOGRAM("VertexVisitCountStatisticHandler","Frequency of normalized visit times",true, false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    ROOM_DURATION_FREQUENCY("RoomDurationTotalFrequencyStatisticHandler","Frequency of time spent per room",true, false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DISTANCE_TIME_EXPLORATION_STATISTIC("DistanceTimeExplorationStatisticHandler","Distance and time taken by each agent - Exploration",false,false),
    DISTANCE_TIME_FOR_TASKS_STATISTIC("DistanceTimeForTasksStatisticHandler","Distance and time taken by each agent - Tasks",false,false),
    TIME_SPENT_PER_VISIT("RoomDurationBreakupFrequencyStatisticHandler","Frequency of time spent per visit",true,false,
        Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    STAT_SUMMARY("GraphDetailsStatisticHandler","Detailed statistics of each agent",true,true),
    PARALLEL_COORDINATE_PLOT("ParallelCoordinatePlotGenerator","Plot on a parallel coordinate plot",false,false),
    ;


    private List<Phase> phases;
    private StatisticsHandler statisticsHandler;
    private final String packageName = "stats.statisticshandlers.";
    private final String description ;
    private boolean canAggregate;
    private boolean hasAggregationChoice;

    StatisticChoice(String handler,String description, boolean canAggregate,boolean hasAggregationChoice, Phase... phases) {

        this.description =description;
        this.canAggregate = canAggregate;
        this.hasAggregationChoice = hasAggregationChoice;

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

    @Override
    public String toString() {
        return description;
    }

    public boolean canAggregate() {
        return canAggregate;
    }

    public boolean hasAggregationChoice() {
        return hasAggregationChoice;
    }
}
