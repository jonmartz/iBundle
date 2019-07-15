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
    public boolean determineWinners(HashMap<Agent, Set<Bid>> bids) {
        agents = new ArrayList<>(bids.keySet());
        agents.sort(new AgentComparator());
        getAllocations();
        for (Agent agent : agents) {
            if (agent.allocation == null) return false;
        }
        return true;
    }

    private void getAllocations() {
        int maximalRevenue = 0;
        for (Agent agent : agents) maximalRevenue += agent.bids.size() - 1;
        for (int targetRevenue = maximalRevenue; targetRevenue >= 0; targetRevenue--) {
            System.out.println("    trying revenue: " + targetRevenue);
            for (int agentCount = agents.size(); agentCount >= 0; agentCount--)
                if (allocateToAgentSubset(agents, new ArrayList<>(), agentCount, 0, targetRevenue))
                    return;
        }
    }

    private boolean allocateToAgentSubset(ArrayList<Agent> agents, ArrayList<Agent> agentSubset,
                                                 int n, int offset, int targetRevenue){
        if (n == 0){

            ArrayList<String> strings = new ArrayList<>();
            for (Agent agent : agentSubset) strings.add(String.valueOf(agent.id));
//            System.out.println("        "+strings.size()+" agents: "+String.join(" ",strings));

            // get all mdds from agent subset that give target revenue
            return checkAllMddCombinations(agentSubset, new ArrayList<>(), targetRevenue, 0);
        }
        for (int i = offset; i < agents.size()-n+1; i++){
            ArrayList<Agent> agentSubsetClone = new ArrayList<>(agentSubset);
            agentSubsetClone.add(agents.get(i));
            if (allocateToAgentSubset(agents, agentSubsetClone, n-1, i+1, targetRevenue)) return true;
        }
        return false;
    }

    private boolean checkAllMddCombinations(ArrayList<Agent> agentSubset, ArrayList<MDD> mdds,
                                            int targetRevenue, int subsetRevenue) {
        if (mdds.size() == agentSubset.size()){

//            ArrayList<String> strings = new ArrayList<>();
//            for (MDD mdd : mdds) strings.add(String.valueOf(mdd.cost));
//            System.out.println("    mdd costs: "+String.join(" ",strings));
//            System.out.println("        target="+String.valueOf(targetRevenue)+"subset="+String.valueOf(subsetRevenue));

            if (subsetRevenue < targetRevenue){
//                System.out.println("        not enough revenue");
                return false;
            }
            return MDD.getAllocations(mdds);
        }
        Agent agent = agentSubset.get(mdds.size());
        for (Bid bid : agent.bids){
            ArrayList<MDD> mddSubsetClone = new ArrayList<>(mdds);
            mddSubsetClone.add(bid.mdd);
            if (MDD.isSetIncompatible(mddSubsetClone)){
//                System.out.println("        INCOMPATIBLE");
                continue;
            }
            if (checkAllMddCombinations(agentSubset, mddSubsetClone, targetRevenue, subsetRevenue+bid.price))
                return true;
        }
        return false;
    }

    private class AgentComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent o1, Agent o2) {
            return o1.id - o2.id;
        }
    }
}