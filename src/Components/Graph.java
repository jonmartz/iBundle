package Components;

import java.util.*;

/**
 * Represents a graph
 */
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
//            if (node == mdd.goal) continue;
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
//            if (node == mdd.goal) continue;
            for (Node neighbor : node.neighbors){
//                if (neighbor == mdd.goal) continue;
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

//    /**
//     * Get a deep copy of the graph
//     * @param originalNodeToCopyNodeMap map of node copies, gotten from outside so that caller can
//     *                                  retrieve start and goal nodes from it
//     * @return a copy of the graph
//     */
//    public Graph getCopy(HashMap<Node, Node> originalNodeToCopyNodeMap) {
//        Graph graphCopy = getNewGraph();
//        LinkedList<Node[]> nodeQueue = new LinkedList<>();
//        Node[] firstEntry = {nodes.get(0), null};
//        nodeQueue.add(firstEntry);
//        HashSet<Node> nodesAddedToQueue = new HashSet<>();
//        nodesAddedToQueue.add(nodes.get(0));
//        while(iterativeCopy(nodeQueue, graphCopy, originalNodeToCopyNodeMap, nodesAddedToQueue));
//        return graphCopy;
//    }

//    /**
//     * May need to be overiden in different graphs
//     * @return clone of graph
//     */
//    protected Graph getNewGraph() {
//        return new Graph();
//    }
//
//    /**
//     * Copy the graph. If copy has been completed, returns false. Is iterative in order to avoid a stack overflow.
//     * @param nodeQueue to retrieve node to copy and the prev node copied
//     * @param graphCopy graph to copy
//     * @param originalNodeToCopyNodeMap explained before
//     * @return true to continue, false to end copy
//     */
//    private boolean iterativeCopy(LinkedList<Node[]> nodeQueue, Graph graphCopy,
//                                  HashMap<Node, Node> originalNodeToCopyNodeMap,
//                                  HashSet<Node> nodesAddedToQueue) {
//
//        if (nodeQueue.isEmpty()) return false;
//
//        // get nodes from queue
//        Node[] nodesEntry = nodeQueue.removeFirst();
//        Node node = nodesEntry[0];
//        Node prevNode = nodesEntry[1];
//        Node nodeCopy = node.getCopy(); // will not be null
//        Node prevNodeCopy = null;
//        if (prevNode != null) prevNodeCopy = nodesEntry[1].getCopy();
//        nodesAddedToQueue.remove(node);
//
//        // connect nodes
//        graphCopy.addNode(nodeCopy);
//        if (prevNodeCopy != null) nodeCopy.addNeighbor(prevNodeCopy);
//
//        // to not add a neighbor twice,and be able to have more than one node with same id
//        originalNodeToCopyNodeMap.put(node, nodeCopy);
//
//        for (Node neighbor : node.neighbors){
//            if (!originalNodeToCopyNodeMap.containsKey(neighbor)){
//                if (nodesAddedToQueue.contains(neighbor)) continue;
//                Node[] newEntry = {neighbor, nodeCopy};
//                nodeQueue.add(newEntry);
//                nodesAddedToQueue.add(neighbor);
//            }
//            else { // make them neighbors anyway!
//                nodeCopy.addNeighbor(originalNodeToCopyNodeMap.get(neighbor));
//            }
//        }
//        return true;
//    }

    /**
     * Reset the state of the nodes in the graph, for doing more than one search in the graph
     */
    public void reset() {
        for (Node node : nodes){
            node.visited = false;
            node.previousNodes = new ArrayList<>();
            node.distance = Integer.MAX_VALUE;
        }
    }

    /**
     * Return a random node
     */
    public Node getRandomNode() {
        Random rand = new Random();
        return nodes.get(rand.nextInt(nodes.size()));
    }
}
