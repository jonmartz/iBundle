package Components;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Graph {
    public ArrayList<Node> nodes = new ArrayList<>();

    public Graph() { }

    public Graph(String mapPath) {
        try {
            File file = new File(mapPath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine(); // ignore type

            // get dimensions
            String line = reader.readLine();
            int rows = Integer.parseInt(line.trim().split(" ")[1]);
            line = reader.readLine();
            int cols = Integer.parseInt(line.trim().split(" ")[1]);
            GridNode[][] nodeGrid = new GridNode[rows][cols];
            line = reader.readLine(); // ignore word "map"

            // make node grid
            int id = 0;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++){
                    if (line.charAt(col) == '.'){
                        // new node
                        GridNode node = new GridNode(id++, col, row);
                        addNode(node);
                        nodeGrid[row][col] = node;

                        // connect node with upper and left nodes
                        if (row > 0 && nodeGrid[row-1][col] != null) node.addNeighbor(nodeGrid[row-1][col]);
                        if (col > 0 && nodeGrid[row][col-1] != null) node.addNeighbor(nodeGrid[row][col-1]);
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            Node nodeCopy = node.getCopy();
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
