package Modal;

public class Trades {
    public Trades() {
    }

    public long id;
    public String price;
    public String qty;
    public String quoteQty;
    public long time;
    public Boolean isBuyerMaker;
    public Boolean isBestMatch;

    public Trades(long id, String price, String qty, String quoteQty, long time,
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