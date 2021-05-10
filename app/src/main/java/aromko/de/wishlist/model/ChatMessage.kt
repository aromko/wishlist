package aromko.de.wishlist.model

import com.google.firebase.database.Exclude

class ChatMessage {
    @Exclude
    var wishId: String? = null
    var name: String? = null
    var text: String? = null
    var timestamp: Long = 0
    var userId: String? = null
    var allowedUsers: MutableMap<String?, Any?>? = null
    var wishName: String? = null
    var wishListId: String? = null

    constructor()
    constructor(
        name: String?,
        text: String?,
        timestamp: Long,
        userId: String?,
        allowedUsers: MutableMap<String?, Any?>?,
        wishName: String?,
        wishListId: String?
    ) {
        this.name = name
        this.text = text
        this.timestamp = timestamp
        this.userId = userId
        this.allowedUsers = allowedUsers
        this.wishName = wishName
        this.wishListId = wishListId
    }

    override fun toString(): String {
        return "ChatMessage(wishId=$wishId, name=$name, text=$text, timestamp=$timestamp, userId=$userId, allowedUsers=$allowedUsers, wishName=$wishName, wishListId=$wishListId)"
    }
}