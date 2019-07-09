package Searchers;
import Components.*;

public interface ISearcher {

    /**
     * Find a return a set of all shortest paths (MDD) from start to goal.
     * @param start node
     * @param goal node
     * @return MDD containing all shortest paths
     */
    MDD findShortestPaths(Node start, Node goal, Agent agent);
}
