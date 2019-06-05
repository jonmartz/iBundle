package Components;

import java.util.HashMap;

public class MDD {
    public int cost;
    public HashMap<Integer, MDDNode> mddNodes = new HashMap<>();

    public MDD(int cost) {
        this.cost = cost;
    }

    public void add(MDDNode mddNode){
        mddNodes.put(mddNode.node.id, mddNode);
    }
}
