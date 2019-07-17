package Components;

import java.util.ArrayList;

/**
 * Represents a node in a graph. In a run of iBundle, each agents get's a copy of the graph so that is why
 * the node has members that are specific to a certain agent (like the visited member)
 */
public class Node {
    public int id; // incremental
    public ArrayList<Node> neighbors = new ArrayList<>(); // all neighbors
    public ArrayList<Node> previousNodes = new ArrayList<>(); // parents of node, assigned during a single agent search
    public int distance = Integer.MAX_VALUE; // distance from start node
    public boolean visited = false; // true if node was already visited

    /**
     * Constructor
     */
    public Node(int id) {
        this.id = id;
    }

    /**
     * Add neighbor to self's neighbors and self to neighbor's neighbors
     */
    public void addNeighbor(Node neighbor){
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
            neighbor.neighbors.add(this);
        }
    }

    /**
     * Clone
     */
    public Node getCopy() {
        return new Node(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
