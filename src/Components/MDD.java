package Components;

import java.util.*;

/**
 * Represents an MDD. Also, heavy use of static functions was made in this class to do all the low level search
 * of the k-Agent MDD space.
 */
public class MDD {
    // static variables for the super fun low level search
    public static boolean print = false; // to print in low level search
    public static long startTime; // to know when timeout is reached
    private static ArrayList<MDDNode> nodeBackup; // to save here nodes that were pruned and restore them later
    private static boolean unsolvable = false; // true if an unavoidable collision was found
    public static boolean ignoreCollisions = false; // to ignore all collisions
    public static HashMap<MDD, HashSet<MDD>> incompatibleMddsSet; // set of all mdds pairs that are unsolvable
    public static boolean timeout = false; // true if run was ended because og timeout
    public static long timeoutSeconds; // seconds to timeout

    // normal members
    public int cost; // length of the paths in MDD
    public HashSet<Node> nodes = new HashSet<>(); // set of all actual nodes the MDD contains (not MDD nodes)
    public ArrayList<MDDNode>[] mddNodes; // array of array lists. mddNodes[i] = list of all nodes in time i
    public Node start;
    public Node goal;
    public Agent agent; // owner of the bid that contains the MDD

    // for getting a possible path
    private int[] offsets; // to indicate the current path (doesn't include t=cost)
    public boolean gotFirstPath = false;

