package Components;

import AuctionStrategies.IAuctionStrategy;

import java.util.HashSet;

/**
 * This cass represents an auction
 */
public class Auction {
    public int epsilon;//The additional cost added in each round by the Auctioneer (by default 1)
    public HashSet<Bid> bids = new HashSet<>();//The bids made
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
        for (Bid bid : bids){
            if (bid.agent.allocation == null){
                bid.price += epsilon;
            }
        }
    }

    /**
     * This function will add a bid to the auction
     * @param bid - The given bid
     */
    public void addBid(Bid bid){
        bids.add(bid);
    }
}
