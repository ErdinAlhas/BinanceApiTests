package Modal;

public class AvgPriceModal {
    public AvgPriceModal() {
        super();
    }
    public int mins;
    public String price;
    public long closeTime;

    public int getMins() { return mins; }
    public void setMins(int mins) { this.mins = mins; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public long getCloseTime() { return closeTime; }
    public void setCloseTime(long closeTime) { this.closeTime = closeTime; }
}
