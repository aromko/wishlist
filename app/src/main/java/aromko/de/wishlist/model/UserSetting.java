package aromko.de.wishlist.model;

public class UserSetting {

    private String favoriteListId;

    public UserSetting() {
    }

    public UserSetting(String favoriteListId) {
        this.favoriteListId = favoriteListId;
    }

    public String getFavoriteListId() {
        return favoriteListId;
    }

    public void setFavoriteListId(String favoriteListId) {
        this.favoriteListId = favoriteListId;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "favoriteListId='" + favoriteListId + '\'' +
                '}';
    }
}
