package Components;

public class Bid {
    public int price = 0;
    public Agent agent;
    public MDD mdd;

    public Bid(Agent agent, MDD mdd) {
        this.agent = agent;
        this.mdd = mdd;
    }

    public int getValue() {
        return mdd.cost + price;
    }
}
