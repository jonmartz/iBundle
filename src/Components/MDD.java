package Components;

import Main.Main;

import java.util.*;

public class MDD {
    // static variables for the super fun low level search
    public static boolean print = false;
    public static long startTime;
    private static ArrayList<MDDNode> nodeBackup;
    private static boolean unsolvable = false;
    public static boolean skipCollisionChecking = false;
    public static HashMap<MDD, HashSet<MDD>> incompatibleMddsSet;
    public static boolean timeout = false;
    public static long timeoutSeconds = 1;

    // normal members
    public int cost;
    public HashSet<Node> nodes = new HashSet<>();
    public ArrayList<MDDNode>[] mddNodes; // array of array lists. mddNodes[i] = all nodes in time i
    public Node start;
    public Node goal;
    public Agent agent;

    // for getting a possible path
    private int[] offsets; // to indicate the current path (doesn't include t=cost)
    public boolean gotFirstPath = false;

    public MDD(int cost, Node start, Node goal, Agent agent) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost];
        this.agent = agent;
    }

    public static boolean getAllocations(ArrayList<MDD> mdds) {
//        MDD.print = false;
        mdds.sort(new MDDComparator());

        ArrayList<String> strings = new ArrayList<>();
        for (MDD mdd : mdds) strings.add(String.valueOf(mdd.agent.id));
//        System.out.println("    "+mdds.size()+" mdd set: "+String.join(" ",strings));

        // If already solved and was found unfeasible
//        Boolean subsetFeasible = MDD.checkedMddSubsets.get(getHashString(mdds));
//        if (subsetFeasible != null) {
//            if (!subsetFeasible) return false;
//        }

        // check every pair for pruning :D
//        if (!pruneMDDs(mdds, new ArrayList<>(), 2, 0)) {
//            resetAllocations(mdds);
//            System.out.println("        FAIL");
//            return false;
//        }

        nodeBackup = new ArrayList<>();
        boolean result = lowLevelSearch(mdds);
        restoreMdds();
//        if (!result) System.out.println("   FAIL");
        return result;
    }

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

    private static String getHashString(ArrayList<MDD> mdds) {
        // todo: make a hashSet for incompatible pairs:
        // Each time an unsolvable conflict between two mdds is found,
        // add the mdd pair to the hash. In each iteration, before triggering
        // the low level search, look for each possible pair for conflicts
        ArrayList<String> strings = new ArrayList<>();
        for (MDD mdd : mdds) strings.add(mdd.toString());
        return String.join(" ",strings);
    }

    /**
     * Merge all the MDDs in agentMDDMap to look for collisions.
     * @param mdds to allccate
     * @return true if there are paths with no collisions for all agents
     */
    public static boolean lowLevelSearch(ArrayList<MDD> mdds) {
//        System.out.println("pair: "+mdds.get(0).agent.id+" "+mdds.get(1).agent.id);

        if (timeout()) return false;

        print("low level search, mdds = "+mdds.size());
        if (mdds.size() == 1){
            mdds.get(0).agent.allocation = mdds.get(0).getNextPath();
            return true;
        }

        // If not so lucky:
//        PriorityQueue<MergedState> openStack = new PriorityQueue<>(stateComparator);
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

            MergedState currState = openStack.removeFirst();
            print("mdd = " + currState.toString()+"");
            if (currState.time == goalTime) {
                assignAllocations(mdds, currState);
                return true;
            }

            // May have a pointer to a node that was already pruned
            // todo: maybe useless after implementing iterative neighbor adding
            boolean pruned = false;
            for (MDDNode mddNode : currState.mddNodes) {
                if (mddNode.pruned) {
                    print("        ALREADY PRUNED!");
                    pruned = true;
                    break;
                }
            }
            if (pruned) continue;

            if (visited.contains(currState.id)){
                print("             visited");
                continue;
            }
            MergedState neighbor = currState.getNextNeighbor();
            if (!currState.gotAllPossibleNeighbors) openStack.addFirst(currState);
            else visited.add(currState.id);
            if (!hasCollision(mdds, neighbor)) openStack.addFirst(neighbor);
            if (unsolvable){
                unsolvable = false;
                return false;
            }
        }
        return false;
    }

    private static boolean timeout() {
        if (System.currentTimeMillis()-startTime > timeoutSeconds*1000){ // more than two minutes...
            timeout = true;
            return true;
        }
        return false;
    }

    private static boolean hasCollision(ArrayList<MDD> mdds, MergedState currState) {
        if (skipCollisionChecking) return false;
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

    private static void recursivePruning(MDDNode node) {
        if (node.time == 0){
            System.out.println("    TRIED PRUNING ROOT");
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

    public static void resetIncompatibleMddsSet() {
        incompatibleMddsSet = new HashMap<>();
    }

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

//    public static boolean isSetIncompatible(ArrayList<MDD> mdds) {
//        for (int i = 0; i < mdds.size()-1; i++){
//            MDD mdd = mdds.get(i);
//            HashSet<MDD> incompatibles = incompatibleMddsSet.get(mdd);
//            if (incompatibles == null) continue;
//            for (int j = i + 1; j < mdds.size(); j++){
//                if (incompatibles.contains(mdds.get(j)))
//                    return true;
//            }
//        }
//        return false;
//    }

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
