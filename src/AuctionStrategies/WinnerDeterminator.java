package AuctionStrategies;

import Components.Agent;
import Components.Bid;
import Components.MDD;

import java.util.*;

/**
 * This class will determine the agents that win an iteration (inspired form ICTS)
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
            else if (MDD.failed) return;
        }

//        for (int targetRevenue = maximalRevenue; targetRevenue >= 0; targetRevenue--) {
//            System.out.println("    trying revenue: " + targetRevenue);
//            for (int agentCount = agents.size(); agentCount >= 0; agentCount--)
//                if (allocateToAgentSubset(agents, new ArrayList<>(), agentCount, 0, targetRevenue))
//                    return;
//                else if (MDD.failed) return;
//        }
    }

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
            }
        }
        return false;
    }

    private boolean checkAllEliminationCombinations(ArrayList<Bid> bids) {
        ArrayList<MDD> constantMdds = new ArrayList<>();
        ArrayList<MDD> eliminableMdds = new ArrayList<>();
        for (Bid bid : bids)
            if (bid.price == 0) eliminableMdds.add(bid.mdd);
            else constantMdds.add(bid.mdd);
        constantMdds.sort(new MDD.MDDComparator());
        if (MDD.isSetIncompatible(constantMdds)) return false;
        for (int eliminationCount = 0; eliminationCount < eliminableMdds.size() + 1; eliminationCount++){
            boolean[] eliminations = new boolean[eliminableMdds.size()];
            for (int i = 0; i < eliminationCount; i++) eliminations[i] = true;
            boolean ended = false;
            while(!ended){
                ArrayList<MDD> mdds = new ArrayList<>(constantMdds);
                for (int i = 0; i < eliminations.length; i++)
                    if (!eliminations[i]) mdds.add(eliminableMdds.get(i));
                if (!MDD.isSetIncompatible(mdds) && MDD.getAllocations(mdds))
                    return true;
                ended = advanceCombination(eliminations);
            }
        }
        return false;
    }

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

//    private boolean allocateToAgentSubset(ArrayList<Agent> agents, ArrayList<Agent> agentSubset,
//                                                 int n, int offset, int targetRevenue){
//        if (n == 0){
//
//            ArrayList<String> strings = new ArrayList<>();
//            for (Agent agent : agentSubset) strings.add(String.valueOf(agent.id));
////            System.out.println("        "+strings.size()+" agents: "+String.join(" ",strings));
//
//            // get all mdds from agent subset that give target revenue
//            return checkAllMddCombinations(agentSubset, new ArrayList<>(), targetRevenue, 0);
//        }
//        for (int i = offset; i < agents.size()-n+1; i++){
//            ArrayList<Agent> agentSubsetClone = new ArrayList<>(agentSubset);
//            agentSubsetClone.add(agents.get(i));
//            if (allocateToAgentSubset(agents, agentSubsetClone, n-1, i+1, targetRevenue)) return true;
//            else if (MDD.failed) return false;
//        }
//        return false;
//    }

//    private boolean checkAllMddCombinations(ArrayList<Agent> agentSubset, ArrayList<MDD> mdds,
//                                            int targetRevenue, int subsetRevenue) {
//        if (mdds.size() == agentSubset.size()){
//
////            ArrayList<String> strings = new ArrayList<>();
////            for (MDD mdd : mdds) strings.add(String.valueOf(mdd.cost));
////            System.out.println("    mdd costs: "+String.join(" ",strings));
////            System.out.println("        target="+String.valueOf(targetRevenue)+"subset="+String.valueOf(subsetRevenue));
//
//            if (subsetRevenue < targetRevenue){
//                System.out.println("        not enough revenue");
//                return false;
//            }
//            return MDD.getAllocations(mdds);
//        }
//        Agent agent = agentSubset.get(mdds.size());
//        for (Bid bid : agent.bids){
//            ArrayList<MDD> mddSubsetClone = new ArrayList<>(mdds);
//            mddSubsetClone.add(bid.mdd);
//            if (MDD.isSetIncompatible(mddSubsetClone)){
////                System.out.println("        INCOMPATIBLE");
//                continue;
//            }
//            if (checkAllMddCombinations(agentSubset, mddSubsetClone, targetRevenue, subsetRevenue+bid.price))
//                return true;
//            else if (MDD.failed) return false;
//        }
//        return false;
//    }

    private class AgentComparator implements Comparator<Agent> {
        /**
         * Agent with highest price bid first
         */
        @Override
        public int compare(Agent o1, Agent o2) {
            return o2.bids.size() - o1.bids.size();
        }
    }
}