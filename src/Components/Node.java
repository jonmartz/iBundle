package Components;

import java.util.HashSet;

public class Node {
    public int id;
    public HashSet<Node> neighbors = new HashSet<>();

    public Node(int id) {
        this.id = id;
    }

    public void addNeighbor(Node neighbor){
        neighbors.add(neighbor);
        neighbor.neighbors.add(this);
    }

    public void removeNeighbor(Node neighbor){
        neighbors.remove(neighbor);
        neighbor.neighbors.remove(this);
    }

    public Node getCopy() {
        return new Node(id);
    }
}
