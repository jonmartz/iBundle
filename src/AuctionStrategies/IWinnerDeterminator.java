package AuctionStrategies;

import Components.Agent;

import java.util.List;
import java.util.Set;

/**
 * This interface represents a strategy for determining the winner in an auction iteration
 */
public interface IWinnerDeterminator {

    public Set<Agent> getWinners(List<Agent> agents);
}
