package markovmodeldata;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 10/4/13
 * Time: 2:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecursiveHashMap {
    private HashMap<String, RecursiveHashMap> myMap;
    private String key;
    private double value;
    private final int order;
    private List<List<String>> sequences;

    public RecursiveHashMap(int n){
        order =n;
        sequences = new ArrayList<List<String>>();
        if(order == 0){
            myMap =null;
        }else {
            myMap = new HashMap<String, RecursiveHashMap>();

        }
    }

    public void put(List<String> states, double value){
        assert states.size() ==order+1;
        this.sequences.add(states);
        
        ArrayDeque<String> statesQueue = new ArrayDeque<String>();

        statesQueue.addAll(states);
        recursiveCheckAndPut(statesQueue, value);


    }

    private void recursiveCheckAndPut(
            ArrayDeque<String> states, double value) {
        String currentOldest = states.getFirst();
        if(!states.isEmpty()){
            if(!myMap.containsKey(currentOldest)){
                myMap.put(currentOldest, new RecursiveHashMap(order-1));
            }
            RecursiveHashMap lowerLevelMap = myMap.get(currentOldest);
            lowerLevelMap.recursiveCheckAndPut(states, value);
            this.myMap.put(currentOldest, lowerLevelMap);
        }else{
            assert this.order == 0;
            assert this.myMap == null;
            this.key = currentOldest;
            this.value = value;
        }
    }

    public Double get(List<String> stateSequence){
        assert stateSequence.size() == order+1;
        if(stateSequence.isEmpty()){
            return null;
        }
        if(stateSequence.size()==1){
            if(key == stateSequence.get(0)){
                return value;
            }else {
                return null;
            }
        }else {
            String oldest = stateSequence.remove(0);
            if(myMap.containsKey(oldest)){
                return myMap.get(oldest).get(stateSequence);
            }else{
                return null;
            }

        }
    }

    public List<List<String>> getSequences() {
        return sequences;
    }
}
