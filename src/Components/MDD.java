package Components;

import java.util.HashMap;
import java.util.HashSet;

public class MDD {
    public int cost;
    public HashSet<Node> nodes = new HashSet<>();
    public HashMap<String, MDDNode> mddNodes = new HashMap<>();
    public Node start;
    public Node goal;

    public MDD(int cost, Node start, Node goal) {
        this.cost = cost;
        this.start = start;
        this.goal = goal;
    }

    public void add(MDDNode mddNode){
        nodes.add(mddNode.node);
        mddNodes.put(mddNode.node.id + " " + mddNode.time, mddNode);
    }
}
