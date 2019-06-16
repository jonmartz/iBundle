package AuctionStrategies;

import Components.Agent;
import Components.Bid;
import Components.MDD;

import java.util.*;

/**
 * This class will determine the agents that win an iteration (inspired form ICTS)
 */
public class WinnerDeterminator implements IAuctionStrategy {


    List<Agent> agents;//agents as a list
    List<Agent> allAgents;//all the given agents as a list
    HashMap<Agent,Set<Bid>> bids;//The given agents that participate in the bid with all of his bids
    /**
     * The constructor of the class
     */
    public WinnerDeterminator() {

    }

    @Override
    public boolean determineWinners(HashMap<Agent, Set<Bid>> bids) {
        //Set the parameter
        this.bids = bids;
        this.allAgents = new ArrayList<>(this.bids.keySet());
        Iterator<Agent> iterator = this.allAgents.iterator();

        while(iterator.hasNext())
        {
            Agent a = iterator.next();
            if(a.allocation!=null)
            {
                iterator.remove();
            }
        }
        //Decide on the winners in that iteration
        getWinners();



        for(Agent agent : this.allAgents)
        {
            if(agent.allocation == null)
                return false;
        }
        return true;
    }


    /**
     * This function will decide who the winners are
     */
    public void getWinners() {
        int numOfAgents =bids.size();

        String ans = "";
        for (int i = numOfAgents; i >= 1; i--) {
            ans = checkAgentsInFixedSize(i,0,"");
            if(!ans.equals(""))
            {
                return;
            }

        }
    }

    /**
     * This function will check if there is a group of agents in the given size
     * That can win the bid (all of the agents)
     * @param size - Teh given agents's size
     * @param index - The index of the agent in the list
     * @param str - The string that will save the indices of the agents
     * @return - The string of indices of the agents in the given list
     * (If there is no group of agents that in the given size that can all win the bid, the function will return an empty string)
     */
    private String checkAgentsInFixedSize(int size,int index,String str)
    {
        if(index ==  allAgents.size())
            return "";
        if(size == 1)
        {
            if(assembleAndCheck(str+index))
                return str+index;
            return checkAgentsInFixedSize(size,index+1,str);
        }
        String ans = checkAgentsInFixedSize(size-1,index+1,str+index+",");
        if(!ans.equals(""))
            return ans;

        return checkAgentsInFixedSize(size,index+1,str);

    }

    /**
     * This function will get a list of agents and a string with their the bids's indices separated by ','
     * This function will check if the agents in the given indices can win the bid together
     * @param whichAgents - The indices of the agents
     * @return - True IFF all the agents can win in the bid
     */
    private boolean assembleAndCheck(String whichAgents)
    {

        return checkIfAllCanWin(getSetOfAgentsWithGivenString(whichAgents));
    }

    /**
     * This function will return the set of agents in the given indices (in string)
     * @param whichAgents - The indices of the agent that we want to return
     * @return - A set of agents
     */
    private Set<Agent> getSetOfAgentsWithGivenString(String whichAgents)
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
            setToCheck.add(allAgents.get(indices[i]));
        }
        return setToCheck;
    }
    /**
     * This function will receive a set of agents and
     * will return True IFF all the agents in the set can win the bid =
     */
    private  boolean checkIfAllCanWin(Set<Agent> agentsToCheck)
    {

        List<Agent> agentsAsList = new ArrayList<>(agentsToCheck);
        agents = agentsAsList;
        return recursiveAssignmentCheck(0,new ArrayList<>());
    }

    private boolean recursiveAssignmentCheck( int index,List<int []> paths)
    {

        //If we scanned all the agents
        if(index == agents.size())
        {
            return checkAllPaths(paths,true);
        }

        //Get the agent and its history
        Agent currentAgent = this.agents.get(index);
        Set<Bid> bidHistory = this.bids.get(currentAgent);
        List<Bid> bidHistoryList = new ArrayList<>(bidHistory);
        bidHistoryList.sort(new CompareBid());

        int [] path;
        boolean flag = false;
        for(Bid bid: bidHistoryList)
        {
            MDD mdd = bid.mdd;
            //System.out.println(mdd.mddNodes.length+" length");
            do {


                path = mdd.getNextPath();
                paths.add(index,path);
                if(checkAllPaths(paths,false))
                {
                    flag = flag || recursiveAssignmentCheck(index+1,paths);
                    if(flag)
                    {
                        return true;
                    }
                }
                paths.remove(index);

            } while (!mdd.gotFirstPath);
        }

        return false;
    }

    private boolean checkAllPaths(List<int []> paths,boolean toAllocate)
    {

        if(paths==null)
        {
            return true;
        }
        List<int []> new_list_paths = new ArrayList<>(paths);

        int size = paths.size();
        Set<Integer> check = new HashSet<>();
        int iter = 0;
        while(size>1)
        {


            Iterator<int []> iterator = new_list_paths.iterator();

            while(iterator.hasNext()) {
                int [] curr = iterator.next();

                if(iter == curr.length)
                {
                   // System.out.println("loop");
                    iterator.remove();
                }
                else
                {
                    check.add(curr[iter]);
                }
            }

            if(check.size()<new_list_paths.size())
            {
                //Collision
               // System.out.println("Collision");
                return false;
            }

            check.clear();
            iter++;
            size = new_list_paths.size();
        }
      //  System.out.println("exit");
        if(toAllocate)
        {
            size = paths.size();
            for(int i=0;i<size;i++)
            {
                this.agents.get(i).allocation = paths.get(i);
                //printAgentPath(this.agents.get(i));
            }

        }
        return true;

    }

    public  void printAgentPath(int [] path)
    {
        String pathInString = "";

        pathInString += path[0];
        for(int i=1;i<path.length;i++)
        {
            pathInString+= ","+path[i];
        }

        System.out.println("path: "+pathInString);
    }

    private boolean test(List<int [] >li)
    {
        if(li.size()!=2)
            return false;
        int [] g = li.get(0);
        int [] f = li.get(1);

        return (g[0] == 5 && g[1] == 3 && g[2] == 6 && g[3] == 8 && f[0] == 1 && f[1] == 0 && f[2] == 2 && f[3] == 4) ||(f[0] == 5 && f[1] == 3 && f[2] == 6 && f[3] == 8 && g[0] == 1 && g[1] == 0 && g[2] == 2 && g[3] == 4);
    }
    public class CompareBid implements Comparator<Bid>
    {
        public CompareBid()
        {

        }
        @Override
        public int compare(Bid o1, Bid o2) {
            if(o1.mdd.mddNodes.length == o2.mdd.mddNodes.length)
                return 0;
            return o2.mdd.mddNodes.length - o1.mdd.mddNodes.length;
        }
    }
    public void printAgentPath(Agent agent)
    {
        String pathInString = "";
        int [] path = agent.allocation;

        pathInString += agent.allocation[0];
        for(int i=1;i<path.length;i++)
        {
            pathInString+= ","+agent.allocation[i];
        }

        System.out.println("Agent "+agent.id+" new path: "+pathInString);
    }
}
