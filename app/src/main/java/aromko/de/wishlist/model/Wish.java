package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

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
    private Map<String, Boolean> markedAsFavorite;
    private long timestamp;
    private double longitude;
    private double latitude;

    public Wish() {
    }

    public Wish(String title, double price, String url, String description, long wishstrength, boolean isImageSet, Map<String, Boolean> markedAsFavorite, long timestamp) {
        this.title = title;
        this.price = price;
        this.url = url;
        this.description = description;
        this.wishstrength = wishstrength;
        this.isImageSet = isImageSet;
        this.markedAsFavorite = markedAsFavorite;
        this.timestamp = timestamp;
    }

    public Wish(String title, double price, String url, String description, long wishstrength, boolean isImageSet, long timestamp, double longitude, double latitude) {
        this.title = title;
        this.price = price;
        this.url = url;
        this.description = description;
        this.wishstrength = wishstrength;
        this.isImageSet = isImageSet;
        this.markedAsFavorite = markedAsFavorite;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Boolean> getMarkedAsFavorite() {
        return markedAsFavorite;
    }

    public void setMarkedAsFavorite(Map<String, Boolean> markedAsFavorite) {
        this.markedAsFavorite = markedAsFavorite;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
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
                ", markedAsFavorite=" + markedAsFavorite +
                ", timestamp=" + timestamp +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
