package markovmodeldata;

import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwister;

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
    private HashBasedTable<String, String, Double> firstOrderTable;

    private final int order;
    private HashMap<String, Integer> sequenceOccurrenceCount;
    private HashMap<String, Double> codeToValueMapping;


    public RecursiveHashMap(int n) {
        assert n >= 1;
        order = n;
        sequenceCodesToSequenceMapping = new HashMap<String, List<String>>();
        codeToValueMapping = new HashMap<String, Double>();
        if (order == 1) {
            myMap = null;
            firstOrderTable = HashBasedTable.create();
        } else {
            myMap = new HashMap<String, RecursiveHashMap>();
            firstOrderTable = null;
        }
    }

    /**
     * Only for highest level
     * As the name suggests it puts in the value and looses the old value
     */
    private void putSequenceOccurrence(String pathCode, int value) {
//        assert states.size() == order + 1;

        this.sequenceOccurrenceCount.put(pathCode, value);


    }

    public void putValue(List<String> states, double value) {
        assert states.size() == order + 1;
        int initialSize = states.size();
        String sequenceCode = findCodeForPath(states);
        if (!this.sequenceCodesToSequenceMapping.containsKey(sequenceCode)) {
            this.sequenceCodesToSequenceMapping.put(sequenceCode, states);
        }


        ArrayDeque<String> statesQueue = new ArrayDeque<String>(states);

        recursiveCheckAndPutValue(statesQueue, value);

        this.codeToValueMapping.put(sequenceCode, value);
        assert states.size() == initialSize;

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
            this.firstOrderTable.put(currentOldest, states.pollFirst(), value);
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
        return getValueForCode(sequenceCode);

    }

    private double getValueForCode(String sequenceCode) {
        return codeToValueMapping.get(sequenceCode);
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

        sequenceOccurrenceCount = new HashMap<String, Integer>();
        double total = 0;
        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            List<String> path = getSequenceForCode(code);
            int rawValue = (int) getProbabilityOfSequence(path);
            putSequenceOccurrence(code, rawValue);   // gets the raw value.
            total += rawValue;
        }

        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            List<String> path = getSequenceForCode(code);
            double rawValue = (double) getProbabilityOfSequence(path);
            this.codeToValueMapping.put(code, rawValue / total);
        }
        recursiveNormalize();

    }

    private void recursiveNormalize() {
        if (order == 1) {
            for (String source : firstOrderTable.rowKeySet()) {
                double totalForSource = 0;
                for (String destination : firstOrderTable.columnKeySet()) {
                    if (firstOrderTable.contains(source, destination)) {
                        totalForSource += firstOrderTable.get(source, destination);

                    }
                }
                for (String destination : firstOrderTable.columnKeySet()) {
                    if (firstOrderTable.contains(source, destination)) {
                        firstOrderTable.put(source, destination,
                                firstOrderTable.get(source, destination) / totalForSource);
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
                double probabilityOfPath = getValueForCode(code);
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
            assert firstOrderTable != null;
            String penultimateLocation = stateSequence.get(0);
            if (firstOrderTable.containsRow(penultimateLocation)) {
                return firstOrderTable.rowMap().get(penultimateLocation);
            } else {
                return null;
            }
        } else {
            String oldest = stateSequence.remove(0);
            if (myMap.containsKey(oldest)) {
                return myMap.get(oldest).getDestinationProbabilities(stateSequence);
            } else {
                return null;
            }

        }
    }

    public Map<String, Double> getDestinationProbabilities(String startRoom) {
        List<String> singletonList = new ArrayList<String>(1);
        singletonList.add(startRoom);
        return this.getDestinationProbabilities(singletonList);
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
}
