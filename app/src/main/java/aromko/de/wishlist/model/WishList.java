package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class WishList {

    @Exclude
    private String key;
    private String name;
    private long timestamp;
    private Map<String, Object> allowedUsers;
    private int wishCounter;
    private boolean isFavoriteList;

    public WishList() {
    }

    public WishList(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    public WishList(String name, long timestamp, Map<String, Object> allowedUsers, int wishCounter, boolean isFavoriteList) {
        this.name = name;
        this.timestamp = timestamp;
        this.allowedUsers = allowedUsers;
        this.wishCounter = wishCounter;
        this.isFavoriteList = isFavoriteList;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(Map<String, Object> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public int getWishCounter() {
        return wishCounter;
    }

    public void setWishCounter(int wishCounter) {
        this.wishCounter = wishCounter;
    }

    public boolean isFavoriteList() {
        return isFavoriteList;
    }

    public void setFavoriteList(boolean favoriteList) {
        isFavoriteList = favoriteList;
    }

    @Override
    public String toString() {
        return "WishList{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", allowedUsers=" + allowedUsers +
                ", wishCounter=" + wishCounter +
                ", isFavoriteList=" + isFavoriteList +
                '}';
    }
}
