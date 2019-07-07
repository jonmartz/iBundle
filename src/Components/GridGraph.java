package Components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GridGraph extends Graph {

    public int[][] intGrid;
    public GridNode[][] nodeGrid;

    private GridGraph(){}

    public GridGraph(String mapPath) {
        try {
            File file = new File(mapPath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine(); // ignore type

            // get dimensions
            String line = reader.readLine();
            int rows = Integer.parseInt(line.trim().split(" ")[1]);
            line = reader.readLine();
            int cols = Integer.parseInt(line.trim().split(" ")[1]);
            nodeGrid = new GridNode[rows][cols];
            intGrid = new int[rows][cols];
            line = reader.readLine(); // ignore word "map"

            // make node grid
            int id = 0;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++){
                    intGrid[row][col] = -1;
                    if (line.charAt(col) == '.'){
                        intGrid[row][col] = id;
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

    @Override
    protected Graph getNewGraph() {
        return new GridGraph();
    }

    @Override
    protected Node cloneNode(Node node) {
        GridNode clone = (GridNode) node.getCopy();
        clone.z = clone.z+1;
        return clone;
    }
}
