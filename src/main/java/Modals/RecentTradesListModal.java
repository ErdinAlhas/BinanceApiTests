package Modals;

public class RecentTradesListModal {
    public RecentTradesListModal() {
    }

    public long id;
    public String price;
    public String qty;
    public String quoteQty;
    public long time;
    public Boolean isBuyerMaker;
    public Boolean isBestMatch;

    public RecentTradesListModal(long id, String price, String qty, String quoteQty, long time,
                                 boolean isBuyerMaker, boolean isBestMatch) {
        this.id = id;
        this.price = price;
        this.qty = qty;
        this.quoteQty = quoteQty;
        this.time = time;
        this.isBuyerMaker = isBuyerMaker;
        this.isBestMatch = isBestMatch;
    }
}