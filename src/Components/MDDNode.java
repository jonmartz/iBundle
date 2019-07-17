package Components;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Represents a node in an MDD, which points to the original node in the graph but also holds a timestamp and
 * specialized lists of neighbors.
 */
public class MDDNode{
    public int offset; // offset of node in it's MDDNode layer at the mdd
    public Node node; // pointer to actual node in the graph
    public int time; // timestamp of node
    public LinkedHashSet<MDDNode> neighbors = new LinkedHashSet<>(); // all neighbors
    public ArrayList<MDDNode> nextNeighbors = new ArrayList<>(); // only neighbors with a higher timestamp
    public MDD mdd; // for MDD merging
    public boolean pruned = false; // true if the node was pruned during the a low level search

    /**
     * Constructor
     * @param node pointer to the node in the graph
     * @param time of getting to the node
     * @param mdd that will contain this MMNode
     */
    public MDDNode(Node node, int time, MDD mdd){
        this.node = node;
        this.time = time;
        this.mdd = mdd;
    }

    /**
     * Add neighbor to self and add self to neighbor
     * @param neighbor to add
     */
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
