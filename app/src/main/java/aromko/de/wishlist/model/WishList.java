package aromko.de.wishlist.model;

import java.util.HashMap;
import java.util.Map;

public class WishList {
    private String name;
    private long timestamp;

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

    public WishList() {
    }

    public WishList(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WishList{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("timestamp", timestamp);
        return result;
    }
}
