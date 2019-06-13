package Components;

import java.util.ArrayList;
import java.util.List;

public class MDDPath {
    List<MDDNode> nodes;//The nodes in the Path (sorted by time)

    /**
     * The constructor
     * @param mddNodes - The mddNodes that are in the path
     */
    public MDDPath(List<MDDNode> mddNodes)
    {
        this.nodes = mddNodes;
    }

    /**
     * The constructor
     */
    public MDDPath()
    {
        this.nodes = new ArrayList<>();
    }



}
