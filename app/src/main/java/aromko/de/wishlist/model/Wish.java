package aromko.de.wishlist.model;

public class Wish {

    private String title;
    private double price;
    private String url;
    private String description;
    private long wishstrength;
    private boolean isImageSet;
    private long timestamp;

    public Wish() {
    }

    public Wish(String title, double price, String url, String description, long wishstrength, boolean isImageSet, long timestamp) {
        this.title = title;
        this.price = price;
        this.url = url;
        this.description = description;
        this.wishstrength = wishstrength;
        this.isImageSet = isImageSet;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getWishstrength() {
        return wishstrength;
    }

    public void setWishstrength(long wishstrength) {
        this.wishstrength = wishstrength;
    }

    public boolean isImageSet() {
        return isImageSet;
    }

    public void setImageSet(boolean imageSet) {
        isImageSet = imageSet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Wish{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", wishstrength=" + wishstrength +
                ", isImageSet=" + isImageSet +
                ", timestamp=" + timestamp +
                '}';
    }
}
