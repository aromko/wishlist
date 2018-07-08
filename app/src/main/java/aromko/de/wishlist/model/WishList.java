package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class WishList {

    @Exclude
    private String key;
    private String name;
    private long timestamp;
    private Map<String, Object> allowedUsers;

    public WishList() {
    }

    public WishList(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    public WishList(String name, long timestamp, Map<String, Object> allowedUsers) {
        this.name = name;
        this.timestamp = timestamp;
        this.allowedUsers = allowedUsers;
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

    @Override
    public String toString() {
        return name;
    }


}
