package aromko.de.wishlist.model

import com.google.firebase.database.Exclude

class Payment {
    @Exclude
    var wishId: String? = null
    var price = 0.0
    var salvagePrice = 0.0
    var partialPayments: Map<String?, Double?>? = null

    constructor()
    constructor(price: Double, salvagePrice: Double, partialPayments: Map<String?, Double?>?) {
        this.price = price
        this.salvagePrice = salvagePrice
        this.partialPayments = partialPayments
    }

    override fun toString(): String {
        return "Payment{" +
                " price=" + price +
                ", salvagePrice=" + salvagePrice +
                ", partialPayments=" + partialPayments +
                '}'
    }
}