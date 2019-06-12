package Components;

public class GridNode extends Node{

    public int x;
    public int y;

    public GridNode(int id, int x, int y) {
        super(id);
        this.x = x;
        this.y = y;
    }

    public double distanceTo(GridNode node) {
        return Math.sqrt(Math.pow(node.x-x,2)-Math.pow(node.y-y,2));
    }
}
