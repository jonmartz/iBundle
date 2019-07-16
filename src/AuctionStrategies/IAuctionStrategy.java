package AuctionStrategies;
import Components.*;

import java.util.*;

/**
 * This interface contains the necessary functions for a class o implement inorder to
 * Create an AuctionStrategy class
 * */
public interface IAuctionStrategy {

    /**
     * Determine the winners of the current bidding and set allocation for winner agents.
     * @return true if auction is to be ended, false otherwise.
     */
    boolean determineWinners(ArrayList<Agent> agents);
}
