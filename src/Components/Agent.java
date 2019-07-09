package Components;

import Main.Main;
import Searchers.ISearcher;
import jdk.nashorn.internal.objects.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Agent {
    public Node start;
    public Node goal;
    public int [] allocation;
    public List<Bid> bids = new ArrayList<>();
    public ISearcher searcher;
    public GridGraph graph;
    public int id;
    public static int counter=1;
    public Bid currentBid;
//    public int i = 0;

    public Agent(int startX, int startY, int goalX, int goalY, ISearcher searcher, String graphPath) {
        this.searcher = searcher;
        graph = new GridGraph(graphPath);
        this.start = graph.getNode(startX, startY);
        this.goal = graph.getNode(goalX, goalY);

//        HashMap<Node, Node> originalNodeToCopyNodeMap = new HashMap<>();
//        this.graph = graph.getCopy(originalNodeToCopyNodeMap);
//        this.start = originalNodeToCopyNodeMap.get(start);
//        this.goal = originalNodeToCopyNodeMap.get(goal);

        this.id = counter;
        counter++;

    }

    public MDD findNextShortestPaths(){
        graph.reset();
//        if (Main.iteration == 5){
//            int x = 5;
//        }
        MDD mdd = searcher.findShortestPaths(start, goal, this);
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
        String mood = ":(";
        if (allocation != null) mood = ":D";
        return "id="+id+" "+mood;
    }
}
