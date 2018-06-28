package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

public class Wish {

    @Exclude
    private String wishId;
    @Exclude
    private String wishlistId;
    private String title;
    private double price;
    private String url;
    private String description;
    private long wishstrength;
    private boolean isImageSet;
    private boolean isFavorite;
    private long timestamp;

    public Wish() {
    }

    public Wish(String title, double price, String url, String description, long wishstrength, boolean isImageSet, boolean isFavorite, long timestamp) {
        this.title = title;
        this.price = price;
        this.url = url;
        this.description = description;
        this.wishstrength = wishstrength;
        this.isImageSet = isImageSet;
        this.isFavorite = isFavorite;
        this.timestamp = timestamp;
    }

    @Exclude
    public String getWishId() {
        return wishId;
    }

    @Exclude
    public void setWishId(String wishId) {
        this.wishId = wishId;
    }

    @Exclude
    public String getWishlistId() {
        return wishlistId;
    }

    @Exclude
    public void setWishlistId(String wishlistId) {
        this.wishlistId = wishlistId;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
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
                ", isFavorite=" + isFavorite +
                ", timestamp=" + timestamp +
                '}';
    }
}
