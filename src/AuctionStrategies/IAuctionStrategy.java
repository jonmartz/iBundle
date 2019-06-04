package AuctionStrategies;
import Components.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

public interface IAuctionStrategy {

    /**
     * Determine the winners of the current bidding and set allocation for winner agents
     */
    void determineWinners(HashSet<Bid> bids);
}
