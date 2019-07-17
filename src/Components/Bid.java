package Components;

/**
 * This class represents a bid
 */
public class Bid {
    public int price = 0;//The additional price to the MDD's cost
    public Agent agent;//The agent that bid the bid
    public MDD mdd;//The MDD ("optional bundles")

    /**
     * The constructor
     * @param agent - The agent that bid the bid
     * @param mdd - The MDD
     */
    public Bid(Agent agent, MDD mdd) {
        this.agent = agent;
        this.mdd = mdd;
    }
}
