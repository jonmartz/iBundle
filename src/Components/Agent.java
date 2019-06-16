package Components;

import Searchers.ISearcher;

import java.util.HashMap;
import java.util.HashSet;

public class Agent {
    public Node start;
    public Node goal;
    public int [] allocation;
    public HashSet<Bid> bids = new HashSet<>();
    public ISearcher searcher;
    public Graph graph;
    public int id;
    public static int counter=0;
//    public int i = 0;

    public Agent(Node start, Node goal, ISearcher searcher, Graph graph) {
        this.searcher = searcher;
        HashMap<Node, Node> originalNodeToCopyNodeMap = new HashMap<>();
        this.graph = graph.getCopy(originalNodeToCopyNodeMap);
        this.start = originalNodeToCopyNodeMap.get(start);
        this.goal = originalNodeToCopyNodeMap.get(goal);
        this.id = counter;
        counter++;

    }

    public MDD findNextShortestPaths(){
//        i++;
        graph.reset();
        MDD mdd = searcher.findShortestPaths(start, goal);
        start = graph.enlargeShortestPaths(mdd); // todo: maybe don't run if allocation granted
//        int[] path = new int[8];
//        while (i > 1){
//            path = mdd.getNextPath();
//            i++;
//        }
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
