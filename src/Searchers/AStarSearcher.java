package Searchers;

import Components.GridNode;
import Components.Node;

import java.util.Comparator;
import java.util.PriorityQueue;

public class AStarSearcher extends ASearcher{

    private PriorityQueue<GridNode> openQueue;

    @Override
    protected void initializeQueue() {
        openQueue = new PriorityQueue<>(new GridNodeComparator((GridNode) goal));
    }

    @Override
    protected void enqueue(Node node) {
        openQueue.add((GridNode) node);
    }

    @Override
    protected Node dequeue() {
        return openQueue.poll();
    }

    @Override
    protected boolean isQueueEmpty() {
        return openQueue.isEmpty();
    }

    private class GridNodeComparator implements Comparator {

        private GridNode goal;

        public GridNodeComparator(GridNode goal) {
            this.goal = goal;
        }

        @Override
        public int compare(Object o1, Object o2) {
            GridNode n1 = (GridNode)o1;
            GridNode n2 = (GridNode)o2;
            return (int)(n1.distanceTo(goal)-n2.distanceTo(goal));
        }
    }
}
