package stats;

import gui.Phase;
import stats.statisticshandlers.*;

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
    SIGNIFICANT_VERTEX_VISIT_FREQUENCY(new SignificantVertexVisitFrequencyStatisticHandler(), "Frequency of " +
            " \"significant\" room visits", true, true, true,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_SIGNIFICANT_VERTEX(new SignificantVertexVisitDurationStatisticHandler(), "Time spent at \"significant\" vertices", true, true, false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    ROOM_ANALYSIS(new RoomAnalysisHandler(), "Analyse rooms in detail", true, false,true),

    VERTEX_VISIT_FREQUENCY(new VertexVisitFrequencyStatisticHandler(), "Frequency of visit for each room"
            , true, true,true,  Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DOOR_USE_FREQUENCY(new DoorUseFrequencyStatisticHandler(), "Frequency of use of each door"
            , true, true,true,  Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    TIME_SPENT_PER_VERTEX(new VertexVisitDurationStatisticHandler(), "Time spent in each room", true, true, false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    VERTEX_VISIT_TIMES(null, "Time at which each vertex was visited - Only for internal use", false, false,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    PATH_FREQUENCY(new PathStatisticHandler(), "Frequency of each path", false, false,false,
            Phase.TASK_2, Phase.TASK_3),
    DOOR_FREQUENCY(new SignificantDoorFrequencyStatisticHandler(), "Frequency of use of significant doors", true, true,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    ROOM_REMEMBERED(new RoomRememberedStatisticHandler(), "Correlation of vertex visit to survey answer", false, false,false,
            Phase.EXPLORATION, Phase.TASK_1),
    STAIRCASE_VISIT_CHANCE(new StaircaseVisitStatisticHandler(), "Frequency of taking a staircase in opp direction", false, false,false,
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    CORRIDOR_PREFERENCE_MEASURE(new CorridorPrefStatisticHandler(), "Frequency of exploring through a non corridor", false, false, false,
            Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DOOR_REPETITION_FREQUENCY(new DoorRepetitionFrequencyStatisticHandler(), "Frequency of deltaT = time diff b/w door usages", true, true,false,
            Phase.EXPLORATION, Phase.TASK_1),
    NODE_REPETITION_FREQUENCY(new NodeRepetitionFrequencyStatisticHandler(), "Frequency of deltaT = time diff b/w room visits", true, true,false,
            Phase.EXPLORATION, Phase.TASK_1),
    COVERAGE(new CoverageStatisticHandler(), "Frequency of overall coverage", false, false,false,
            Phase.EXPLORATION),
    VERTEX_VISIT_HISTOGRAM(new VertexVisitCountStatisticHandler(), "Frequency of normalized visit times", true, false,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    ROOM_DURATION_FREQUENCY(new RoomDurationTotalFrequencyStatisticHandler(), "Frequency of time spent per room", true, false,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    DISTANCE_TIME_EXPLORATION_STATISTIC(new DistanceTimeExplorationStatisticHandler(), "Distance and time taken by each agent - Exploration", false, false, false),
    DISTANCE_TIME_FOR_TASKS_STATISTIC(new DistanceTimeForTasksStatisticHandler(), "Distance and time taken by each agent - Tasks", false, false, false),
    TIME_SPENT_PER_VISIT(new RoomDurationBreakupFrequencyStatisticHandler(), "Frequency of time spent per visit", true, false,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),
    STAT_SUMMARY(new GraphDetailsStatisticHandler(), "Detailed statistics of each agent", true, true, false),
    PARALLEL_COORDINATE_PLOT(new ParallelCoordinatePlotGenerator(), "Plot on a parallel coordinate plot", false, false,false),
    HEAT_MAP(new NthOrderHeatMapHandler(), "Get markov data related stats", false, false,false,
            Phase.EXPLORATION, Phase.TASK_1, Phase.TASK_2, Phase.TASK_3),;


    private List<Phase> phases;
    private StatisticsHandler statisticsHandler;
    private final String description;
    private boolean canAggregate;
    private boolean hasAggregationChoice;
    private boolean randomWalkMeasurePossible;

    StatisticChoice(StatisticsHandler handler, String description, boolean canAggregate, boolean hasAggregationChoice, boolean randomWalkPossible, Phase... phases) {

        this.description = description;
        this.canAggregate = canAggregate;
        this.randomWalkMeasurePossible = randomWalkPossible;
        this.hasAggregationChoice = hasAggregationChoice;

        this.phases = new ArrayList<Phase>();
        for (Phase phase : phases) {
            this.phases.add(phase);
        }

        this.statisticsHandler = handler;

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

    public boolean isRandomWalkMeasurePossible() {
        return randomWalkMeasurePossible;
    }
}
