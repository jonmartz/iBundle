package Components;

import java.util.*;

import static javax.swing.UIManager.getString;

public class MDD {
    // static variables for the super fun low level search
    private static int neighsAdded; // test
    private static int neighsVisited; // test
    private static int littleAdded = 0;
    private static boolean cancelIfTooLittleAdded = false;
    private static boolean pruneOnCollision = false;
    private static HashMap<String, Boolean> checkedMddSubsets;
    private static boolean print = true;

    // normal members
    public int cost;
    public HashSet<Node> nodes = new HashSet<>();
    public ArrayList<MDDNode>[] mddNodes; // array of array lists. mddNodes[i] = all nodes in time i
    public Node start;
    public Node goal;
    public Agent agent;

    // for getting a possible path
    private int[] offsets; // to indicate the current path (doesn't include t=cost)
    private boolean firstPathEver = true;
    public boolean gotFirstPath = false;

    public MDD(int cost, Node start, Node goal, Agent agent) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost];
        this.agent = agent;
    }

    /**
     * Copy Constructor
     * @param mdd to copy
     */
    public MDD(MDD mdd) {
        this.cost = mdd.cost;
        this.start = mdd.start;
        this.goal = mdd.goal;
        offsets = mdd.offsets;
        this.agent = mdd.agent;
        this.nodes = mdd.nodes;

        // clone this
        this.mddNodes = new ArrayList[cost+1];
        HashMap<MDDNode, MDDNode> originalToCloneMap = new HashMap<>();
        int time = 0;
        for (ArrayList<MDDNode> layer : mdd.mddNodes){
            ArrayList<MDDNode> cloneLayer = new ArrayList<>();
            for (MDDNode mddNode : layer){
                MDDNode cloneMddNode = new MDDNode(mddNode, this);
                cloneLayer.add(cloneMddNode);
                originalToCloneMap.put(mddNode, cloneMddNode);
                for (MDDNode neighbor : mddNode.neighbors){
                    MDDNode cloneNeighbor = originalToCloneMap.get(neighbor);
                    if (cloneNeighbor != null) cloneMddNode.addNeighbor(cloneNeighbor);
                }
            }
            this.mddNodes[time] = cloneLayer;
            time++;
        }
    }

    public static void resetCheckedSubsets(){
        MDD.checkedMddSubsets = new HashMap<>();
    }

    public static boolean getAllocations(ArrayList<MDD> originalMdds) {
        MDD.print = false;

//        // try with all mdds for an easy solution
//        return getSubAllocations(mdds);

////        MDD.cancelIfTooLittleAdded = true;
////        if (getSubAllocations(mdds)) return true;
////        MDD.cancelIfTooLittleAdded = false;
//
        // no easy solution found: go for the hard way :(
        ArrayList<MDD> mdds = cloneMDDs(originalMdds);
        mdds.sort(new MDDComparator());
        System.out.println(mdds.size()+" mdd set: "+getHashString(mdds));

        // check for every pair to prune mdd! :D
        Boolean subsetFeasible = MDD.checkedMddSubsets.get(getHashString(mdds));
        if (subsetFeasible != null) {
            if (!subsetFeasible) return false;
        }
//        else {
        pruneOnCollision = true;
//        for (int i = 2; i < mdds.size(); i++) { // this is for checking all possible subset sizes
//        for (int i = 2; i < 3; i++) { // todo: maybe triples too? - NO: pruning rules too complex
//            System.out.println("    checking subset size "+i);
        if (!pruneMDDs(mdds, new ArrayList<>(), 2, 0)) {
            resetAllocations(mdds);
            System.out.println("        FAIL");
            return false;
//            }
        }
        pruneOnCollision = false;
//        }
        MDD.print = true;
        resetAllocations(mdds);
        System.out.println("checking whole set");
        boolean result = getSubAllocations(mdds, new DFSStateComparator());
        if (!result) System.out.println("        FAIL");
        return result;
    }

    private static ArrayList<MDD> cloneMDDs(ArrayList<MDD> mdds) {
        ArrayList<MDD> cloneMdds = new ArrayList<>();
        for (MDD mdd : mdds) cloneMdds.add(new MDD(mdd));
        return cloneMdds;
    }

    private static void resetAllocations(ArrayList<MDD> mdds) {
        for (MDD mdd : mdds) mdd.agent.allocation = null;
    }

    private static String getHashString(ArrayList<MDD> mdds) {
        ArrayList<String> strings = new ArrayList<>();
        for (MDD mdd : mdds) strings.add(mdd.toString());
        return String.join(" ",strings);
    }

    public static boolean pruneMDDs(ArrayList<MDD> mdds, ArrayList<MDD> mddSubset, int n, int offset){
        Boolean subsetFeasible = null;
        if (mddSubset.size() >= 2){
            subsetFeasible = MDD.checkedMddSubsets.get(getHashString(mddSubset));
            if (subsetFeasible != null && !subsetFeasible) return false;
        }
        if (n == 0){
            if (subsetFeasible != null) return subsetFeasible;
            subsetFeasible = getSubAllocations(mddSubset, new BFSStateComparator());
            MDD.checkedMddSubsets.put(getHashString(mddSubset), subsetFeasible);
            return subsetFeasible;
        }
        for (int i = offset; i < mdds.size(); i++){
            ArrayList<MDD> mddSubsetClone = new ArrayList<>(mddSubset);
            mddSubsetClone.add(mdds.get(i));
            if (!pruneMDDs(mdds, mddSubsetClone, n-1, i+1)) return false;
        }
        return true;
    }


    /**
     * Merge all the MDDs in agentMDDMap to look for collisions.
     * @param mdds to allccate
     * @param stateComparator for BFS / DFS low level search
     * @return If there are paths with no collisions for all agents: mapping from agents to allocations.
     *         else: null.
     */
    public static boolean getSubAllocations(ArrayList<MDD> mdds, StateComparator stateComparator) {
        System.out.println("pair: "+mdds.get(0).agent.id+" "+mdds.get(1).agent.id);
        print("getting alloc, mdds = "+mdds.size());
        if (mdds.size() == 1){
            mdds.get(0).agent.allocation = mdds.get(0).getNextPath();
            return true;
        }

        // If not so lucky:
        PriorityQueue<MergedState> openStack = new PriorityQueue<>(stateComparator);
        HashSet<String> visited = new HashSet<>();

        int goalTime = 0;
        for (MDD mdd : mdds) {
            if (goalTime < mdd.cost) goalTime = mdd.cost;
        }
        MergedState startState = new MergedState(0, null, new int[mdds.size()], mdds);

        print("max cost = " + goalTime);

        openStack.add(startState);
        while (!openStack.isEmpty()){
            if (cancelIfTooLittleAdded && littleAdded > 100){
                littleAdded = 0;
                System.out.println("too little added");
                return false;
            }
            MergedState currState = openStack.remove();
            print("mdd = " + currState.toString()+""); //todo: print anyway sometimes
            if (currState.time == goalTime) {
                assignAllocations(mdds, currState);
                return true;
            }
            if (visited.contains(currState.id)){
                print("             visited");
                continue;
            }
            visited.add(currState.id);
            if (!pruneOnCollision && hasCollision(mdds, currState)) continue;

            neighsAdded = 0;
            neighsVisited = 0;
            addNeighbors(currState, openStack, visited, currState.mddNodes, new ArrayList<>(), mdds);
            print("         n.added="+neighsAdded);
            print("         n.visited="+neighsVisited);
            if (neighsAdded < neighsVisited*0.1) littleAdded += 1;
            else littleAdded = 0;
        }
        return false;
    }

    private static void addNeighbors(MergedState currentState, PriorityQueue<MergedState> openStack,
                                     HashSet<String> visited, MDDNode[] mddNodes,
                                     ArrayList<Integer> offsetList, ArrayList<MDD> mdds) {
        if (offsetList.size() == mddNodes.length){ // generate neighbor
            int[] offsets = new int[offsetList.size()];
            for (int i = 0; i < offsets.length; i++){
                offsets[i] = offsetList.get(i);
            }
            MergedState neighbor = new MergedState(currentState.time+1, currentState, offsets, mdds);
            if (!visited.contains(neighbor.id)){
                if (!pruneOnCollision || !hasCollision(mdds, neighbor)) {
                openStack.add(neighbor);
                neighsAdded++;
                }
            }
            else{
                neighsVisited++;
                // check for collision in case of BFS
                if (pruneOnCollision) hasCollision(mdds, neighbor);
            }
            return;
        }
        MDDNode currentMDDNode = mddNodes[offsetList.size()];
        if (currentMDDNode.nextNeighbors.isEmpty()){
            ArrayList<Integer> offsetListClone = new ArrayList<>(offsetList);
            offsetListClone.add(currentMDDNode.offset);
            addNeighbors(currentState, openStack, visited, mddNodes, offsetListClone, mdds);
        }
        else {
            // weird loop for dealing with dynamically changing nextNeighbors and avoid ConcurrentModificationException
            int i = 0;
            int currSize = currentMDDNode.nextNeighbors.size();
            while (i < currentMDDNode.nextNeighbors.size()) {
                MDDNode next = currentMDDNode.nextNeighbors.get(i);
                ArrayList<Integer> offsetListClone = new ArrayList<>(offsetList);
                offsetListClone.add(next.offset);
                addNeighbors(currentState, openStack, visited, mddNodes, offsetListClone, mdds);
                if (currSize == currentMDDNode.nextNeighbors.size()) i++;
                else currSize = currentMDDNode.nextNeighbors.size();
            }
        }
    }

    private static boolean hasCollision(ArrayList<MDD> mdds, MergedState currState) {
        HashMap<Integer, Integer> nodeIDs = new HashMap<>();
        // check for same position collision
        for (int i = 0; i < mdds.size(); i++){
            int nodeID = currState.mddNodes[i].node.id;
            if (nodeIDs.containsKey(nodeID)){
                print("                     collision");
                System.out.println("        collision: "+currState.time);
                // prune
                if (pruneOnCollision){
                    int j = nodeIDs.get(nodeID);
                    MDD mdd1 = mdds.get(i);
                    MDD mdd2 = mdds.get(j);
                    ArrayList<MDDNode> mmdNodes1 = mdd1.getTime(currState.time);
                    ArrayList<MDDNode> mmdNodes2 = mdd2.getTime(currState.time);
                    MDDNode node1 = currState.mddNodes[i];
                    MDDNode node2 = currState.mddNodes[j];
                    // prune only if the other mdd has only one choice of being there
                    if (mmdNodes1.size() == 1) recursivePruning(node2);
                    else if (mmdNodes2.size() == 1) recursivePruning(node1);
                }
                return true;
            }
            nodeIDs.put(nodeID, i);
        }
        // check for swap position collision
        if (currState.prev != null){
            MergedState prevState = currState.prev;
            HashMap<String, Integer> nodeIDpairs = new HashMap<>();
            for (int i1 = 0; i1 < mdds.size(); i1++) {
                int idPrev = prevState.mddNodes[i1].node.id;
                int idCurr = currState.mddNodes[i1].node.id;
                if (nodeIDpairs.containsKey(String.valueOf(idCurr)+" "+String.valueOf(idPrev))){
                    print("                     swap collision");
                    System.out.println("        swap collision: "+currState.time);
                    if (pruneOnCollision){
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
                        if (currNodes1.size() == 1 && prevNodes1.size() == 1){
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
                            if (prevNode.nextNeighbors.size() == 1)
                                // case 1: if curr is only next of prev, prune prev
                                recursivePruning(prevNode);
                            else if (currNode.neighbors.size() - currNode.nextNeighbors.size() == 1)
                                // case 1: if prev is only prev of curr, prune curr
                                recursivePruning(currNode);
                        }
                    }
                    return true;
                }
                nodeIDpairs.put(String.valueOf(idPrev)+" "+String.valueOf(idCurr), i1);
            }
        }
        return false;
    }

    private static void recursivePruning(MDDNode node) {
        System.out.println("            PRUNE!");
        ArrayList<MDDNode> siblings = node.mdd.getTime(node.time);
        ArrayList<MDDNode> prevs = node.mdd.getTime(node.time-1);
        siblings.remove(node);
        for (MDDNode sibling : siblings)
            if (sibling.offset > node.offset)
                sibling.offset--;
        for (MDDNode prev : prevs)
            prev.nextNeighbors.remove(node);
        ArrayList<MDDNode> prevsBackup = new ArrayList<>(prevs);
        for (MDDNode prev : prevsBackup)
            if (prev.nextNeighbors.size() == 0)
                recursivePruning(prev);
    }

    /**
     * Assign path to each allocation;
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

    public ArrayList<MDDNode> getTime(int time) {
        if (time <= cost) return mddNodes[time];
        return mddNodes[cost];
    }

    private static abstract class StateComparator implements Comparator<MergedState>{}

    private static class DFSStateComparator extends StateComparator {
        @Override
        public int compare(MergedState o1, MergedState o2) {
            return o2.time - o1.time;
        }
    }

    private static class BFSStateComparator extends StateComparator{
        @Override
        public int compare(MergedState o1, MergedState o2) {
            return o1.time - o2.time;
        }
    }

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

    private static class MDDComparator implements Comparator<MDD> {
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
