package Components;

import Searchers.ISearcher;

import java.util.HashSet;

public class Agent {
    public Node start;
    public Node goal;
    public MDD allocation;
    public HashSet<MDD> bids = new HashSet<>();
    public ISearcher searcher;

    public Agent(Node start, Node goal, ISearcher searcher) {
        this.start = start;
        this.goal = goal;
        this.searcher = searcher;
    }
}
