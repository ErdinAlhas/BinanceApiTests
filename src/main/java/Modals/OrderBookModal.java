package Modals;

import java.util.List;

public class OrderBookModal {
    private long lastUpdateId;
    private List<List<String>> bids;
    private List<List<String>> asks;

    public OrderBookModal() {
        super();
    }

    public OrderBookModal(long lastUpdateId, List<List<String>> bids, List<List<String>> asks) {
        this.lastUpdateId = lastUpdateId;
        this.bids = bids;
        this.asks = asks;
    }

    public List<List<String>> getBids() {
        return bids;
    }

    public void setBids(List<List<String>> bids) {
        this.bids = bids;
    }

    public long getLastUpdateId() {
        return lastUpdateId;
    }

    public void setLastUpdateId(long lastUpdateId) {
        this.lastUpdateId = lastUpdateId;
    }

    public List<List<String>> getAsks() {
        return asks;
    }

    public void setAsks(List<List<String>> asks) {
        this.asks = asks;
    }

    @Override
    public String toString() {
        return "OrderBook{" +
                "lastUpdateId=" + lastUpdateId +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }
}
