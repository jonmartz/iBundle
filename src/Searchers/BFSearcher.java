package Searchers;

import Components.Node;

import java.util.LinkedList;

/**
 * Represents the BFS algorithm, for single agent shortest path search. Find ALL the possible shortest paths.
 */
public class BFSearcher extends ASearcher{

    private LinkedList<Node> openQueue;

    @Override
    protected void initializeQueue() {
        openQueue = new LinkedList<>();
    }

    @Override
    protected void enqueue(Node node) {
        openQueue.add(node);
    }

    @Override
    protected Node dequeue() {
        return openQueue.pollFirst();
    }

    @Override
    protected boolean isQueueEmpty() {
        return openQueue.isEmpty();
    }
}
