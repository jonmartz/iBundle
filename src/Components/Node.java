package Components;

import java.util.ArrayList;

public class Node {
    public int id;
    public ArrayList<Node> neighbors = new ArrayList<>();
    public ArrayList<Node> previousNodes = new ArrayList<>();
    public int distance = Integer.MAX_VALUE;
    public boolean visited = false;

    public Node(int id) {
        this.id = id;
    }

    public void addNeighbor(Node neighbor){
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
            neighbor.neighbors.add(this);
        }
    }

    public Node getCopy() {
        return new Node(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
