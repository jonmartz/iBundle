package Components;

import Searchers.ISearcher;

import java.util.HashMap;
import java.util.HashSet;

public class Agent {
    public Node start;
    public Node goal;
    public MDD allocation;
    public HashSet<Bid> bids = new HashSet<>();
    public ISearcher searcher;
    public int lowerBoundary = 0;
    public Graph graph;

    public Agent(Node start, Node goal, ISearcher searcher, Graph graph) {
        this.searcher = searcher;
        HashMap<Node, Node> originalNodeToCopyNodeMap = new HashMap<>();
        this.graph = graph.getCopy(originalNodeToCopyNodeMap);
        this.start = originalNodeToCopyNodeMap.get(start);
        this.goal = originalNodeToCopyNodeMap.get(goal);
    }

    // todo: have to extend this to get also not shortest paths...
    public MDD findNextShortestPaths(){
        MDD mdd = searcher.findShortestPaths(start, goal);
        graph.enlargeShortestPaths(mdd); // so next time we'll get only longer paths than this time
        return mdd;
    }

    public Bid getNextBid(){
        Bid bestBid = new Bid(this, findNextShortestPaths());
        bids.add(bestBid);
        for (Bid bid : bids){
            if (bid.getValue()<=bestBid.getValue()) bestBid = bid;
        }
        return bestBid;
    }
}
