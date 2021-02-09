package aromko.de.wishlist.model

import com.google.firebase.database.Exclude

class ChatMessage {
    @Exclude
    var wishId: String? = null
    var name: String? = null
    var text: String? = null
    var timestamp: Long = 0

    constructor()
    constructor(name: String?, text: String?, timestamp: Long) {
        this.name = name
        this.text = text
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "ChatMessage{" +
                "wishId='" + wishId + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}'
    }
}