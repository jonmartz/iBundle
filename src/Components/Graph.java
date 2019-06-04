package Components;

import java.util.HashSet;

public class Graph {
    public HashSet<Node> nodes = new HashSet<>();

    public void addNode(Node node){
        nodes.add(node);
    }
}
