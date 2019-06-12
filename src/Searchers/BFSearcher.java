package Searchers;

import Components.Node;

import java.util.LinkedList;

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