    /**
     * Constructor
     * @param cost of mdd
     * @param start node
     * @param goal node
     * @param agent that owns the bid with the MDD
     */
    public MDD(int cost, Node start, Node goal, Agent agent) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost];
        this.agent = agent;
    }

    /**
     * Attempt to allocate a path to all MDDs
     * @param mdds to allocate
     * @return true if allocation was found
     */
    public static boolean getAllocations(ArrayList<MDD> mdds) {
        mdds.sort(new MDDComparator());
        nodeBackup = new ArrayList<>();
        boolean result = lowLevelSearch(mdds);
        restoreMdds();
        return result;
    }

    /**
     * Restore the mdds to the state they were before the pruning
     */
    private static void restoreMdds() {
        for (MDDNode node : nodeBackup){
            ArrayList<MDDNode> layer = node.mdd.getTime(node.time);
            layer.add(node);
            for (MDDNode neighbor : node.neighbors){
                if (neighbor.time == node.time-1)
                    neighbor.nextNeighbors.add(node);
            }
        }
    }

    /**
     * Merge all the MDDs in agentMDDMap to look for collisions.
     * @param mdds to allocate
     * @return true if there are paths with no collisions for all agents
     */
    public static boolean lowLevelSearch(ArrayList<MDD> mdds) {
        if (timeout()) return false;

        print("low level search, mdds = "+mdds.size());

        // easy case
        if (mdds.size() == 1){
            mdds.get(0).agent.allocation = mdds.get(0).getNextPath();
            return true;
        }

        // If not so lucky:
        LinkedList<MergedState> openStack = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        MDDNode[] startMddNodes = new MDDNode[mdds.size()];
        int goalTime = 0;
        int i = 0;
        for (MDD mdd : mdds) {
            if (goalTime < mdd.cost) goalTime = mdd.cost;
            startMddNodes[i] = mdd.getTime(0).get(0);
            i++;
        }
        MergedState startState = new MergedState(0, null, startMddNodes);

        print("max cost = " + goalTime);

        openStack.addFirst(startState);
        int iterations = 0;
        while (!openStack.isEmpty()){

            // to stop in case it takes too long to solve
            iterations++;
            if (iterations == 1000){
                iterations = 0;
                if (timeout()) return false;
            }

            // retrieve state from open list
            MergedState currState = openStack.removeFirst();
            print("mdd = " + currState.toString()+"");
            if (currState.time == goalTime) {
                assignAllocations(mdds, currState);
                return true;
            }

            // May have a pointer to a node that was already pruned
            boolean pruned = false;
            for (MDDNode mddNode : currState.mddNodes) {
                if (mddNode.pruned) {
                    print("        ALREADY PRUNED!");
                    pruned = true;
                    break;
                }
            }
            if (pruned) continue;

            // check if already visited
            if (visited.contains(currState.id)){
                print("             visited");
                continue;
            }

            // add neighbor to open queue. If neighbor is not the last possible neighbor of the state,
            // the state puts itself back into the open queue to give away more neighbors later.
            MergedState neighbor = currState.getNextNeighbor();
            if (!currState.gotAllPossibleNeighbors) openStack.addFirst(currState);
            else visited.add(currState.id);

            // add neighbor only if it doesn't produce collisions
            if (!hasCollision(mdds, neighbor)) openStack.addFirst(neighbor);
            if (unsolvable){ // if the collision is unavoidable
                unsolvable = false;
                return false;
            }
        }
        return false;
    }

    /**
     * Check if timeout has been reached
     * @return true if timeout reached
     */
    private static boolean timeout() {
        if (System.currentTimeMillis()-startTime > timeoutSeconds*1000){ // more than two minutes...
            timeout = true;
            return true;
        }
        return false;
    }

    /**
     * Check if there is a collision in the state. Will also try to prune in case of collision.
     * In this function will check only for normal collisions, and then try to find swap collisions.
     * @param mdds of the current run
     * @param currState to check for collisions
     * @return true if collision was found
     */
    private static boolean hasCollision(ArrayList<MDD> mdds, MergedState currState) {
        if (ignoreCollisions) return false;
        HashMap<Integer, Integer> nodeIDs = new HashMap<>();

        // check for same position collision
        for (int i = 0; i < mdds.size(); i++){
            int nodeID = currState.mddNodes[i].node.id;
            if (nodeIDs.containsKey(nodeID)){
                print("        collision: "+currState.time);

                // attempt pruning
                int j = nodeIDs.get(nodeID);
                MDD mdd1 = mdds.get(i);
                MDD mdd2 = mdds.get(j);
                ArrayList<MDDNode> mmdNodes1 = mdd1.getTime(currState.time);
                ArrayList<MDDNode> mmdNodes2 = mdd2.getTime(currState.time);
                MDDNode node1 = currState.mddNodes[i];
                MDDNode node2 = currState.mddNodes[j];

                // prune only if the other mdd has only one choice of being there
                if (mmdNodes1.size() == 1 && mmdNodes2.size() == 1){
                    addIncompatible(mdd1, mdd2);
                    unsolvable = true;
                    return true;
                }
                else if (mmdNodes1.size() == 1) recursivePruning(node2);
                else if (mmdNodes2.size() == 1) recursivePruning(node1);
                return true;
            }
            nodeIDs.put(nodeID, i);
        }
        return findSwapCollision(mdds, currState);
    }

    /**
     * Look for swap collisions, and try to prune if possible.
     * @param mdds of the current run
     * @param currState to check for collisions
     * @return true if collision was found
     */
    private static boolean findSwapCollision(ArrayList<MDD> mdds, MergedState currState) {
        // check for swap position collision
        if (currState.prev != null){
            MergedState prevState = currState.prev;
            HashMap<String, Integer> nodeIDpairs = new HashMap<>();
            for (int i1 = 0; i1 < mdds.size(); i1++) {
                int idPrev = prevState.mddNodes[i1].node.id;
                int idCurr = currState.mddNodes[i1].node.id;
                if (nodeIDpairs.containsKey(String.valueOf(idCurr)+" "+String.valueOf(idPrev))){

                    print("        swap collision: "+currState.time);

                    // attempt pruning
                    int i2 = nodeIDpairs.get(String.valueOf(idCurr)+" "+String.valueOf(idPrev));
                    MDD mdd1 = mdds.get(i1);
                    MDD mdd2 = mdds.get(i2);
                    ArrayList<MDDNode> currNodes1 = mdd1.getTime(currState.time);
                    ArrayList<MDDNode> prevNodes1 = mdd1.getTime(prevState.time);
                    ArrayList<MDDNode> currNodes2 = mdd2.getTime(currState.time);
                    ArrayList<MDDNode> prevNodes2 = mdd2.getTime(prevState.time);
                    MDDNode currNode1 = currState.mddNodes[i1];
                    MDDNode currNode2 = currState.mddNodes[i2];

                    // prune only if the other mdd's only choice is to traverse the collision path
                    MDD mddToPrune = null;
                    MDDNode currNode = null;
                    int prevNodeID = -1; // have to look for prev node in t-1 layer
                    if (currNodes1.size() == 1 && prevNodes1.size() == 1 &&
                            currNodes2.size() == 1 && prevNodes2.size() == 1){
                        addIncompatible(mdd1, mdd2);
                        unsolvable = true;
                        return true;
                    }
                    else if (currNodes1.size() == 1 && prevNodes1.size() == 1){
                        mddToPrune = mdd2;
                        currNode = currNode2;
                        prevNodeID = currNode1.node.id;
                    }
                    else if (currNodes2.size() == 1 && prevNodes2.size() == 1){
                        mddToPrune = mdd1;
                        currNode = currNode1;
                        prevNodeID = currNode2.node.id;
                    }
                    if (mddToPrune != null){ // prune! (maybe)
                        MDDNode prevNode = null;
                        for (MDDNode mddNode : currNode.mdd.getTime(currNode.time-1)){
                            if (mddNode.node.id == prevNodeID){
                                prevNode = mddNode;
                                break;
                            }
                        }
                        // weird case: node was already pruned
                        if (prevNode == null) return true;
                        // case 1: if curr is only next of prev, prune prev
                        if (prevNode.nextNeighbors.size() == 1)
                            recursivePruning(prevNode);
                            // case 2: if prev is only prev of curr, prune curr
                        else if (currNode.neighbors.size() - currNode.nextNeighbors.size() == 1)
                            recursivePruning(currNode);
                    }
                    return true;
                }
                nodeIDpairs.put(String.valueOf(idPrev)+" "+String.valueOf(idCurr), i1);
            }
        }
        return false;
    }

    /**
     * Pruning is possible! Yay! (May prune parent of node, in case node is the only child of it's parent)
     * @param node to prune
     */
    private static void recursivePruning(MDDNode node) {
        if (node.time == 0){
            print("         TRIED PRUNING ROOT");
            unsolvable = true;
            return;
        }
        print("            PRUNE!");
        nodeBackup.add(node);
        node.pruned = true;
        ArrayList<MDDNode> currLayer = node.mdd.getTime(node.time);
        ArrayList<MDDNode> prevLayer = node.mdd.getTime(node.time-1);
        currLayer.remove(node);
        for (MDDNode sibling : currLayer)
            if (sibling.offset > node.offset)
                sibling.offset--;
        for (MDDNode prev : prevLayer)
            prev.nextNeighbors.remove(node);
        ArrayList<MDDNode> prevsBackup = new ArrayList<>(prevLayer);
        for (MDDNode prev : prevsBackup)
            if (prev.nextNeighbors.size() == 0)
                recursivePruning(prev);
    }

    /**
     * Select a path from each MDD and assign it as the mdd's agent's allocation.
     * The path may include extension (if the mdd was not the mdd with the longest path among all the mdds),
     * So the extension will be pruned in order to correctly calculate the sum of costs later.
     */
    private static void assignAllocations(ArrayList<MDD> mdds, MergedState currState) {

        // Build allocations including extensions
        ArrayList<LinkedList<Integer>> allocations = new ArrayList<>();
        boolean first = true;
        while (currState != null){
            for (int i = 0; i < mdds.size(); i++){
                if (first) allocations.add(new LinkedList<>());
                int nodeID = currState.mddNodes[i].node.id;
                allocations.get(i).addFirst(nodeID);
            }
            currState = currState.prev;
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

    /**
     * Run at the start of a scenario
     */
    public static void resetIncompatibleMddsSet() {
        incompatibleMddsSet = new HashMap<>();
    }

    /**
     * Add an incompatible pair of mdds (unavoidable collision) to the list.
     * Will always add the mdd with smallest agent id as the key and the other mdd is added
     * to the value of that map entry, which is a list of mdds.
     */
    private static void addIncompatible(MDD mdd1, MDD mdd2) {
        if (mdd1.agent.id > mdd2.agent.id){
            MDD temp = mdd1;
            mdd1 = mdd2;
            mdd2 = temp;
        }
        HashSet<MDD> incompatibles = incompatibleMddsSet.get(mdd1);
        if (incompatibles == null) {
            incompatibles = new HashSet<>();
            incompatibleMddsSet.put(mdd1, incompatibles);
        }
        incompatibles.add(mdd2);
    }

    /**
     * Will check if the set of mdds contains a pair of mdds that are incompatible
     * @param incompatiblePair will be modified to point to the conflicting mdd pair, in case there is one
     * @return true of an incompatible pair was found
     */
    public static boolean isSetIncompatible(ArrayList<MDD> mdds, MDD[] incompatiblePair) {
        for (int i = 0; i < mdds.size()-1; i++){
            MDD mdd1 = mdds.get(i);
            HashSet<MDD> incompatibles = incompatibleMddsSet.get(mdd1);
            if (incompatibles == null) continue;
            for (int j = i + 1; j < mdds.size(); j++){
                MDD mdd2 = mdds.get(j);
                if (incompatibles.contains(mdd2)) {
                    if (incompatiblePair != null) {
                        incompatiblePair[0] = mdd1;
                        incompatiblePair[1] = mdd2;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the MDDNode layer at a certain timestamp
     * @param time of layer
     * @return the layer at timestamp time, or if the timestamp is greater than the mdds' largest timestamp,
     * the last layer (in order to execute the extension needed to search the whole k-mdd search space)
     */
    public ArrayList<MDDNode> getTime(int time) {
        if (time <= cost) return mddNodes[time];
        return mddNodes[cost];
    }

    /**
     * Print stuff during the low level search
     * @param s string to print
     */
    private static void print(String s){
        if (print) System.out.println(s);
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

    /**
     * Get a random legal path from mdd
     * @return int[] with the ids of all the nodes in the path
     */
    public int[] getNextPath(){
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
        return nextPath;
    }

    /**
     * Check if that is legal (all nodes are actually connected)
     * @param t timestamp
     */
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

    /**
     * Find the next path in the mdd
     * @param t timestamp
     */
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

    /**
     * For private purposes
     */
    private void resetOffsets(int t) {
        for (int i = t; i < offsets.length; i++){
            offsets[i] = 0;
        }
    }

    /**
     * Sorts the mdds from the one with the smallest agent id to the biggest one
     */
    public static class MDDComparator implements Comparator<MDD> {
        @Override
        public int compare(MDD o1, MDD o2) {
            return o1.agent.id - o2.agent.id;
        }
    }

    @Override
    public String toString() {
        return agent.id+","+cost;
    }
}
