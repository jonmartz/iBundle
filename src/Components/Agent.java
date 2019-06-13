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
    public Graph graph;

    int i = 0; // todo: delete

    public Agent(Node start, Node goal, ISearcher searcher, Graph graph) {
        this.searcher = searcher;
        HashMap<Node, Node> originalNodeToCopyNodeMap = new HashMap<>();
        this.graph = graph.getCopy(originalNodeToCopyNodeMap);
        this.start = originalNodeToCopyNodeMap.get(start);
        this.goal = originalNodeToCopyNodeMap.get(goal);
    }

    public MDD findNextShortestPaths(){
        i++;
        graph.reset();
        MDD mdd = searcher.findShortestPaths(start, goal);
        int[] nextPath;
        while (i>1 && i<10) {
            nextPath = mdd.getNextPath();
            for (int j : nextPath) System.out.print(j+" ");
            System.out.println();
            i++;
        }
        start = graph.enlargeShortestPaths(mdd); // todo: maybe dont run if allocation granted
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
