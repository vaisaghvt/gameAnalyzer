package markovmodeldata;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import gui.Phase;
import modelcomponents.ModelEdge;
import modelcomponents.ModelObject;
import randomwalk.RandomWalkOrganizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 4/11/13
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarkovDataOrganizer {


    HashMap<String, MarkovDataStore> collectionToMarkovDataMapping = new HashMap<String, MarkovDataStore>();
    private static MarkovDataOrganizer instance;

    private MarkovDataOrganizer() {
    }

    public static MarkovDataOrganizer instance(){
        if(instance==null){
            instance = new MarkovDataOrganizer();
        }
        return instance;
    }


    public MarkovDataStore getMarkovDataStore(Collection<String> names, Collection<Phase> phases, Collection<DirectedSparseMultigraph<ModelObject, ModelEdge>> graphCollection){
        String codeString = findCode(names, phases);
        if(!collectionToMarkovDataMapping.containsKey(codeString)){
            MarkovDataStore newStore = new MarkovDataStore(graphCollection);
            collectionToMarkovDataMapping.put(codeString, newStore);
        }
        return collectionToMarkovDataMapping.get(codeString);
    }

    public MarkovDataStore getRandomWalkMarkovData(RandomWalkOrganizer.RANDOM_WALK_TYPE type){

        String codeString = "RandomWalk"+type;
        if(!collectionToMarkovDataMapping.containsKey(codeString)){
            //TODO: check semaphore usage
            MarkovDataStore newStore = new MarkovDataStore(RandomWalkOrganizer.getAllRandomWalkGraphs(new Semaphore(1), type));
            collectionToMarkovDataMapping.put(codeString, newStore);
        }
        return collectionToMarkovDataMapping.get(codeString);
    }


    private String findCode(Collection<String> names, Collection<Phase> phases) {
        TreeSet<String> sortedNames = new TreeSet<String>(names);

        StringBuilder finalCode= new StringBuilder();
        for(String name: sortedNames){
            finalCode.append(name);
        }
        if(phases.contains(Phase.EXPLORATION)){
            finalCode.append("e");
        }
        if(phases.contains(Phase.TASK_1)){
            finalCode.append("1");
        }
        if(phases.contains(Phase.TASK_2)){
            finalCode.append("2");
        }
        if(phases.contains(Phase.TASK_3)){
            finalCode.append("3");
        }
        return  finalCode.toString();

    }


}
