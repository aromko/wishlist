package aromko.de.wishlist.model

class UserSetting {
    var favoriteListId: String? = null

    constructor()
    constructor(favoriteListId: String?) {
        this.favoriteListId = favoriteListId
    }

    override fun toString(): String {
        return "UserSettings{" +
                "favoriteListId='" + favoriteListId + '\'' +
                '}'
    }
}