package aromko.de.wishlist.model;

public class HotStock {
    private String ticker;
    private float price;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String toString() {
        return "{HotStock ticker=" + ticker + " price=" + price + "}";
    }
}