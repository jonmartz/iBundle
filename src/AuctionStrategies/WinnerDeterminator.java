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

    int test = 0;
    List<Agent> agents;//agents as a list
    List<Agent> allAgents;//all the given agents as a list
    //List<int []> pathsToConsider;//The allocated path
    HashMap<Agent, Set<Bid>> bids;//The given agents that participate in the bid with all of his bids

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



        //   this.pathsToConsider = new ArrayList<>();

        test++;
    /*    while(iterator.hasNext())
        {
            Agent a = iterator.next();
            if(a.allocation!=null)
            {
                pathsToConsider.add(a.allocation);
                iterator.remove();
            }
        }*/
        //Decide on the winners in that iteration
        getWinners();


        for (Agent agent : this.allAgents) {
            if (agent.allocation == null) {

                //System.out.println(agent.id +" id");
                return false;

            }

        }
        return true;
    }


    /**
     * This function will decide who the winners are
     */
    public void getWinners() {
        int numOfAgents = bids.size();

        String ans = "";
        List<Set<String>> allPosibillitiesAllSize = new ArrayList<>();
        Set<String> allPossibilities = new HashSet<>();
        for (int i = numOfAgents; i >= 1; i--) {
            checkAgentsInFixedSize(i, 0, "",allPossibilities);

            allPosibillitiesAllSize.add(allPossibilities);
            allPossibilities = new HashSet<>();
            //if (!ans.equals("")) {

            //return;
            //}

        }
        checkByOrder(allPosibillitiesAllSize);
    }


    private String checkAgentsInFixedSize(int size, int index, String str,Set<String> allPossibilities) {
        if (index == allAgents.size()||size ==0)
            return "";
        if (size == 1) {
            allPossibilities.add(str+index);
            // if (assembleAndCheck(str + index))

            checkAgentsInFixedSize(size, index + 1, str,allPossibilities);
        }
        String ans = checkAgentsInFixedSize(size - 1, index + 1, str + index + ",",allPossibilities);
        if (!ans.equals(""))
            return ans;

        return checkAgentsInFixedSize(size, index + 1, str,allPossibilities);

    }
    private void checkByOrder(List<Set<String>> allPossibilities)
    {

        List<List<String>> allPossibilities2 = new ArrayList<>();
        for(int i=0;i<allPossibilities.size();i++) {
            List<String> temp = new ArrayList<>(allPossibilities.get(i));
            temp.sort(new CompareString(this.allAgents));
            allPossibilities2.add(temp);

        }

        // System.out.println(allPossibilities2.size());

        for(int i=0;i<allPossibilities2.size();i++) {
            //  System.out.println(allPossibilities2.get(i).size());
            for(int j=0;j<allPossibilities2.get(i).size();j++) {


                boolean flag = assembleAndCheck(allPossibilities2.get(i).get(j));
                // System.out.println("check for "+allPossibilities2.get(i).get(j) +" res "+flag);
                if(flag)
                    return;
            }
        }

    }
    /**
     * This function will get a list of agents and a string with their the bids's indices separated by ','
     * This function will check if the agents in the given indices can win the bid together
     *
     * @param whichAgents - The indices of the agents
     * @return - True IFF all the agents can win in the bid
     */
    private boolean assembleAndCheck(String whichAgents) {

        return checkIfAllCanWin(getSetOfAgentsWithGivenString(whichAgents));
    }

    /**
     * This function will return the set of agents in the given indices (in string)
     *
     * @param whichAgents - The indices of the agent that we want to return
     * @return - A set of agents
     */
    private Set<Agent> getSetOfAgentsWithGivenString(String whichAgents) {

        String[] indexes = whichAgents.split(",");
        int[] indices = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            indices[i] = Integer.parseInt(indexes[i]);
        }

        Set<Agent> setToCheck = new HashSet<>();
        for (int i = 0; i < indices.length; i++) {
            setToCheck.add(allAgents.get(indices[i]));
        }
        return setToCheck;
    }

    /**
     * This function will receive a set of agents and
     * will return True IFF all the agents in the set can win the bid =
     */
    private boolean checkIfAllCanWin(Set<Agent> agentsToCheck) {

        List<Agent> agentsAsList = new ArrayList<>(agentsToCheck);
        agents = agentsAsList;

        return recursiveAssignmentCheck(0, new ArrayList<>());
    }

    private boolean recursiveAssignmentCheck(int index, List<int[]> paths) {

        //If we scanned all the agents
        if (index == agents.size()) {

            return checkAllPaths(paths, true);
        }

        //Get the agent and its history
        Agent currentAgent = this.agents.get(index);
        Set<Bid> bidHistory = this.bids.get(currentAgent);
        List<Bid> bidHistoryList = new ArrayList<>(bidHistory);
        bidHistoryList.sort(new CompareBid());

        int[] path;
        boolean flag2 = false;
        int [] firstPath;
        boolean flag =false;
        boolean compatible = false;
        for(Bid bid: bidHistoryList)
        {
            // MDD mdd = currentAgent.currentBid.mdd;
            MDD mdd = bid.mdd;
            flag2 = false;
            mdd.gotFirstPath = false;
            firstPath = mdd.getNextPath();
            path = firstPath;

            while (!areEqual(firstPath,path) || !flag2){
                flag2= true;
                // printPath(path);

                paths.add(index, path);
                if (checkAllPaths(paths, false)) {

                    compatible = true;
                    flag = flag || recursiveAssignmentCheck(index + 1, paths);
                    if (flag) {
                        return true;
                    }
                }

                paths.remove(index);
                path = mdd.getNextPath();
            }

        }

        return compatible && flag;
    }
    private boolean areEqual(int [] a,int []b)
    {
        if(a.length!=b.length)
            return false;
        for(int i=0;i<a.length;i++)
        {
            if(a[i]!=b[i])
            {
                return false;
            }
        }
        return true;
    }
    private void printPath(int [] path)
    {
        System.out.print("path: ");
        for(int i=0;i<path.length-1;i++)
        {
            System.out.print(path[i]+", ");
        }
        System.out.println(path[path.length-1]);
    }
    private boolean checkAllPaths(List<int[]> paths, boolean toAllocate)
    {

        HashMap <Integer, int [] > previousIteration = new HashMap<>();//The previous iteration
        HashMap <Integer, int [] > currentIteration = new HashMap<>();//The current iteration
        HashMap <Integer, Pair<Integer,int []>> fixed = new HashMap<>();//The final nodes. key - node, value - time & full path pair
        int maxPath = -1;//The size pf the longest path
        int length;
        //Initial setup
        for(int i=0;i<paths.size();i++)
        {
            int [] path = paths.get(i);
            //Update the first iteration with the start position
            previousIteration.put(path[0],path);
            length = path.length;
            //Finding the longest path's length
            if(maxPath<length)
            {
                maxPath = length;
            }

            //Initializing the fixed map
            fixed.put(path[length-1],new Pair<>(length -1 , path));
        }

        //If There are 2 agents with the same start/end node (shouldn't happen)
        if(previousIteration.size()<paths.size() || fixed.size()<paths.size())
        {
            return false;
        }

        length = paths.size();//Amount of paths

        int numOfSurvivers = 0;//The number of paths that we didn't finish going through
        //Going through all of the nodes in the path
        for(int iter=1; iter<maxPath;iter++)
        {
            numOfSurvivers = 0;

            //For every path
            for(int j=0;j<length;j++)
            {

                int [] current = paths.get(j);

                //If we didn't go over all this path
                if(current.length>iter)
                {

                    numOfSurvivers++;
                    //Need to check 3 things:
                    //1. don't collide
                    //2. don't cross
                    //3. watch out for agents that ended their path


                    //2. don't cross
                    if(previousIteration.containsKey(current[iter]))
                    {
                        //The suspicious path
                        int [] suspect = previousIteration.get(current[iter]);

                        if(suspect!=current)
                        {
                            //This means that last iteration was to the final node
                            //The agent won't disappear so they will collide
                            if(suspect.length<=iter)
                            {
                                // System.out.println("don't disappear1");
                                return false;

                            }
                            //Cross
                            if(current[iter-1] == suspect[iter]) {
                                //   System.out.println("cross");
                                return false;
                            }
                        }
                    }

                    //3. watch out for agents that ended their path
                    if(fixed.containsKey(current[iter]))
                    {
                        Pair<Integer,int [] > timePathPair = fixed.get(current[iter]);
                        //Collision with an agent that already reached it's destination

                        if(timePathPair.getValue() != current && iter>=timePathPair.getKey()) {
                            //  System.out.println("don't disappear2");
                            //  System.out.println("iter "+iter+" time "+timePathPair.getKey()+" node num "+current[iter]);
                            return false;
                        }
                    }
                    currentIteration.put(current[iter],current);

                }
            }

            //1. don't collide
            if(numOfSurvivers>currentIteration.size())
            {
                //System.out.println("collide");
                return false;
            }
            previousIteration = currentIteration;
            currentIteration = new HashMap<>();
        }

        //Allocate the paths
        if(toAllocate)
        {
            //printHashMap2(fixed);
            length = paths.size();
            for(int i=0;i<length;i++)
            {
                this.agents.get(i).allocation = paths.get(i);
                //printAgentPath(this.agents.get(i));
            }

        }
        return true;
    }
    private void printHashMap(HashMap<Integer, int [] > s)
    {
        String array = "";
        for(int key : s.keySet())
        {


            int [] a = s.get(key);
            for(int i=0;i<a.length;i++)
            {
                array += a[i]+",";
            }
            array = array.substring(0,array.length()-1);
            System.out.println("key "+key+" value "+array);
        }
    }
    private void printHashMap2(HashMap <Integer, Pair<Integer,int []>> s)
    {
        String array = "";
        for(int key : s.keySet())
        {


            Pair<Integer,int []>p = s.get(key);
            int [] a = p.getValue();
            int time = p.getKey();
            for(int i=0;i<a.length;i++)
            {
                array += a[i]+",";
            }
            array = array.substring(0,array.length()-1);
            System.out.println("key "+key+" time "+time+" value "+array);
        }
    }
    /*
    private boolean checkAllPaths(List<int []> paths,boolean toAllocate)
    {


        if(paths==null)
        {
            return true;
        }
        List<int []> new_list_paths = new ArrayList<>(paths);
       // List<int []> new_paths_to_consider = new ArrayList<>(this.pathsToConsider);

        int size = paths.size();
        Map<Integer,int []> check = new HashMap<>();
        Map<Integer,int []> checkPrev = new HashMap<>();
        Map<Integer,int []> fixed = new HashMap<>();
        int [] temp;
      //  for(int i=0;i<pathsToConsider.size();i++)
        {
      //      temp = pathsToConsider.get(i);
      //      fixed.put(temp[temp.length-1],temp);
        }
        int iter = 0;
            //As long as agents remain
        while(size>0)
        {

            //System.out.println("akksdha,d "+size+" "+test);

            Iterator<int []> iterator = new_list_paths.iterator();
            //Iterator<int []> iteratorConsider = new_paths_to_consider.iterator();
            while(iterator.hasNext()) {
                int [] curr = iterator.next();

                //Done with this agent
                if(iter >= curr.length)
                {
                    // System.out.println("loop");
                    // System.out.println("a;lsd;aksd;s;aisdaoasid;");
                    iterator.remove();

                }
                else
                {


                    if(iter>0)
                    {
                        if(checkPrev.containsKey(curr[iter]))
                        {
                            int [] suspect = checkPrev.get(curr[iter]);
                            if(suspect != curr)
                            {
                                if(suspect.length<=iter)//Dose not disappear
                                {
                                    // printNul2(pathsToConsider,paths);
                                    return false;
                                }
                                if(suspect[iter-1] == curr[iter] && suspect[iter] == curr[iter-1])//Cross
                                {
                                    //printNul2(pathsToConsider,paths);
                                    return false;
                                }
                            }

                        }
                        if(fixed.containsKey(curr[iter]))
                        {
                            int [] suspect = fixed.get(curr[iter]);
                            if(suspect.length-1<=iter)
                            {
                                return false;
                            }
                        }
                    }
                    if(iter==curr.length-1)
                    {
                        if(!fixed.containsKey(curr[curr.length-1]))
                            fixed.put(curr[curr.length-1],curr);
                        else {

                            int [] suspect = fixed.get(curr[curr.length-1]);
                            if(suspect.length<curr.length)
                                return false;
                            fixed.put(curr[curr.length-1],curr);
                        }
                    }

                    check.put(curr[iter],curr);
                }
            }
            while(iteratorConsider.hasNext()) {
                int [] curr = iteratorConsider.next();

                if(iter == curr.length)
                {

                    iteratorConsider.remove();
                }
                else
                {
                    check.put(curr[iter],curr);
                }
            }

            if(check.size()<new_list_paths.size()+new_paths_to_consider.size())
            {
                //Collision
                //printNul2(pathsToConsider,paths);
                return false;
            }
            //check the fixes
            checkPrev = check;
            check=new HashMap<>();
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

    }*/

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
    public void printNul(List<int []> paths)
    {

        for(int [] pathse:paths)
        {
            printAgentPath(pathse);
        }

    }
    public void printNul2(List<int []> paths,List<int []> paths2)
    {
        System.out.println("++++++++++++++");
        for(int [] pathse:paths)
        {
            printAgentPath(pathse);
        }
        for(int [] pathse:paths2)
        {
            printAgentPath(pathse);
        }
        System.out.println("++++++++++++++");

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
            return o1.mdd.mddNodes.length - o2.mdd.mddNodes.length;
        }
    }
    public class CompareString implements Comparator<String>
    {
        List<Agent> agents;
        public CompareString(List<Agent> given_agents)
        {
            this.agents = given_agents;
        }
        @Override
        public int compare(String o1,String o2) {
            int numOfBids1 = sumNumOfBids(o1);
            int numOfBids2 = sumNumOfBids(o2);
            return -(numOfBids1 - numOfBids2);
        }
        private int sumNumOfBids(String str)
        {
            String [] split = str.split((","));
            int sum = 0;
            int index;
            for(int i=0;i<split.length;i++)
            {
                index = Integer.parseInt(split[i]);
                sum+= this.agents.get(index).bids.size();
            }
            // return sum;

            double avg = sum;
            avg =avg/split.length;
            double sunOfDistance = 0;
            for(int i=0;i<split.length;i++)
            {
                index = Integer.parseInt(split[i]);
                sunOfDistance+= Math.abs(this.agents.get(index).bids.size()-avg);
            }
            return (int)sunOfDistance;
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
