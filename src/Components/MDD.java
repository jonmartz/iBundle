package Components;

import java.util.*;

public class MDD {
    private static int neighsAdded; // test
    private static int neighsVisited; // test
    private static int littleAdded = 0;
    private static boolean print = false;
    public int cost;
    public HashSet<Node> nodes = new HashSet<>();
    public ArrayList<MDDNode>[] mddNodes; // array of array lists. mddNodes[i] = all nodes in time i
    public Node start;
    public Node goal;
    private int[] offsets; // to indicate the current path (doesn't include t=cost)
    private boolean firstPathEver = true;
    /**
     * true if the path returned by getNextPath() is the first possible path.
     * resets after all possible paths have been returned and the first path is again returned.
     */
    public boolean gotFirstPath = false;
    public Agent agent;

    public MDD(int cost, Node start, Node goal, Agent agent) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost];
        this.agent = agent;
    }

    public boolean isNodeThere(int id)
    {
        for(int i=0;i<this.mddNodes.length;i++)
        {
            for(int j=0;j<this.mddNodes[i].size();j++)
            {
                if(this.mddNodes[i].get(j).node.id == id)
                    return true;
            }
        }
        return false;
    }

    /**
     * Adds an MDDNode to the correct time t.
     * @param mddNode to add
     * @return If there's already a node with that id in time t, returns that MDDNode.
     *         Else, returns the argument MDDNode.
     */
    public MDDNode add(MDDNode mddNode){
        nodes.add(mddNode.node);
        ArrayList<MDDNode> mddNodesAtTimeT = mddNodes[mddNode.time];
        if (mddNodesAtTimeT == null){ // if first node to add to time t
            mddNodesAtTimeT = new ArrayList<>();
            mddNodes[mddNode.time] = mddNodesAtTimeT;
            mddNode.offset = 0;
            mddNodesAtTimeT.add(mddNode);
        }
        else{ // look if node with id is already in time t
            boolean found = false;
            for (MDDNode current : mddNodesAtTimeT){
                if (current.node.id == mddNode.node.id){
                    mddNode = current;
                    found = true;
                    break;
                }
            }
            if (!found){
                mddNode.offset = mddNodesAtTimeT.size();
                mddNodesAtTimeT.add(mddNode);
            }
        }
        return mddNode;
    }

    // random
    public int[] getNextPath(){
        // get random path
        Random rand = new Random();
        boolean first = true;
        int middle = rand.nextInt(offsets.length-1)+1; // don't include first and last layers
        // move forward from middle
        for (int t = middle; t < offsets.length; t++){
            offsets[t] = rand.nextInt(mddNodes[t].size());
            if (first){ // don't modify middle
                first = false;
                continue;
            }
            // make path legal
            MDDNode curr = mddNodes[t].get(offsets[t]);
            MDDNode prev = mddNodes[t-1].get(offsets[t-1]);
            while (!curr.neighbors.contains(prev)){
                offsets[t] += 1;
                if (mddNodes[t].size() == offsets[t])  offsets[t] = 0;
                curr = mddNodes[t].get(offsets[t]);
            }
        }
        // move backwards from middle
        for (int t = middle-1; t > 0; t--){
            offsets[t] = rand.nextInt(mddNodes[t].size());
            // make path legal
            MDDNode curr = mddNodes[t].get(offsets[t]);
            MDDNode next = mddNodes[t+1].get(offsets[t+1]);
            while (!curr.neighbors.contains(next)){
                offsets[t] += 1;
                if (mddNodes[t].size() == offsets[t])  offsets[t] = 0;
                curr = mddNodes[t].get(offsets[t]);
            }
        }
        // return path
        int[] nextPath = new int[cost+1];
        for (int i = 0; i < cost; i++)
            nextPath[i] = mddNodes[i].get(offsets[i]).node.id;
        nextPath[cost] = goal.id;

//        // optional print
//        ArrayList<String> stringPath = new ArrayList<>();
//        for (Integer i : nextPath) stringPath.add(i.toString());
//        print(String.join(" ",stringPath));

        return nextPath;
    }

