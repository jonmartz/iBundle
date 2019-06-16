package Components;

import AuctionStrategies.IAuctionStrategy;

import java.util.*;

/**
 * This cass represents an auction
 */
public class Auction {
    public int epsilon;//The additional cost added in each round by the Auctioneer (by default 1)
    public HashMap<Agent,Set<Bid>> bids = new HashMap<>();//The bids made
    public IAuctionStrategy strategy;//The auction strategy
    public boolean finished = false;//Is the auction finished

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
     * The constructor
     * epsilon - The additional cost added in every iteration (set to 1)
     * @param strategy - The Auction strategy
     */
    public Auction(IAuctionStrategy strategy) {
        this(1,strategy);
    }
    /**
     * Determine the winners of the current bidding and set their allocations
     */
    public void determineWinners(){
        finished = strategy.determineWinners(bids);
    }

    /**
     * update the prices of bids from losers
     */
    public void updatePrices(){
        Collection<Agent> agents = this.bids.keySet();
        for(Agent agent : agents)
        {
            if (agent.allocation == null){
                Collection<Bid> bids = this.bids.get(agent);
                for (Bid bid : bids){
                    bid.price += epsilon;

                }
            }
        }

    }

    /**
     * This function will add a bid to the auction
     * @param bid - The given bid
     */
    public void addBid(Bid bid){
        Set<Bid> bidHistory = bids.get(bid.agent);
        if(bidHistory == null)
        {
            bidHistory = new HashSet<>();
            this.bids.put(bid.agent,bidHistory);
        }
        bidHistory.add(bid);
    }
}
