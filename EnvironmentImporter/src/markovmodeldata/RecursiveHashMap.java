package markovmodeldata;

import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwister;
import gui.Phase;

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



    public RecursiveHashMap(int n) {
        assert n >= 1;
        order = n;
        sequenceCodesToSequenceMapping = new HashMap<String, List<String>>();
        if (order == 1) {
            myMap = null;
            firstOrderTable = HashBasedTable.create();
        } else {
            myMap = new HashMap<String, RecursiveHashMap>();
            firstOrderTable = null;
        }
    }

    public void putValue(List<String> states, double value) {
        assert states.size() == order + 1;
        String sequenceCode = findCodeForPath(states);
        if(!this.sequenceCodesToSequenceMapping.containsKey(sequenceCode)){
            this.sequenceCodesToSequenceMapping.put(sequenceCode, states);
        }

        ArrayDeque<String> statesQueue = new ArrayDeque<String>();

        statesQueue.addAll(states);
        recursiveCheckAndPutValue(statesQueue, value);


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

    /**
     * Returns the value associated with a particular path. Before normalization this might be a raw value. Afterwards,
     * it is always a value between 0 and 1.
     *
     * @param stateSequence
     * @return 0 if no such path exists, value associated with path otherwise
     */
    public double getValue(List<String> stateSequence) {
        assert stateSequence.size() == order + 1;
        if (stateSequence.size() < 2) {
            return 0;
        }
        if (stateSequence.size() == 2) {
            assert firstOrderTable != null;
            String penultimateLocation = stateSequence.get(0);
            String destination = stateSequence.get(1);
            if (firstOrderTable.contains(penultimateLocation, destination)) {
                return firstOrderTable.get(penultimateLocation, destination);
            } else {
                return 0;
            }
        } else {
            String oldest = stateSequence.remove(0);
            if (myMap.containsKey(oldest)) {
                return myMap.get(oldest).getValue(stateSequence);
            } else {
                return 0;
            }

        }
    }

    public Set<String> getSequenceCodes() {
        return sequenceCodesToSequenceMapping.keySet();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String sequenceCode : sequenceCodesToSequenceMapping.keySet()) {
            result.append(sequenceCode + "=" + this.getValue(new ArrayList<String>(sequenceCodesToSequenceMapping.get(sequenceCode))) + "\n");
        }
        return result.toString();
    }


    public void normalize() {
        assert order != 0;
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
                myMap.get(source).normalize();
            }
        }
    }

    public HashBasedTable<String, String, Double> obtainHeatMap() {
        HashBasedTable<String, String, Double> result = HashBasedTable.create();
        HashMap<String, Integer> pathsFromSource = new HashMap<String, Integer>();
        HashBasedTable<String, String, Integer> sourceDestinationPathNumberMapping =
                HashBasedTable.create();

        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            List<String> path = sequenceCodesToSequenceMapping.get(code);
            String source = path.get(0);
            String destination = path.get(path.size() - 1);
            if (!pathsFromSource.containsKey(source)) {
                pathsFromSource.put(source, 0);
            }
            pathsFromSource.put(source, pathsFromSource.get(source) + 1);


            if (!sourceDestinationPathNumberMapping.contains(source, destination)) {
                sourceDestinationPathNumberMapping.put(source, destination, 0);
            }
            sourceDestinationPathNumberMapping.put(source, destination,
                    sourceDestinationPathNumberMapping.get(source, destination) + 1);
        }

        for (String source : pathsFromSource.keySet()) {
            int totalNumberOfPathsFromSource = pathsFromSource.get(source);

            for (String destination : sourceDestinationPathNumberMapping.columnKeySet()) {
                if (sourceDestinationPathNumberMapping.contains(source, destination)) {
                    int pathsToDestinationFromSource = sourceDestinationPathNumberMapping.get(source, destination);
                    result.put(source, destination, (double) pathsToDestinationFromSource / (double) totalNumberOfPathsFromSource);
                }
            }

        }

        return result;
    }

    public Map<String, Double> getDestinationProbabilities(List<String> stateSequence) {
        assert stateSequence.size() == order;
        ArrayList<String> newStateSequence = new ArrayList<String>(stateSequence);
        return recursiveGetDestinationProbabilities(newStateSequence);


    }

    private Map<String, Double> recursiveGetDestinationProbabilities(ArrayList<String> stateSequence) {

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


        HashMap<String, Double> pathToProbMapping = new HashMap<String, Double>();

        for (String code : sequenceCodesToSequenceMapping.keySet()) {
            List<String> path = getSequenceForCode(code);
            if (path.get(0).equals(source)) {

                pathToProbMapping.put(code, getValue(path));
            }
        }

        double total = 0;
        for (String key : pathToProbMapping.keySet()) {
            total += pathToProbMapping.get(key);

        }
        for (String key : pathToProbMapping.keySet()) {
            pathToProbMapping.put(key, pathToProbMapping.get(key) / total);
        }

        double value = 0.0;

        double randomDouble = random.nextDouble();

        for (String pathCode : pathToProbMapping.keySet()) {
            value += pathToProbMapping.get(pathCode);

            if (randomDouble < value) {

                return getSequenceForCode(pathCode);
            }
        }


        return null;  //To change body of created methods use File | Settings | File Templates.
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
