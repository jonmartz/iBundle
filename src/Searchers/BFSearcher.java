package Searchers;


import Components.MDD;
import Components.MDDNode;
import Components.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class BFSearcher implements ISearcher {

    public LinkedList<Node> queue;
    public HashSet<Node> visited;
    public HashMap<Node, ArrayList<Node>> previousNodes;
    public HashMap<Node, Integer> distances;

    @Override
    public MDD findShortestPaths(Node start, Node goal) {
        //todo: use lowerBoundary
        visited = new HashSet<>();
        queue = new LinkedList<>();
        previousNodes = new HashMap<>();
        distances = new HashMap<>();
        queue.add(start);
        previousNodes.put(start, new ArrayList<>());
        distances.put(start,0);

        while(!queue.isEmpty()){
            // get next node from open queue
            Node current = queue.pollFirst();
            if (current == goal) return getSolution(start, goal, distances.get(goal));

            // check if visited
            if (visited.contains(current)) continue;
            visited.add(current);

            // add neighbors to queue
            int distance = distances.get(current);
            for (Node neighbor : current.neighbors){
                if (!visited.contains(neighbor)){
                    queue.add(neighbor);
                    addPreviousNode(neighbor, current);
                    distances.put(neighbor,distance+1);
                }
            }
        }
        return null;
    }

    /**
     * Build the MDD that contains all the shortest paths
     * @param start node
     * @param goalDistance  of shortest path
     * @return MDD
     */
    private MDD getSolution(Node start, Node goal, int goalDistance) {

        // make new MDD
        int distance = goalDistance;
        MDD mdd = new MDD(distance);

        HashMap<Integer, MDDNode> currentLayer = new HashMap<>(); // all nodes at distance n from start
        MDDNode mddGoal = new MDDNode(goal, distance);
        currentLayer.put(mddGoal.node.id, mddGoal);

        while (!currentLayer.isEmpty()){
            HashMap<Integer, MDDNode> previousLayer = new HashMap<>();

            for (MDDNode current : currentLayer.values()){ // for each node in current layer
                if (mdd.mddNodes.containsKey(current.node.id)) continue;
                mdd.add(current);
                ArrayList<Node> previousNodesOfCurrent =  previousNodes.get(current.node);

                for (Node previousNode : previousNodesOfCurrent){
                    // add previous nodes to mddDone and to prev layer
//                    if (mdd.mddNodes.containsKey(previousNode.id)){
//                        current.addNeighbor(mdd.mddNodes.get(previousNode.id));
//                    }
                    if (previousLayer.containsKey(previousNode.id)){
                        current.addNeighbor(previousLayer.get(previousNode.id));
                    }
                    else {
                        MDDNode previousMddNode = new MDDNode(previousNode, distance - 1);
                        current.addNeighbor(previousMddNode);
                        previousLayer.put(previousMddNode.node.id, previousMddNode);
                    }
                }
            }
            currentLayer = previousLayer;
            distance--;
        }
        return mdd;
    }

    /**
     * Add previousNode to the list of node's previousNodes
     * @param node to add previous to
     * @param previousNode of node
     */
    private void addPreviousNode(Node node, Node previousNode) {
        ArrayList<Node> previousNodesOfNode = previousNodes.get(node);
        if (previousNodesOfNode == null){
            previousNodesOfNode = new ArrayList<>();
            previousNodes.put(node, previousNodesOfNode);
        }
        previousNodesOfNode.add(previousNode);
    }
}
