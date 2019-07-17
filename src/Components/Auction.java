package Components;

import AuctionStrategies.IAuctionStrategy;

import java.util.*;

/**
 * Represents an auction,
 */
public class Auction {
    public int epsilon; //The additional cost added in each round by the Auctioneer (by default 1)
    public IAuctionStrategy strategy; //The auction strategy for determining the winners of a round
    public boolean finished = false; //Is the auction finished

    /**
     * The constructor
     * @param epsilon - The additional cost added in every iteration
     * @param strategy - The Auction strategy
     */
    public Auction(int epsilon, IAuctionStrategy strategy) {
        this.epsilon = epsilon;
        this.strategy = strategy;
    }

    /**
     * Determine the winners of the current bidding and set their allocations
     */
    public void determineWinners(ArrayList<Agent> agents){
        finished = strategy.determineWinners(agents);
    }

    /**
     * update the prices of bids from losers
     */
    public void updatePrices(ArrayList<Agent> agents){
        for(Agent agent : agents)
            if (agent.allocation == null)
                for (Bid bid : agent.bids)
                    bid.price += epsilon;
    }
}
