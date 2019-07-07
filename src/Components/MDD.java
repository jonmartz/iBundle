package Components;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

public class MDD {
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

    public MDD(int cost, Node start, Node goal) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost];
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
            if (!found) mddNodesAtTimeT.add(mddNode);
        }
        return mddNode;
    }

    public int[] getNextPath(){
        gotFirstPath = false;
        if (firstPathEver){
            // to handle getting a path for the first time
            firstPathEver = false;
            gotFirstPath = true;
            checkLegalPath(1);
        }
        else findNextPath(offsets.length-1);
        int[] nextPath = new int[cost+1];
        for (int i = 0; i < cost; i++)
            nextPath[i] = mddNodes[i].get(offsets[i]).node.id;
        nextPath[cost] = goal.id;

//        ArrayList<String> stringPath = new ArrayList<>();
//        for (Integer i : nextPath) stringPath.add(i.toString());
//        System.out.println(String.join(" ",stringPath));
//        if (gotFirstPath) System.out.println("gotFirstPath");

        return nextPath;
    }

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
}
