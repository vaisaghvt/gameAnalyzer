package markovmodeldata;

import gui.NetworkModel;
import modelcomponents.ModelObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 10/4/13
 * Time: 3:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class MarkovDataStore {
    HashMap<Integer, RecursiveHashMap> orderToMarkovDataMapping =
            new HashMap<Integer, RecursiveHashMap>();


    public boolean containsOrderData(int order) {
        return orderToMarkovDataMapping.containsKey(order);
    }

    public RecursiveHashMap getMarkovDataForOrder(int order) {
        if (containsOrderData(order))
            return orderToMarkovDataMapping.get(order);
        else {
            //TODO : generate data otherwise
            return null;
        }
    }

    public void put(int order, RecursiveHashMap markovData) {
        orderToMarkovDataMapping.put(order, markovData);
    }

    public RecursiveHashMap constructNFromM(int n, int m) {
        if (n <= m) {
            return orderToMarkovDataMapping.get(n);
        } else if (n == m + 1) {
            RecursiveHashMap mThOrderMap = orderToMarkovDataMapping.get(m);
            RecursiveHashMap result = new RecursiveHashMap(n);
            for (List<String> sequence : mThOrderMap.getSequences()) {
                String destination = sequence.get(sequence.size() - 1);
                ModelObject destinationNode = NetworkModel.instance().findRoomByName(destination);
                Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(destinationNode);
                double prior = mThOrderMap.get(sequence);

                for (ModelObject neighbour: neighbours) {
                    List<String> newSequence = new ArrayList<String>();
                    newSequence.addAll(sequence);
                    newSequence.remove(0);
                    newSequence.add(neighbour.toString());
                    double newValue = prior *
                            mThOrderMap.get(newSequence);

                    List<String> newSequenceForN = new ArrayList<String>();
                    newSequenceForN.addAll(sequence);
                    newSequenceForN.add(neighbour.toString());

                    result.put(newSequenceForN, newValue);


                }
            }
            return result;

        } else {
            RecursiveHashMap mThOrderMap = orderToMarkovDataMapping.get(m);
            RecursiveHashMap nMinusOneTable = constructNFromM(n - 1, m);
            RecursiveHashMap result = new RecursiveHashMap(n);
            for (List<String> sequence : nMinusOneTable.getSequences()) {
                String destination = sequence.get(sequence.size() - 1);
                ModelObject destinationNode = NetworkModel.instance().findRoomByName(destination);
                Collection<ModelObject> neighbours = NetworkModel.instance().getCompleteGraph().getNeighbors(destinationNode);
                double prior = nMinusOneTable.get(sequence);
                List<String> trimmedSequence = trimSequence(m, sequence);

                for (ModelObject neighbour:neighbours) {
                    List<String> newSequence = new ArrayList<String>();
                    newSequence.addAll(trimmedSequence);
                    newSequence.remove(0);
                    newSequence.add(neighbour.toString());
                    double newValue = prior *
                            mThOrderMap.get(newSequence);

                    List<String> newSequenceForN = new ArrayList<String>();
                    newSequenceForN.addAll(sequence);
                    newSequenceForN.add(neighbour.toString());

                    result.put(newSequenceForN, newValue);


                }
            }
            return result;

        }
    }

    private List<String> trimSequence(int m, List<String> sequence) {
        List<String> result =new ArrayList<String>();
        for(int i= sequence.size()-1;result.size()<m;i--){
            result.add(0,sequence.get(i));

        }
        assert result.size()==m;
        assert result.get(result.size()-1) == sequence.get(sequence.size()-1);
        return result;

        //To change body of created methods use File | Settings | File Templates.
    }


}
