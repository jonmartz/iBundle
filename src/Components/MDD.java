package Components;

import java.util.ArrayList;
import java.util.HashSet;

public class MDD {
    public int cost;
    public HashSet<Node> nodes = new HashSet<>();
    public ArrayList<MDDNode>[] mddNodes; // array of array lists. mddNodes[i] = all nodes in time i
    public Node start;
    public Node goal;
    private int[] offsets; // to indicate the current path
    private int currentTime = 0; // current time where path must try to modify path

    public MDD(int cost, Node start, Node goal) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
        mddNodes = new ArrayList[cost+1];
        offsets = new int[cost+1];
    }

    public void add(MDDNode mddNode){
        nodes.add(mddNode.node);
        ArrayList<MDDNode> mddNodesAtTimeT = mddNodes[mddNode.time];
        if (mddNodesAtTimeT == null){
            mddNodesAtTimeT = new ArrayList<>();
            mddNodes[mddNode.time] = mddNodesAtTimeT;
        }
        mddNodesAtTimeT.add(mddNode);
    }

    public int[] getNextPath(){
        findNextPath();
        int[] nextPath = new int[cost];
        int i = 0;
        for (int offset : offsets){
            nextPath[i] = mddNodes[i].get(offset).node.id;
            i++;
        }
        return nextPath;
    }

    private void findNextPath(){
        if (mddNodes[currentTime].size() > offsets[currentTime]+1){
            // go to next node in current time
            offsets[currentTime] += 1;
            // at t=0 and t=cost there's only one node!
            // so no worries about currentTime+1 here
            resetOffsetsFromIndex(currentTime+1);
            // or here
            checkLegalPath(currentTime);
        }
        else{
            // no more nodes in current time, reset offset here
            // and move to next time
            offsets[currentTime] = 0;
            currentTime++;
            if (currentTime > cost) {
                // went through all possible paths: start over
                currentTime = 0;
                resetOffsetsFromIndex(0);
                checkLegalPath(1);
                return;
            }
            findNextPath();
        }
    }

    private void checkLegalPath(int timeChecked) {
        if (timeChecked > cost) return;
        MDDNode currentMddNode = mddNodes[timeChecked].get(offsets[timeChecked]);
        MDDNode previousMddNode = mddNodes[timeChecked-1].get(offsets[timeChecked-1]);
        if (!currentMddNode.neighbors.contains(previousMddNode)){
            // advance one node in current time
            offsets[timeChecked]+=1;
            checkLegalPath(timeChecked);
        }
        // check forward
        else checkLegalPath(timeChecked+1);
    }

    private void resetOffsetsFromIndex(int fromIndex) {
        for (int i = fromIndex; i < cost; i++){
            offsets[i] = 0;
        }
    }
}
