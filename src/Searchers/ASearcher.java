package Searchers;

import Components.MDD;
import Components.MDDNode;
import Components.Node;

import java.util.HashMap;
import java.util.HashSet;

public abstract class ASearcher implements ISearcher {

    public Node start;
    public Node goal;

    @Override
    public MDD findShortestPaths(Node start, Node goal) {
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
                if (!neighbor.visited){
                    enqueue(neighbor);
                    neighbor.previousNodes.add(current);
                    neighbor.distance = current.distance+1;
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
        MDD mdd = new MDD(goal.distance, start, goal);
        int currentDistance = goal.distance;
        HashSet<MDDNode> currentLayer = new HashSet<>(); // all nodes at distance n from start
        HashSet<Node> nodesAddedToMDD = new HashSet<>();
        MDDNode mddGoal = new MDDNode(goal, currentDistance);
        currentLayer.add(mddGoal);

        while (!currentLayer.isEmpty()){
            HashSet<MDDNode> previousLayer = new HashSet<>();
            HashMap<Node, MDDNode> previousLayerNodeToMDDNodeMap = new HashMap<>();
            for (MDDNode current : currentLayer){
                if (nodesAddedToMDD.contains(current.node)) continue;
                nodesAddedToMDD.add(current.node);
                mdd.add(current);

                for (Node previousNode : current.node.previousNodes){
                    if (previousLayerNodeToMDDNodeMap.containsKey(previousNode)){
                        // get the already existing MDDNode that contains previousNode
                        current.addNeighbor(previousLayerNodeToMDDNodeMap.get(previousNode));
                    }
                    else {
                        MDDNode previousMddNode = new MDDNode(previousNode, currentDistance - 1);
                        current.addNeighbor(previousMddNode);
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
