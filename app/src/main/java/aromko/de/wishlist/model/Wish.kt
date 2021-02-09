package aromko.de.wishlist.model

import com.google.firebase.database.Exclude

class Wish : Comparable<Wish> {
    @get:Exclude
    @set:Exclude
    @Exclude
    var wishId: String? = null

    @get:Exclude
    @set:Exclude
    @Exclude
    var wishlistId: String? = null
    var title: String? = null
    var price = 0.0
    var url: String? = null
    var description: String? = null
    var wishstrength: Long = 0
    var isImageSet = false
    var markedAsFavorite: Map<String?, Boolean?>? = null
    var timestamp: Long = 0
    var longitude = 0.0
    var latitude = 0.0
    var salvagePrice = 0.0
    var placeId: String? = null
    var photoUrl: String? = null

    constructor()
    constructor(title: String?, price: Double, url: String?, description: String?, wishstrength: Long, isImageSet: Boolean, timestamp: Long, longitude: Double, latitude: Double, salvagePrice: Double, placeId: String?, photoUrl: String?) {
        this.title = title
        this.price = price
        this.url = url
        this.description = description
        this.wishstrength = wishstrength
        this.isImageSet = isImageSet
        this.timestamp = timestamp
        this.longitude = longitude
        this.latitude = latitude
        this.salvagePrice = salvagePrice
        this.placeId = placeId
        this.photoUrl = photoUrl
    }

    override fun toString(): String {
        return "Wish{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", wishstrength=" + wishstrength +
                ", isImageSet=" + isImageSet +
                ", markedAsFavorite=" + markedAsFavorite +
                ", timestamp=" + timestamp +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", salvagePrice=" + salvagePrice +
                ", placeId=" + placeId +
                ", photoUrl=" + photoUrl +
                '}'
    }

    override fun compareTo(wish: Wish): Int {
        return java.lang.Long.compare(wish.timestamp, timestamp)
    }
}