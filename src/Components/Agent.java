package Components;

import Searchers.ISearcher;

import java.util.*;

public class Agent {
    public Node start;
    public Node goal;
    public int [] allocation;
    public ArrayList<Bid> bids = new ArrayList<>();
    public ISearcher searcher;
    public GridGraph graph;
    public int id;
    public static int nextID =1;
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

        this.id = nextID;
        nextID++;

    }

    public MDD findNextShortestPaths(){
        graph.reset();
        MDD mdd = searcher.findShortestPaths(start, goal, this);
        start = graph.enlargeShortestPaths(mdd);
        return mdd;
    }

    public Bid getNextBid(){
        Bid bid = new Bid(this, findNextShortestPaths());
        bids.add(bid);
        return bid;
    }

    @Override
    public String toString() {
        String mood = ":(";
        if (allocation != null) mood = ":D";
        return "id="+id+" "+mood;
    }
}
