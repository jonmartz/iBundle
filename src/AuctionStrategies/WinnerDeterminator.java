package AuctionStrategies;

import Components.Agent;
import Components.Bid;
import Components.MDD;
import javafx.util.Pair;

import java.util.*;

/**
 * This class will determine the agents that win an iteration (inspired form ICTS)
 */
public class WinnerDeterminator implements IAuctionStrategy {

    private ArrayList<Agent> agents;

    @Override
    public boolean determineWinners(HashMap<Agent, Set<Bid>> bids) {
        this.agents = new ArrayList<>(bids.keySet());
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
            System.out.println("revenue " + targetRevenue);
            for (int agentCount = agents.size(); agentCount >= 0; agentCount--)
                if (allocateToAgentSubset(agents, new ArrayList<>(), agentCount, 0, targetRevenue))
                    return;
        }
    }

    private boolean allocateToAgentSubset(ArrayList<Agent> agents, ArrayList<Agent> agentSubset,
                                                 int n, int offset, int targetRevenue){
        if (n == 0){
            // get all mdds from agent subset that give target revenue
            return checkAllMddCombinatons(agentSubset, new ArrayList<>(), targetRevenue, 0);
        }
        for (int i = offset; i < agents.size(); i++){
            ArrayList<Agent> agentSubsetClone = new ArrayList<>(agentSubset);
            agentSubsetClone.add(agents.get(i));
            if (allocateToAgentSubset(agents, agentSubsetClone, n-1, i+1, targetRevenue)) return true;
        }
        return false;
    }

    private boolean checkAllMddCombinatons(ArrayList<Agent> agentSubset, ArrayList<MDD> mdds,
                                           int targetRevenue, int subsetRevenue) {
        if (mdds.size() == agentSubset.size()){
            if (subsetRevenue < targetRevenue) return false;
            return MDD.getAllocations(mdds);
        }
        Agent agent = agentSubset.get(mdds.size());
        for (Bid bid : agent.bids){
            ArrayList<MDD> mddSubsetClone = new ArrayList<>(mdds);
            mddSubsetClone.add(bid.mdd);
            if (checkAllMddCombinatons(agentSubset, mddSubsetClone, targetRevenue, subsetRevenue+bid.price))
                return true;
        }
        return false;
    }
}