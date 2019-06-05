package Components;

import Searchers.ISearcher;

import java.util.HashSet;

public class Agent {
    public Node start;
    public Node goal;
    public MDD allocation;
    public HashSet<Bid> bids = new HashSet<>();
    public ISearcher searcher;
    public int lowerBoundary = 0;

    public Agent(Node start, Node goal, ISearcher searcher) {
        this.start = start;
        this.goal = goal;
        this.searcher = searcher;
    }

    // todo: have to extend this to get also not shortest paths...
    public MDD findNextShortestPaths(){
        MDD mdd = searcher.findShortestPaths(start, goal, lowerBoundary);
        lowerBoundary = mdd.cost+1;
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
