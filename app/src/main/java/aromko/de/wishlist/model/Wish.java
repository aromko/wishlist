package aromko.de.wishlist.model;

public class Wish {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Wish() {
    }

    public Wish(String name) {

        this.name = name;
    }

    @Override
    public String toString() {
        return "Wish{" +
                "name='" + name + '\'' +
                '}';
    }
}
