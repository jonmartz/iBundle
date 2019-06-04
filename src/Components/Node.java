package Components;

import java.util.HashSet;

public class Node {
    public static int nextId = 0;
    public int id;
    public HashSet<Node> neighbors = new HashSet<>();

    public Node() {
        this.id = nextId++;
    }

    public void addNeighbor(Node neighbor){
        neighbors.add(neighbor);
        neighbor.neighbors.add(this);
    }
}
