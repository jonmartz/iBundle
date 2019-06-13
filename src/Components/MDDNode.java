package Components;

import java.util.HashSet;

public class MDDNode{
    public Node node;
    public int time;
    public HashSet<MDDNode> neighbors = new HashSet<>();

    public MDDNode(Node node, int time){
        this.node = node;
        this.time = time;
    }

    public void addNeighbor(MDDNode neighbor){
        neighbors.add(neighbor);
        neighbor.neighbors.add(this);
    }

    @Override
    public String toString() {
        return node.id + " t=" + time;
    }
}
