package AuctionStrategies;

import Components.Agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thia class will determine the agents that win an iteration (inspired form ICTS)
 */
public class WinnerDeterminator implements IWinnerDeterminator {
    /**
     * The constructor of the class
     */
    public WinnerDeterminator() {

    }

    /**
     * This function will return the winning agents
     *
     * @param agents - The given agents that participate in the bid
     * @return - The agents that will win the auction
     */
    @Override
    public Set<Agent> getWinners(List<Agent> agents) {
        int numOfAgents = agents.size();

        String ans = "";
        for (int i = numOfAgents; i >= 1; i--) {
            ans = checkAgentsInFixedSize(agents,i,0,"");
            if(!ans.equals(""))
                return getSetOfAgentsWithGivenString(agents,ans);

        }
        return null;
    }

    /**
     * This function will check if there is a group of agents in the given size
     * That can win the bid (all of the agents)
     * @param agents - The given agents
     * @param size - Teh given agents's size
     * @param index - The index of the agent in the list
     * @param str - The string that will save the indices of the agents
     * @return - The string of indices of the agents in the given list
     * (If there is no group of agents that in the given size that can all win the bid, the function will return an empty string)
     */
    private String checkAgentsInFixedSize(List<Agent> agents,int size,int index,String str)
    {
        if(index ==  agents.size())
            return "";
        if(size == 1)
        {
            if(assembleAndCheck(agents,str+index))
                return str+index;
            return checkAgentsInFixedSize(agents,size,index+1,str);
        }
        String ans = checkAgentsInFixedSize(agents,size-1,index+1,str+index+",");
        if(!ans.equals(""))
            return ans;

        return checkAgentsInFixedSize(agents,size,index+1,str);

    }

    /**
     * This function will get a list of agents and a string with their the agents's indices separated by ','
     * This function will check if the agents in the given indices can win the bid together
     * @param agents - The given agents
     * @param whichAgents - The indices of the agents
     * @return - True IFF all the agents can win in the bid
     */
    private boolean assembleAndCheck(List<Agent> agents, String whichAgents)
    {
        System.out.println(whichAgents);
        return checkIfAllCanWin(getSetOfAgentsWithGivenString(agents,whichAgents));
    }

    /**
     * This function will return the set of agents in the given indices (in string)
     * @param agents - The given agents
     * @param whichAgents - The indices of the agent that we want to return
     * @return - A set of agents
     */
    private Set<Agent> getSetOfAgentsWithGivenString(List<Agent> agents, String whichAgents)
    {

        String [] indexes = whichAgents.split(",");
        int [] indices = new int[indexes.length];
        for(int i=0;i<indexes.length;i++)
        {
            indices[i] = Integer.parseInt(indexes[i]);
        }

        Set<Agent> setToCheck = new HashSet<>();
        for(int i=0;i<indices.length;i++)
        {
            setToCheck.add(agents.get(indices[i]));
        }
        return setToCheck;
    }
    /**
     * This function will receive a set of agents and
     * will return True IFF all the agents in the set can win the bid =
     */
    private  boolean checkIfAllCanWin(Set<Agent> agents)
    {
        // TODO: 6/13/2019 This function will check if all the agents received are able to win  
        return false;
    }


}
