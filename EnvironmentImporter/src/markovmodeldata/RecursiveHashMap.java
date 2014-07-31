package markovmodeldata;

import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwister;
import modelcomponents.CompleteGraph;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 10/4/13
 * Time: 2:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecursiveHashMap {
    private final HashMap<String, List<String>> sequenceCodesToSequenceMapping;
    private HashMap<String, RecursiveHashMap> myMap;

    /**
     * Only exists when degree =1;
     */
    private HashBasedTable<String, String, Double> firstOrderProbabilityTable;
    private HashBasedTable<String, String, Integer> firstOrderRawValueTable;

    private final int order;
    private HashMap<String, Integer> sequenceOccurrenceCount;
    private HashMap<String, Double> sequenceProbability;


    public RecursiveHashMap(int n) {
        assert n >= 1;
        order = n;
        sequenceCodesToSequenceMapping = new HashMap<String, List<String>>();
        sequenceProbability = new HashMap<String, Double>();
        sequenceOccurrenceCount = new HashMap<String, Integer>();
        if (order == 1) {
            myMap = null;
            firstOrderProbabilityTable = HashBasedTable.create();
            firstOrderRawValueTable = HashBasedTable.create();
        } else {
            myMap = new HashMap<String, RecursiveHashMap>();
            firstOrderProbabilityTable = null;
            firstOrderRawValueTable = null;
        }
    }



    public void putDestinationGivenSeqProbability(List<String> states, double value) {
        assert states.size() == order + 1;
        int initialSize = states.size();


        ArrayDeque<String> statesQueue = new ArrayDeque<String>(states);

        recursiveCheckAndPutValue(statesQueue, value);

        assert states.size() == initialSize;

    }

    public void putSequenceOccurrenceProbability(List<String> states, double value) {
        assert states.size()== order+1;

        String sequenceCode = findCodeForPath(states);
        if (!this.sequenceCodesToSequenceMapping.containsKey(sequenceCode)) {
            this.sequenceCodesToSequenceMapping.put(sequenceCode, states);
        }

        this.sequenceProbability.put(sequenceCode, value);


    }

    private void recursiveCheckAndPutValue(
            ArrayDeque<String> states, double value) {
        assert states.size() > 0;
        String currentOldest = states.pollFirst();
        if (states.size() > 1) {
            if (!myMap.containsKey(currentOldest)) {
                myMap.put(currentOldest, new RecursiveHashMap(order - 1));
            }
            RecursiveHashMap lowerLevelMap = myMap.get(currentOldest);
            lowerLevelMap.recursiveCheckAndPutValue(states, value);
            this.myMap.put(currentOldest, lowerLevelMap);
        } else {
            assert this.order == 1;
            assert this.myMap == null;
            this.firstOrderProbabilityTable.put(currentOldest, states.pollFirst(), value);
        }
    }

    public boolean containsPath(List<String> stateSequence) {
        assert stateSequence.size() == order + 1;
        String code = findCodeForPath(stateSequence);
        return sequenceCodesToSequenceMapping.containsKey(code);
    }


    /**
     * Returns the value associated with a particular path. Before normalization this might be a raw value. Afterwards,
     * it is always a value between 0 and 1.
     *
     * @param stateSequence
     * @return 0 if no such path exists, value associated with path otherwise
     */
    public double getProbabilityOfSequence(List<String> stateSequence) {
        assert stateSequence.size() == order + 1;
        if (!containsPath(stateSequence)) {
            return 0;
        }
        // List<String> sequence = new ArrayList<String>(stateSequence);
        String sequenceCode = findCodeForPath(stateSequence);
        return sequenceProbability.get(sequenceCode);

    }



    public Set<String> getSequenceCodes() {
        return sequenceCodesToSequenceMapping.keySet();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String sequenceCode : sequenceCodesToSequenceMapping.keySet()) {
            result.append(sequenceCode + "=" + this.getProbabilityOfSequence(getSequenceForCode(sequenceCode)) + "\n");
        }
        return result.toString();
    }


    /**
     * Normalizes on the deepest level. Also computes the sequenceOccurences
     * <p/>
     * the table thus becomes the probability of a certain destination given a sequence of vertices
     */
    public void normalize() {
        assert order != 0;


        double total = 0;
        for (String code : sequenceCodesToSequenceMapping.keySet()) {
//            List<String> path = getSequenceForCode(code);
//            int rawValue = (int) getProbabilityOfSequence(path);
//            putSequenceOccurrence(code, rawValue);   // gets the raw value.
            total += sequenceOccurrenceCount.get(code);
        }

        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            double rawValue = (double)  sequenceOccurrenceCount.get(code);
            this.sequenceProbability.put(code, rawValue / total);
        }
        recursiveNormalize();

    }

    private void recursiveNormalize() {
        if (order == 1) {
            for (String source : firstOrderProbabilityTable.rowKeySet()) {
                double totalForSource = 0;
                for (String destination : firstOrderProbabilityTable.columnKeySet()) {
                    if (firstOrderProbabilityTable.contains(source, destination)) {
                        totalForSource += firstOrderProbabilityTable.get(source, destination);
                        firstOrderRawValueTable.put(source, destination, firstOrderProbabilityTable.get(source, destination).intValue());
                    }
                }
                for (String destination : firstOrderProbabilityTable.columnKeySet()) {
                    if (firstOrderProbabilityTable.contains(source, destination)) {
                        firstOrderProbabilityTable.put(source, destination,
                                firstOrderProbabilityTable.get(source, destination) / totalForSource);
                    }
                }
            }
        } else {
            assert myMap != null;
            for (String source : myMap.keySet()) {
                myMap.get(source).recursiveNormalize();
            }
        }

    }

    public HashBasedTable<String, String, Double> obtainHeatMap() {
        HashBasedTable<String, String, Double> result = HashBasedTable.create();
        HashMap<String, Double> probabilityOfPathsFromSource = new HashMap<String, Double>();
        HashBasedTable<String, String, Double> sourceDestinationPathProbabilities =
                HashBasedTable.create();


            for (String code : sequenceCodesToSequenceMapping.keySet()) {
                List<String> path = getSequenceForCode(code);
                String source = path.get(0);
                String destination = path.get(path.size() - 1);
                double probabilityOfPath = sequenceProbability.get(code);
                if (!probabilityOfPathsFromSource.containsKey(source)) {
                    probabilityOfPathsFromSource.put(source, 0.0);
                }
                probabilityOfPathsFromSource.put(source, probabilityOfPathsFromSource.get(source) + probabilityOfPath);


                if (!sourceDestinationPathProbabilities.contains(source, destination)) {
                    sourceDestinationPathProbabilities.put(source, destination, 0.0);
                }
                sourceDestinationPathProbabilities.put(source, destination,
                        sourceDestinationPathProbabilities.get(source, destination) + probabilityOfPath);
            }

            for (String source : probabilityOfPathsFromSource.keySet()) {
                double totalProbabilityOfPathsFromSource = probabilityOfPathsFromSource.get(source);

                for (String destination : sourceDestinationPathProbabilities.columnKeySet()) {
                    if (sourceDestinationPathProbabilities.contains(source, destination)) {
                        double probabilityOfPathsToDestinationFromSource = sourceDestinationPathProbabilities.get(source, destination);
                        result.put(source, destination, probabilityOfPathsToDestinationFromSource / totalProbabilityOfPathsFromSource);
                    }
                }

            }  //TODO : verify this


        return result;
    }

    public Map<String, Double> getDestinationProbabilities(List<String> stateSequence) {
        assert stateSequence.size() == order;
        ArrayList<String> newStateSequence = new ArrayList<String>(stateSequence);
        return recursiveGetDestinationProbabilities(newStateSequence);

    }

    private Map<String, Double> recursiveGetDestinationProbabilities(
            ArrayList<String> sequence) {
        ArrayList<String> stateSequence = new ArrayList<String>(sequence);
        if (stateSequence.size() == 1) {
            assert firstOrderProbabilityTable != null;
            String penultimateLocation = stateSequence.get(0);
            if (firstOrderProbabilityTable.containsRow(penultimateLocation)) {
                return firstOrderProbabilityTable.rowMap().get(penultimateLocation);

            } else {
                return null;
            }
        } else {
            String oldest = stateSequence.remove(0);
            if (myMap.containsKey(oldest)) {
                return myMap.get(oldest).recursiveGetDestinationProbabilities(stateSequence);
            } else {
                return null;
            }

        }
    }


    public int getOrder() {
        return order;
    }

    public List<String> getPathFromRoom(String source, MersenneTwister random) {

        if (sequenceOccurrenceCount == null) {
            System.out.println("Problem!!!! Does not make sense to generate path from estimation");
            assert false;
        }

        HashMap<String, Integer> pathToOccurrenceMapping = new HashMap<String, Integer>();

        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            List<String> path = getSequenceForCode(code);
            if (path.get(0).equals(source)) {

                pathToOccurrenceMapping.put(code, sequenceOccurrenceCount.get(code));
            }
        }

        int total = 0;
        for (String key : pathToOccurrenceMapping.keySet()) {
            total += pathToOccurrenceMapping.get(key);

        }
        HashMap<String, Double> result = new HashMap<String, Double>();
        for (String key : pathToOccurrenceMapping.keySet()) {
            result.put(key, (double) pathToOccurrenceMapping.get(key) / (double) total);
        }

        double value = 0.0;

        double randomDouble = random.nextDouble();

        for (String pathCode : result.keySet()) {
            value += result.get(pathCode);

            if (randomDouble < value) {

                return getSequenceForCode(pathCode);
            }
        }


        return null;
    }

    public String findCodeForPath(List<String> paths) {
        StringBuilder code = new StringBuilder();
        for (String path : paths) {
            code.append(path);
        }
        return code.toString();
    }

    public List<String> getSequenceForCode(String sequenceCode) {
        return new ArrayList<String>(sequenceCodesToSequenceMapping.get(sequenceCode));
    }

    public void incrementSequenceOccurrenceCount(List<String> path) {

        String code = findCodeForPath(path);
        if (!this.sequenceCodesToSequenceMapping.containsKey(code)) {
            this.sequenceCodesToSequenceMapping.put(code, path);
        }


        int currentValue = sequenceOccurrenceCount.containsKey(code) ? sequenceOccurrenceCount.get(code): 0;
        sequenceOccurrenceCount.put(code, currentValue+1);

        //Hack for recursive normalize to work properly
        this.putDestinationGivenSeqProbability(path, currentValue+1);
    }

    public int getNumberOfTakenPaths() {
        return this.sequenceCodesToSequenceMapping.keySet().size();
    }


    public Float getDecisionBase(List<String> stateSequence) {
        assert stateSequence.size() == order;
        ArrayList<String> newStateSequence = new ArrayList<String>(stateSequence);
        return recursiveGetDestinationBase(newStateSequence);

    }

    private Float recursiveGetDestinationBase(
            ArrayList<String> sequence) {
        ArrayList<String> stateSequence = new ArrayList<String>(sequence);
        if (stateSequence.size() == 1) {
            assert firstOrderRawValueTable != null;
            String penultimateLocation = stateSequence.get(0);
            if (firstOrderRawValueTable.containsRow(penultimateLocation)) {
                Map<String, Integer> map = firstOrderRawValueTable.rowMap().get(penultimateLocation);
                int total =0;
                for(String key: map.keySet()){
                    total+=map.get(key);
                }
                return ((float) total/ (float)CompleteGraph.instance().getDegreeOfRoom(CompleteGraph.instance().findRoomByName(penultimateLocation)));
            } else {
                return 0.0f;
            }
        } else {
            String oldest = stateSequence.remove(0);
            if (myMap.containsKey(oldest)) {
                return myMap.get(oldest).recursiveGetDestinationBase(stateSequence);
            } else {
                return 0.0f;
            }

        }
    }

}
