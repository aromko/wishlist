package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Payment {

    @Exclude
    private String wishId;
    private double price;
    private double salvagePrice;
    private Map<String, Double> partialPayments;

    public Payment() {
    }

    public Payment(double price, double salvagePrice, Map<String, Double> partialPayments) {
        this.price = price;
        this.salvagePrice = salvagePrice;
        this.partialPayments = partialPayments;
    }

    public String getWishId() {
        return wishId;
    }

    public void setWishId(String wishId) {
        this.wishId = wishId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSalvagePrice() {
        return salvagePrice;
    }

    public void setSalvagePrice(double salvagePrice) {
        this.salvagePrice = salvagePrice;
    }

    public Map<String, Double> getPartialPayments() {
        return partialPayments;
    }

    public void setPartialPayments(Map<String, Double> partialPayments) {
        this.partialPayments = partialPayments;
    }

    @Override
    public String toString() {
        return "Payment{" +
                " price=" + price +
                ", salvagePrice=" + salvagePrice +
                ", partialPayments=" + partialPayments +
                '}';
    }
}
