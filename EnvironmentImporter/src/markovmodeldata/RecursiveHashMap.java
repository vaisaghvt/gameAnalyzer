package markovmodeldata;

import com.google.common.collect.HashBasedTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 10/4/13
 * Time: 2:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecursiveHashMap {
    private HashMap<String, RecursiveHashMap> myMap;

    /**
     * Only exists when degree =1;
     */
    private HashBasedTable<String, String, Double> firstOrderTable;

    private final int order;
    private List<List<String>> sequences;

    public RecursiveHashMap(int n) {
        assert n >= 1;
        order = n;
        sequences = new ArrayList<List<String>>();
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
        this.sequences.add(states);

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

    public List<List<String>> getSequences() {
        return sequences;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (List<String> sequence : sequences) {
            result.append(sequence + "=" + this.getValue(sequence) + "\n");
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

    public HashBasedTable obtainHeatMap() {
        HashBasedTable<String, String, Double> result = HashBasedTable.create();
        HashMap<String, Integer> pathsFromSource = new HashMap<String, Integer>();
        HashBasedTable<String, String, Integer> sourceDestinationPathNumberMapping =
                HashBasedTable.create();

        for (List<String> path : sequences) {
            String source = path.get(0);
            String destination = path.get(path.size() - 1);
            if(!pathsFromSource.containsKey(source)){
                pathsFromSource.put(source, 0);
            }
            pathsFromSource.put(source, pathsFromSource.get(source)+1);


            if (!sourceDestinationPathNumberMapping.contains(source, destination)){
                sourceDestinationPathNumberMapping.put(source, destination, 0);
            }
            sourceDestinationPathNumberMapping.put(source, destination,
                    sourceDestinationPathNumberMapping.get(source, destination)+1);
        }

        for (String source : pathsFromSource.keySet()) {
            int totalNumberOfPathsFromSource = pathsFromSource.get(source);

            for (String destination : sourceDestinationPathNumberMapping.columnKeySet()) {
                if(sourceDestinationPathNumberMapping.contains(source, destination)){
                    int pathsToDestinationFromSource = sourceDestinationPathNumberMapping.get(source, destination);
                    result.put(source, destination, (double) pathsToDestinationFromSource / (double) totalNumberOfPathsFromSource);
                }
            }

        }

        return result;
    }
}
