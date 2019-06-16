package Components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Graph {
    public ArrayList<Node> nodes = new ArrayList<>();

    public Graph() { }

    public void addNode(Node node){
        nodes.add(node);
    }

    /**
     * Modifies the graph as to simulate "waiting" one time step in any
     * one of the nodes in one of the shortest paths described in the MDD
     * @param mdd containing paths to expand
     * @return new start node (the new start must be the copy of start)
     */
    public Node enlargeShortestPaths(MDD mdd){
        Node newStart = null;
        HashMap<Node, Node> originalNodeToCopyNodeMap = new HashMap<>();
        // in first loop, clone every node in MDD (except for goal)
        // and connect to twin and neighbors (if neighbor not in MDD)
        for (Node node : mdd.nodes){
            if (node == mdd.goal) continue;
            Node nodeCopy = cloneNode(node);
            nodes.add(nodeCopy);
            if (node == mdd.start) newStart = nodeCopy;
            node.addNeighbor(nodeCopy);
            originalNodeToCopyNodeMap.put(node, nodeCopy);
            for (Node neighbor : node.neighbors){
                if (neighbor != nodeCopy && !mdd.nodes.contains(neighbor)) {
                    nodeCopy.addNeighbor(neighbor);
                }
            }
        }
        // in second loop, connect all copies to neighboring copies
        for (Node node : mdd.nodes){
            if (node == mdd.goal) continue;
            for (Node neighbor : node.neighbors){
                if (neighbor == mdd.goal) continue;
                if (mdd.nodes.contains(neighbor)) {
                    Node nodeCopy = originalNodeToCopyNodeMap.get(node);
                    Node neighborCopy = originalNodeToCopyNodeMap.get(neighbor);
                    nodeCopy.addNeighbor(neighborCopy);
                }
            }
        }
        return newStart;
    }

    /**
     * Different graphs may need to implement this differently when enlarging the graph
     * @param node to copy
     * @return copy
     */
    protected Node cloneNode(Node node) {
        if (node instanceof GridNode){
            GridNode clone = (GridNode) node.getCopy();
            clone.z = clone.z+1;
            return clone;
        }
        return node.getCopy();
    }

    public Graph getCopy(HashMap<Node, Node> originalNodeToCopyNodeMap) {
        Graph graphCopy = getNewGraph();
        for (Node node : nodes){
            recursiveCopy(node, graphCopy, originalNodeToCopyNodeMap, null);
            break; // need only one
        }
        return graphCopy;
    }

    /**
     * May need to be overiden in different graphs
     * @return clone of graph
     */
    protected Graph getNewGraph() {
        return new Graph();
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

    public void reset() {
        for (Node node : nodes){
            node.visited = false;
            node.previousNodes = new ArrayList<>();
            node.distance = Integer.MAX_VALUE;
        }
    }

    public Node getRandomNode() {
        Random rand = new Random();
        return nodes.get(rand.nextInt(nodes.size()));
    }
}
