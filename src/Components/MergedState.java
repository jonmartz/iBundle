package Components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MergedState {
    public int time;
    public MDDNode[] mddNodes;
    public String id;
    public MergedState prev;

    public MergedState(int time, MergedState prev, int[] offsets, ArrayList<MDD> mdds) {
        this.time = time;
        this.prev = prev;
        mddNodes = new MDDNode[offsets.length];
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < offsets.length; i++) {
            MDDNode mddNode = mdds.get(i).getTime(time).get(offsets[i]);
            mddNodes[i] = mddNode;
            strings.add(String.valueOf(mddNode.node.id));
        }
        this.id = time+":"+String.join(" ", strings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedState)) return false;
        MergedState that = (MergedState) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
