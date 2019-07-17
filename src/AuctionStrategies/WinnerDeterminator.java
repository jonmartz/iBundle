package AuctionStrategies;

import Components.Agent;
import Components.Bid;
import Components.MDD;
import javafx.beans.property.BooleanProperty;

import java.util.*;

/**
 * This class will determine the agents that win an iteration (inspired from ICTS)
 */
public class WinnerDeterminator implements IAuctionStrategy {

    private ArrayList<Agent> agents;

    @Override
    public boolean determineWinners(ArrayList<Agent> agents) {
        this.agents = agents;
        agents.sort(new AgentComparator());
        getAllocations();
        for (Agent agent : agents) {
            if (agent.allocation == null) return false;
        }
        return true;
    }

    /**
     * Give allocations to as many agents as possible
     */
    private void getAllocations() {
        int maximalRevenue = 0;
        int[] remainingReductions = new int[agents.size()];
        for (int i = 0; i < agents.size(); i++){
            int size = agents.get(i).bids.size();
            maximalRevenue += size - 1;
            remainingReductions[i] = size - 1;
        }
        // target revenue = maximal revenue - reduction
        for (int reduction = 0; reduction < maximalRevenue+1; reduction++){
            if (checkAllReductionCombinations(new int[reduction], 0, remainingReductions))
                return;
            else if (MDD.timeout) return;
        }
    }

    /**
     * Try all the combinations of reducing one or more points to the price of the bundles
     * @return true if an allocation was found
     */
    private boolean checkAllReductionCombinations(int[] reductionOffsets, int baseIndex, int[] remainingReductions) {
        if (baseIndex == reductionOffsets.length){
            // got a valid reduction combination
            int[] reductions = new int[agents.size()];
            // each cell i indicates how many bids to advance in agent i
            for (Integer reductionOffset : reductionOffsets)
                reductions[reductionOffset]++;
            ArrayList<Bid> bids = new ArrayList<>();
            for (int i = 0; i < agents.size(); i++)
                bids.add(agents.get(i).bids.get(reductions[i]));
            return checkAllEliminationCombinations(bids);
        }
        // check all offsets at base index
        for (int offset = reductionOffsets[baseIndex]; offset < agents.size(); offset++){
            for (int i = baseIndex; i < reductionOffsets.length; i++)
                reductionOffsets[i] = offset;
            int[] remainingReductionsClone = remainingReductions.clone();
            // reduce the price of the bid of agent indicated by the base offset
            remainingReductionsClone[reductionOffsets[baseIndex]]--;
            if (remainingReductionsClone[reductionOffsets[baseIndex]] >= 0){
                // reduction was possible, so advance base recursively
                boolean result = checkAllReductionCombinations(reductionOffsets, baseIndex+1, remainingReductionsClone);
                if (result) return true;
                else if (MDD.timeout) return false;
            }
        }
        return false;
    }

    /**
     * Check all the possible ways of dropping zero or more bids with that have a price of 0
     * @return true if an allocation was found
     */
    private boolean checkAllEliminationCombinations(ArrayList<Bid> bids) {
        ArrayList<MDD> constantMdds = new ArrayList<>();
        ArrayList<MDD> eliminableMdds = new ArrayList<>();
        for (Bid bid : bids)
            if (bid.price == 0) eliminableMdds.add(bid.mdd);
            else constantMdds.add(bid.mdd);
        constantMdds.sort(new MDD.MDDComparator());
        if (MDD.isSetIncompatible(constantMdds, null)) return false;
        return recursiveElimination(constantMdds, eliminableMdds, 0);
    }

    /**
     * Divide the search in two, to be sure no more allocations that include conflicting pair of bids is checked.
     * @return true if an allocation was found
     */
    private boolean recursiveElimination(ArrayList<MDD> constantMdds, ArrayList<MDD> eliminableMdds, int initialCount) {
        if (initialCount < 0) return false;
        for (int eliminationCount = initialCount; eliminationCount < eliminableMdds.size() + 1; eliminationCount++){
            boolean[] eliminations = new boolean[eliminableMdds.size()];
            for (int i = 0; i < eliminationCount; i++) eliminations[i] = true;
            boolean ended = false;
            while(!ended){
                ArrayList<MDD> mdds = new ArrayList<>(constantMdds);
                for (int i = 0; i < eliminations.length; i++)
                    if (!eliminations[i]) mdds.add(eliminableMdds.get(i));
                MDD[] incompatibles = new MDD[2];
                if (!MDD.isSetIncompatible(mdds, incompatibles)) {
                    // run low level search
                    if (MDD.getAllocations(mdds))
                        return true;
                    else if (MDD.timeout) return false;
                }
                else {
                    // found two incompatibles: check only combinations that eliminate one of them
                    ArrayList<MDD> newEliminables = new ArrayList<>();
                    for (MDD mdd : eliminableMdds) if (mdd != incompatibles[0]) newEliminables.add(mdd);
                    if (recursiveElimination(constantMdds, newEliminables, eliminationCount-1))
                        return true;
                    newEliminables = new ArrayList<>();
                    for (MDD mdd : eliminableMdds) if (mdd != incompatibles[1]) newEliminables.add(mdd);
                    return recursiveElimination(constantMdds, newEliminables, eliminationCount-1);
                }
                ended = advanceCombination(eliminations);
            }
        }
        return false;
    }

    /**
     * Update the array to indicate the next elimination combination
     * @param eliminations array to update
     * @return true if the last possible combination has been reached
     */
    private boolean advanceCombination(boolean[] eliminations) {

        boolean gotFalse = false;
        boolean ended = true;
        int truths = 0;
        for (int i = eliminations.length-1; i >= 0; i--){
            if (!gotFalse && !eliminations[i]){
                gotFalse = true;
                continue;
            }
            if (eliminations[i]) {
                if (!gotFalse){
                    truths++;
                    eliminations[i] = false;
                }
                else {
                    ended = false;
                    eliminations[i] = false;
                    for (int j = 0; j < truths+1; j++)
                        eliminations[i+j+1] = true;
                    break;
                }
            }
        }
        return ended;
    }

    /**
     * Sorts from the agent with the highest highest bid to the one with the lowest highest bid
     */
    private class AgentComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent o1, Agent o2) {
            return o2.bids.size() - o1.bids.size();
        }
    }
}