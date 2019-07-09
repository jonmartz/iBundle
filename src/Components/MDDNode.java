package Components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class MDDNode{
    public int offset;
    public Node node;
    public int time;
    public LinkedHashSet<MDDNode> neighbors = new LinkedHashSet<>();
    public ArrayList<MDDNode> nextNeighbors = new ArrayList<>(); // for MDD merging
    public MDD mdd; // for MDD merging

    public MDDNode(Node node, int time, MDD mdd){
        this.node = node;
        this.time = time;
        this.mdd = mdd;
    }

    public void addNeighbor(MDDNode neighbor){
        neighbors.add(neighbor);
        neighbor.neighbors.add(this);
        if (neighbor.time > this.time) nextNeighbors.add(neighbor);
        if (neighbor.time < this.time) neighbor.nextNeighbors.add(this);
    }

    @Override
    public String toString() {
        return node.id + " t=" + time;
    }
}