//    // ordered
//    public int[] getNextPath(){
//        gotFirstPath = false;
//        if (firstPathEver){
//            // to handle getting a path for the first time
//            firstPathEver = false;
//            gotFirstPath = true;
//            checkLegalPath(1);
//        }
//        else findNextPath(offsets.length-1);
//        int[] nextPath = new int[cost+1];
//        for (int i = 0; i < cost; i++)
//            nextPath[i] = mddNodes[i].get(offsets[i]).node.id;
//        nextPath[cost] = goal.id;
//
//        ArrayList<String> stringPath = new ArrayList<>();
//        for (Integer i : nextPath) stringPath.add(i.toString());
//        print(String.join(" ",stringPath));
//        if (gotFirstPath) print("gotFirstPath");
//
//        return nextPath;
//    }

    private void checkLegalPath(int t) {
        if (t == offsets.length) return; // on goal node
        MDDNode curr = mddNodes[t].get(offsets[t]);
        MDDNode prev = mddNodes[t-1].get(offsets[t-1]);
        if (!curr.neighbors.contains(prev)){
            // move to next node in time t
            if (mddNodes[t].size() == offsets[t]+1){
                // can happen only one time step after the modified
                // time step x in findNextPath: no more legal nodes,
                // so go back and advance in x
                findNextPath(t);
                return;
            }
            offsets[t]+=1;
            checkLegalPath(t);
        }
        else checkLegalPath(t+1);
    }

    private void findNextPath(int t){
        resetOffsets(t+1);
        if (t == 0){
            // found all paths! reset everything
            gotFirstPath = true;
            checkLegalPath(1);
            return;
        }
        if (mddNodes[t].size() == offsets[t]+1){
            // found all paths from t, so change offset at t-1
            findNextPath(t-1);
        }
        else{
            // move to next node in time t
            offsets[t]+=1;
            checkLegalPath(t);
        }
    }

    private void resetOffsets(int t) {
        for (int i = t; i < offsets.length; i++){
            offsets[i] = 0;
        }
    }

    /**
     * Merge all the MDDs in agentMDDMap to look for collisions.
     * @return If there are paths with no collisions for all agents: mapping from agents to allocations.
     *         else: null.
     */
    public static boolean getAllocations(ArrayList<MDD> mdds) {
        print("getting alloc, mdds = "+mdds.size());
        if (mdds.size() == 1){
            mdds.get(0).agent.allocation = mdds.get(0).getNextPath();
            return true;
        }

        // If not so lucky:
        PriorityQueue<MergedState> openStack = new PriorityQueue<>(new stateComparator());
        HashSet<String> visited = new HashSet<>();

        int maxCost = 0;
        for (MDD mdd : mdds) {
            if (maxCost < mdd.cost) maxCost = mdd.cost;
        }
        MergedState startState = new MergedState(0, new int[mdds.size()]);
        MergedState goalState = new MergedState(maxCost, new int[mdds.size()]);

        print("max cost = " + maxCost);

        openStack.add(startState);
        while (!openStack.isEmpty()){
            MergedState currentState = null;
//            if (littleAdded > 100){
//                littleAdded = 0;
//                System.out.println("too little added");
//                return false;
//            }
//            else currentState = openStack.remove();
            currentState = openStack.remove();
            print("time = " + currentState.time);
            if (currentState.equals(goalState)) {
                assignAllocations(mdds, currentState);
                return true;
            }
            if (visited.contains(currentState.id)){
                print("             visited");
                continue;
            }
            visited.add(currentState.id);
            if (hasCollision(mdds, currentState)) continue;

            // get all actual MDDNodes in current state
            ArrayList<MDDNode> mddNodes = new ArrayList<>();
            for (int i = 0; i < mdds.size(); i++) mddNodes.add(mdds.get(i).getTime(currentState.time).get(currentState.offsets[i]));
            neighsAdded = 0;
            neighsVisited = 0;
            addNeighbors(currentState, openStack, visited, mddNodes, new ArrayList<>());
            print("                    n.added="+neighsAdded);
            print("                    n.visited="+neighsVisited);
            if (neighsAdded < neighsVisited*0.1) littleAdded += 1;
            else littleAdded = 0;
        }
        return false;
    }

    private static void addNeighbors(MergedState currentState, PriorityQueue<MergedState> openStack,
                                     HashSet<String> visited, ArrayList<MDDNode> mddNodes, ArrayList<Integer> offsetList) {
        if (offsetList.size() == mddNodes.size()){ // generate neighbor
            int[] offsets = new int[offsetList.size()];
            for (int i = 0; i < offsets.length; i++){
                offsets[i] = offsetList.get(i);
            }
            MergedState neighbor = new MergedState(currentState.time+1, offsets);
            if (!visited.contains(neighbor.id)){
                neighbor.prev = currentState;
                openStack.add(neighbor);
                neighsAdded++;
            }
            else{
                neighsVisited++;
            }
            return;
        }
        MDDNode currentMDDNode = mddNodes.get(offsetList.size());
        if (currentMDDNode.nextNeighbors.isEmpty()){
            ArrayList<Integer> offsetListClone = new ArrayList<>(offsetList);
            offsetListClone.add(currentMDDNode.offset);
            addNeighbors(currentState, openStack, visited, mddNodes, offsetListClone);
        }
        else
            for (MDDNode next : currentMDDNode.nextNeighbors) {
                ArrayList<Integer> offsetListClone = new ArrayList<>(offsetList);
                offsetListClone.add(next.offset);
                addNeighbors(currentState, openStack, visited, mddNodes, offsetListClone);
            }
    }


    private static boolean hasCollision(ArrayList<MDD> mdds, MergedState currentState) {
        HashSet<Integer> nodeIDs = new HashSet<>();
        // check for same position collision
        for (int i = 0; i < mdds.size(); i++){
            int nodeID = mdds.get(i).getTime(currentState.time).get(currentState.offsets[i]).node.id;
            if (nodeIDs.contains(nodeID)){
                print("collision");
                return true;
            }
            nodeIDs.add(nodeID);
        }
        // check for swap position collision
        if (currentState.prev != null){
            MergedState previousState = currentState.prev;
            HashSet<String> nodeIDpairs = new HashSet<>();
            for (int i = 0; i < mdds.size(); i++) {
                int idPrev = mdds.get(i).getTime(previousState.time).get(previousState.offsets[i]).node.id;
                int idCurr = mdds.get(i).getTime(currentState.time).get(currentState.offsets[i]).node.id;
                if (nodeIDpairs.contains(String.valueOf(idCurr)+" "+String.valueOf(idPrev))){
                    print("collision");
                    return true;
                }
                nodeIDpairs.add(String.valueOf(idPrev)+" "+String.valueOf(idCurr));
            }
        }
        return false;
    }

    /**
     * Assign path to each allocation;
     */
    private static void assignAllocations(ArrayList<MDD> mdds, MergedState currentState) {
        // Build allocations including extensions
        ArrayList<LinkedList<Integer>> allocations = new ArrayList<>();
        boolean first = true;
        while (currentState != null){
            for (int i = 0; i < mdds.size(); i++){
                if (first) allocations.add(new LinkedList<>());
                int nodeID = mdds.get(i).getTime(currentState.time).get(currentState.offsets[i]).node.id;
                allocations.get(i).addFirst(nodeID);
            }
            currentState = currentState.prev;
            first = false;
        }
        // Remove extensions
        for (int i = 0; i < allocations.size(); i++){
            LinkedList<Integer> allocationWithExt = allocations.get(i);
            int extension = 0;
            for (int j = allocationWithExt.size()-2; j >= 0; j--){
                if (allocationWithExt.get(j).equals(allocationWithExt.get(j + 1)))
                    extension++;
                else break;
            }
            int[] allocation = new int[allocationWithExt.size()-extension];
            for (int j = 0; j < allocation.length; j++){
                allocation[j] = allocationWithExt.get(j);
            }
            mdds.get(i).agent.allocation = allocation;
        }
    }

    private ArrayList<MDDNode> getTime(int time) {
        if (time <= cost) return mddNodes[time];
        return mddNodes[cost];
    }

    private static class stateComparator implements Comparator<MergedState>{

        @Override
        public int compare(MergedState o1, MergedState o2) {
            return o2.time - o1.time;
        }
    }
    
    private static void print(String s){
        if (print) System.out.println(s);
    }
}
