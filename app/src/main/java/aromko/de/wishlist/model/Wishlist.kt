package aromko.de.wishlist.model

import com.google.firebase.database.Exclude

class Wishlist : Comparable<Wishlist> {
    @Exclude
    var key: String? = null
    var name: String? = null
    var timestamp: Long = 0
    var allowedUsers: MutableMap<String?, Any?>? = null
    var wishCounter = 0
    var isFavoriteList = false

    constructor()
    constructor(name: String?, timestamp: Long) {
        this.name = name
        this.timestamp = timestamp
    }

    constructor(name: String?, timestamp: Long, allowedUsers: MutableMap<String?, Any?>?, wishCounter: Int, isFavoriteList: Boolean) {
        this.name = name
        this.timestamp = timestamp
        this.allowedUsers = allowedUsers
        this.wishCounter = wishCounter
        this.isFavoriteList = isFavoriteList
    }

    override fun toString(): String {
        return "Wishlist{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", allowedUsers=" + allowedUsers +
                ", wishCounter=" + wishCounter +
                ", isFavoriteList=" + isFavoriteList +
                '}'
    }

    override fun compareTo(wishlist: Wishlist): Int {
        return java.lang.Long.compare(timestamp, wishlist.timestamp)
    }
}