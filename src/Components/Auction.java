package Components;

import AuctionStrategies.IAuctionStrategy;

import java.util.HashSet;

public class Auction {
    public int epsilon;
    public HashSet<Bid> bids = new HashSet<>();
    public IAuctionStrategy strategy;
    public boolean finished = false;

    public Auction(int epsilon, IAuctionStrategy strategy) {
        this.epsilon = epsilon;
        this.strategy = strategy;
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

    public void addBid(Bid bid){
        bids.add(bid);
    }
}
