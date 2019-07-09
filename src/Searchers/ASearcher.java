package Searchers;

import Components.Agent;
import Components.MDD;
import Components.MDDNode;
import Components.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class ASearcher implements ISearcher {

    public Agent agent;
    public Node start;
    public Node goal;

    @Override
    public MDD findShortestPaths(Node start, Node goal, Agent agent) {
        this.agent = agent;
        this.start = start;
        this.goal = goal;
        start.distance = 0;
        initializeQueue();
        enqueue(start);
        while(!isQueueEmpty()){
            Node current = dequeue();
            if (current == goal) return getSolution();

            // check if visited
            if (current.visited) continue;
            current.visited = true;

            // add neighbors to queue
            for (Node neighbor : current.neighbors){
                if (!neighbor.visited && neighbor.distance > current.distance){
                    if (neighbor.distance > current.distance+1) // for weird bug on A*
                        neighbor.previousNodes = new ArrayList<>();
                    neighbor.previousNodes.add(current);
                    neighbor.distance = current.distance+1;
                    enqueue(neighbor);
                }
            }
        }
        return null;
    }

    protected abstract void initializeQueue();

    protected abstract void enqueue(Node node);

    protected abstract Node dequeue();

    protected abstract boolean isQueueEmpty();

    /**
     * Build the MDD that contains all the shortest paths
     * @return MDD
     */
    private MDD getSolution() {

        // make new MDD
        MDD mdd = new MDD(goal.distance, start, goal, agent);
        int currentDistance = goal.distance;
        ArrayList<MDDNode> currentLayer = new ArrayList<>(); // all nodes at distance n from start
        HashSet<Node> nodesAddedToMDD = new HashSet<>();
        MDDNode mddGoal = new MDDNode(goal, currentDistance, mdd);
        currentLayer.add(mddGoal);

        while (!currentLayer.isEmpty()){
            ArrayList<MDDNode> previousLayer = new ArrayList<>();
            HashMap<Node, MDDNode> previousLayerNodeToMDDNodeMap = new HashMap<>();
            for (MDDNode current : currentLayer){
                if (nodesAddedToMDD.contains(current.node)) continue;
                nodesAddedToMDD.add(current.node);

                // to not enter twice a node with same id to time t
                MDDNode originalMddNode = mdd.add(current);

                for (Node previousNode : current.node.previousNodes){
                    if (previousLayerNodeToMDDNodeMap.containsKey(previousNode)){
                        // get the already existing MDDNode that contains previousNode
                        originalMddNode.addNeighbor(previousLayerNodeToMDDNodeMap.get(previousNode));
                    }
                    else {
                        MDDNode previousMddNode = new MDDNode(previousNode, currentDistance - 1, mdd);
                        originalMddNode.addNeighbor(previousMddNode);
                        previousLayerNodeToMDDNodeMap.put(previousMddNode.node, previousMddNode);
                        previousLayer.add(previousMddNode);
                    }
                }
            }
            currentLayer = previousLayer;
            currentDistance--;
        }
        return mdd;
    }
}
