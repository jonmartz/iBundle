package Components;

import Main.Main;
import Searchers.ISearcher;
import jdk.nashorn.internal.objects.Global;

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
    public static int counter=1;
    public Bid currentBid;
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
        graph.reset();
//        if (Main.iteration == 5){
//            int x = 5;
//        }
        MDD mdd = searcher.findShortestPaths(start, goal);
        start = graph.enlargeShortestPaths(mdd); // todo: maybe don't run if allocation granted
        return mdd;
    }

    public Bid getNextBid(){
        Bid bestBid = new Bid(this, findNextShortestPaths());
        bids.add(bestBid);
        for (Bid bid : bids){
            if (bid.getValue()<=bestBid.getValue()) bestBid = bid;
        }
        currentBid = bestBid;
        return bestBid;
    }

    @Override
    public String toString() {
        return "id="+id;
    }
}
