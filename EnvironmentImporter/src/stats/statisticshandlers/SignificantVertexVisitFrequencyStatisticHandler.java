package stats.statisticshandlers;

import gui.NetworkModel;
import gui.Phase;
import gui.StatsDialog;
import modelcomponents.CompleteGraph;
import stats.StatisticChoice;
import stats.chartdisplays.SignificantVertexChartDisplay;
import stats.consoledisplays.SignificantVertexConsoleDisplay;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SignificantVertexVisitFrequencyStatisticHandler extends StatisticsHandler<SignificantVertexConsoleDisplay, SignificantVertexChartDisplay> {


    public SignificantVertexVisitFrequencyStatisticHandler() {
        super(new SignificantVertexChartDisplay(),
                new SignificantVertexConsoleDisplay()
        );

    }


    @Override
    public void generateAndDisplayStats(Collection<String> dataNames, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
        GenerateRequiredDataTask task = new GenerateRequiredDataTask(dataNames, StatisticChoice.VERTEX_VISIT_FREQUENCY, phase, allOrOne, aggregationType);
        super.actualGenerateAndDisplay(task);


    }

    private Double aggregate(Double v1, double v2, StatsDialog.AggregationType aggregationType, int n) {
        switch (aggregationType) {

            case SUM:
                return v1 + v2;

            case MEAN:
                return (v1 * (n - 1) + v2) / n;
            case MIN:
                return Math.min(v1, v2);
            case MAX:
                return Math.max(v1, v2);
            default:
                return null;
        }
    }


    private HashMap<String, HashMap<String, Number>> summarizeData(HashMap<String, HashMap<String, Number>> dataNameDataMap,
                                                                   StatsDialog.AggregationType aggregationType) {
        HashMap<String, HashMap<String, Number>> result = new HashMap<String, HashMap<String, Number>>();
        String summaryName = "overall";
        if (aggregationType != null) {

            HashMap<String, Number> initialMap = new HashMap<String, Number>();
            initialMap.put("Library", 0);
            initialMap.put("PictureGallery", 0);
            initialMap.put("Sauna", 0);
            initialMap.put("StartingRoom", 0);
            result.put(summaryName, initialMap);
        }
        int n = 1;
        HashMap<String, Integer> roomEdgeCountMapping = CompleteGraph.instance().getEdgesForEachRoom();
        for (String dataName : dataNameDataMap.keySet()) {

            HashMap<String, Number> dataForPerson = dataNameDataMap.get(dataName);

            double valueForLibrary;

            long library2Value = dataForPerson.get("Library2") == null ? 0 : (dataForPerson.get("Library2")).longValue();
            long libraryGValue = dataForPerson.get("LibraryG") == null ? 0 : (dataForPerson.get("LibraryG")).longValue();
            valueForLibrary = ((double) library2Value / roomEdgeCountMapping.get("Library2")) + ((double) libraryGValue / roomEdgeCountMapping.get("LibraryG"));

            double valueForSauna;
            valueForSauna = dataForPerson.get("Sauna") == null ? 0 : (dataForPerson.get("Sauna")).longValue();
            valueForSauna /= (double) roomEdgeCountMapping.get("Sauna");

            double valueForGallery;
            valueForGallery = dataForPerson.get("Gallery") == null ? 0 : (dataForPerson.get("Gallery")).longValue();
            valueForGallery /= (double) roomEdgeCountMapping.get("Gallery");

            double valueForStart;
            valueForStart = dataForPerson.get("StartingRoom") == null ? 0 : dataForPerson.get("StartingRoom").longValue();
            valueForStart /= (double) roomEdgeCountMapping.get("StartingRoom");
            HashMap<String, Number> tempMap = new HashMap<String, Number>();
            if (aggregationType != null) {
                tempMap = result.get(summaryName);
                tempMap.put("Library", aggregate(tempMap.get("Library").doubleValue(), valueForLibrary, aggregationType, n));
                tempMap.put("PictureGallery", aggregate(tempMap.get("PictureGallery").doubleValue(), valueForGallery, aggregationType, n));
                tempMap.put("Sauna", aggregate(tempMap.get("Sauna").doubleValue(), valueForSauna, aggregationType, n));
                tempMap.put("StartingRoom", aggregate(tempMap.get("StartingRoom").doubleValue(), valueForStart, aggregationType, n));
                result.put(summaryName, tempMap);
                n++;
            } else {
                tempMap.put("Library", valueForLibrary);
                tempMap.put("PictureGallery", valueForGallery);
                tempMap.put("Sauna", valueForSauna);
                tempMap.put("StartingRoom", valueForStart);
                result.put(dataName, tempMap);
            }

        }
        return result;
    }

    class GenerateRequiredDataTask extends AbstractTask {
        private final Phase phase;
        private final StatisticChoice choice;
        HashMap<String, HashMap<String, Number>> dataNameDataMap = new HashMap<String, HashMap<String, Number>>();
        private final StatsDialog.AllOrOne allOrOne;
        private final StatsDialog.AggregationType type;


        public GenerateRequiredDataTask(Collection<String> dataNames, StatisticChoice choice, Phase phase, StatsDialog.AllOrOne allOrOne, StatsDialog.AggregationType aggregationType) {
            super(dataNames);
            this.choice = choice;
            this.phase = phase;
            this.allOrOne = allOrOne;
            this.type = aggregationType;
        }

        @Override
        protected void doTasks(String dataName) {

            dataNameDataMap.put(dataName, NetworkModel.instance().getVertexDataFor(dataName, choice, phase));

        }

        @Override
        protected void summarizeAndDisplay() {
            HashMap<String, HashMap<String, Number>> data = summarizeData(dataNameDataMap,
                    type);


            chartDisplay.setTitle(choice.toString() + " :" + phase.toString());
            chartDisplay.display(data);
            consoleDisplay.display(data);
        }

    }

}
