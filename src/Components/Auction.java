package Components;

import AuctionStrategies.IAuctionStrategy;

import java.util.HashSet;

public class Auction {
    public int epsilon;
    public HashSet<Bid> bids = new HashSet<>();
    public IAuctionStrategy strategy;

    public Auction(int epsilon, IAuctionStrategy strategy) {
        this.epsilon = epsilon;
        this.strategy = strategy;
    }

    /**
     * Determine the winners of the current bidding and set allocation for winner agents
     */
    public void determineWinners(){
        strategy.determineWinners(bids);
    }
}
