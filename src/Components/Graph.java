package Components;

import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    public HashSet<Node> nodes = new HashSet<>();

    public void addNode(Node node){
        nodes.add(node);
    }

    /**
     *  Modifies the graph as to simulate "waiting" one time step in any
     *  one of the nodes in one of the shortest paths described in the MDD
     * @param mdd containing paths to expand
     */
    public void enlargeShortestPaths(MDD mdd){
        
    }

    public Graph getCopy(HashMap<Node, Node> originalNodeToCopyNodeMap) {
        Graph graphCopy = new Graph();
        for (Node node : nodes){
            recursiveCopy(node, graphCopy, originalNodeToCopyNodeMap, null);
            break; // need only one
        }
        return graphCopy;
    }

    private void recursiveCopy(Node node, Graph graphCopy, HashMap<Node, Node> originalNodeToCopyNodeMap, Node prevNodeCopy) {
        Node nodeCopy = node.getCopy();
        graphCopy.addNode(nodeCopy);
        if (prevNodeCopy != null) nodeCopy.addNeighbor(prevNodeCopy);
        originalNodeToCopyNodeMap.put(node, nodeCopy); // to not add a neighbor twice, and be able to have more than one node with same id
        for (Node neighbor : node.neighbors){
            if (!originalNodeToCopyNodeMap.containsKey(neighbor)){
                recursiveCopy(neighbor, graphCopy, originalNodeToCopyNodeMap, nodeCopy);
            }
            else { // make them neighbors!
                nodeCopy.addNeighbor(originalNodeToCopyNodeMap.get(neighbor));
            }
        }
    }
}
