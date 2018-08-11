package aromko.de.wishlist.model;

import com.google.firebase.database.Exclude;

public class ChatMessage {
    @Exclude
    private String wishId;
    @Exclude
    private String uId;
    private String text;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getWishId() {
        return wishId;
    }

    public void setWishId(String wishId) {
        this.wishId = wishId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "wishId='" + wishId + '\'' +
                ", uId='" + uId + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
