package aromko.de.wishlist.model;

public class Wish {

    private String name;
    private double price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Wish() {
    }

    @Override
    public String toString() {
        return "Wish{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    public Wish(String name, double price) {
        this.name = name;
        this.price = price;
    }
}
