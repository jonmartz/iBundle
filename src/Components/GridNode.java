package Components;

public class GridNode extends Node{

    public int x;
    public int y;
    public int z = 0; // for when the graph is expanded with MDD
    public int timeStamp = 0; // to maintain queue order when breaking equality of g+h

    public GridNode(int id, int x, int y) {
        super(id);
        this.x = x;
        this.y = y;
    }

    @Override
    public Node getCopy() {
        GridNode gridNode = new GridNode(id, x, y);
        gridNode.z = this.z;
        return gridNode;
    }

    /**
     * Return the manhattan distance between this node and node, adding also z to
     * allow A* to find all the shortest paths after the graph has been expanded
     * @param node distance from
     * @return manhattan distance
     */
    public int ManhattanDistance(GridNode node) {
        return Math.abs(node.x-x)+Math.abs(node.y-y) + z;
    }

    @Override
    public String toString() {
        return id+" z="+z+" d="+distance;
    }
}
