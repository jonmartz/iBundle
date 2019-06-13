package Searchers;

import Components.GridNode;
import Components.Node;

import java.util.Comparator;
import java.util.PriorityQueue;

public class AStarSearcher extends ASearcher{

    public int timeStamp = 0;
    private PriorityQueue<GridNode> openQueue;

    @Override
    protected void initializeQueue() {
        openQueue = new PriorityQueue<>(new GridNodeComparator((GridNode) goal));
        timeStamp = 0;
    }

    @Override
    protected void enqueue(Node node) {
        GridNode gridNode = (GridNode)node;
        gridNode.timeStamp = timeStamp++;
        openQueue.add(gridNode);
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

        /**
         * If g+h is equal, decide for the node with smallest timestamp
         * @return -1 if o1 before o2
         */
        @Override
        public int compare(Object o1, Object o2) {
            GridNode n1 = (GridNode)o1;
            GridNode n2 = (GridNode)o2;
            // f = g + h = distance from start + manhattan distance from goal
            int difference = (n1.ManhattanDistance(goal)+n1.distance)-(n2.ManhattanDistance(goal)+n2.distance);
            if (difference < 0) return -1;
            else if (difference > 0) return 1;
            else return n1.timeStamp-n2.timeStamp;
        }
    }
}
