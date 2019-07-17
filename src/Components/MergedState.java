package Components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a merged MDD state, which is a possible combination of MDDNodes at a certain time, from each mdd involved.
 */
public class MergedState {
    public int time; // timestamp
    public MDDNode[] mddNodes; // contains one MDDNode from every mdd involved in the current low level search
    public String id; // for hashing purposes
    public MergedState prev; // prev state
    public int[] nextOffsets; // for remembering what was the last neighbor returned
    public int[] nextSizes; // for detecting a change in the size of next neighbors - from a pruning
    public boolean gotAllPossibleNeighbors = false; // true if all this state has already returned all possible neighbors

    /**
     * Constructor. The state's id is in the from [time]:[mddnode 1] [mddnode 2] ... [mddnode n]
     * Where mddNode i is the node that corresponds to mdd i
     * @param time of state
     * @param prev of state
     * @param mddNodes of all the mdds involved
     */
    public MergedState(int time, MergedState prev, MDDNode[] mddNodes) {
        this.time = time;
        this.prev = prev;
        this.mddNodes = mddNodes;
        nextOffsets = new int[mddNodes.length];
        nextSizes = new int[mddNodes.length];
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < mddNodes.length; i++) {
            MDDNode mddNode = mddNodes[i];
            nextSizes[i] = mddNode.nextNeighbors.size();
            strings.add(String.valueOf(mddNode.node.id));
        }
        this.id = time+":"+String.join(" ", strings);
    }

    /**
     * Return the next neighbor. If last neighbor is returned, marks gotAllPossibleNeighbors = true.
     * @return next neighbor
     */
    public MergedState getNextNeighbor() {
        // set all next
        MDDNode[] nextNodes = new MDDNode[mddNodes.length];
        for (int i = 0; i < mddNodes.length; i++){
            ArrayList<MDDNode> nextNeighbors = mddNodes[i].nextNeighbors;
            // check for changed nextNeighbors' size
            if (nextNeighbors.size() < nextSizes[i]){
                // move back, just in case. Worst case is returning the same neighbor twice, not a problem.
                nextOffsets[i] -= nextSizes[i] - nextNeighbors.size();
                if (nextOffsets[i] < 0) nextOffsets[i] = 0;
                nextSizes[i] = nextNeighbors.size();
            }
            if (nextNeighbors.size() == 0) nextNodes[i] = mddNodes[i];
            else nextNodes[i] = nextNeighbors.get(nextOffsets[i]);
        }
        // advance offset pointers
        gotAllPossibleNeighbors = true;
        for (int i = 0; i < mddNodes.length; i++){
            ArrayList<MDDNode> nextNeighbors = mddNodes[i].nextNeighbors;
            nextOffsets[i]++;
            if (nextOffsets[i] >= nextNeighbors.size()){
                nextOffsets[i] = 0;
            }
            else {
                gotAllPossibleNeighbors = false;
                break;
            }
        }
        return new MergedState(this.time+1, this, nextNodes);
    }

    /**
     * Compares the ids
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedState)) return false;
        MergedState that = (MergedState) o;
        return id.equals(that.id);
    }

    /**
     * The hash code is the id string
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

}
