package Components;

import Searchers.ISearcher;
import java.util.*;

/**
 * Represents an agent with a start and goal nodes in a certain graph.
 */
public class Agent {
    public Node start;
    public Node goal;
    public int [] allocation; // will be null (agent unhappy) if no allocation was given in the current round.
    public ArrayList<Bid> bids = new ArrayList<>(); // All the bid history of the agent
    public ISearcher searcher; // to search a path from start to goal
    public GridGraph graph;
    public int id;

    public static int nextID =1;

    public Agent(int startX, int startY, int goalX, int goalY, ISearcher searcher, String graphPath) {
        this.searcher = searcher;
        graph = new GridGraph(graphPath);
        this.start = graph.getNode(startX, startY);
        this.goal = graph.getNode(goalX, goalY);
        this.id = nextID;
        nextID++;

    }

    /**
     * Finds the paths that are one length len+1 where len is the length of the last paths calculated.
     * @return MDD that represents those paths
     */
    public MDD findNextShortestPaths(){
        graph.reset();
        MDD mdd = searcher.findShortestPaths(start, goal, this);
        start = graph.enlargeShortestPaths(mdd);
        return mdd;
    }

    /**
     * Find the next shortest paths and add a bid with that MDD
     * @return the new bid
     */
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
