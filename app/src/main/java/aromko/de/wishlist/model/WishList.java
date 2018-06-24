package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

public class WishList {

    @Exclude
    private String key;
    private String name;
    private long timestamp;

    public WishList() {
    }

    public WishList(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return name;
    }


}
