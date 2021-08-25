package aromko.de.wishlist.viewModel

import aromko.de.wishlist.model.Payment
import aromko.de.wishlist.model.UserSetting
import aromko.de.wishlist.model.Wish
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PaymentViewModel {
    private val fFirebaseUser = FirebaseAuth.getInstance().currentUser
    fun buyItem(
        wishId: String?,
        price: Double,
        partialPrice: Double,
        wishlistId: String?,
        markedAsFavorite: Map<String?, Boolean?>?
    ) {
        FirebaseDatabase.getInstance().getReference("${DB_PATH_PAYMENTS}$wishId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val partialPayments: MutableMap<String?, Double?> = HashMap()
                    var salvagePrice: Double
                    var salvagePriceUser = 0.0
                    if (dataSnapshot.exists()) {
                        val currentPayment = dataSnapshot.getValue(Payment::class.java)
                        if (currentPayment?.partialPayments != null) {
                            partialPayments.putAll(currentPayment.partialPayments!!)
                        }
                        if (partialPayments.containsKey(fFirebaseUser!!.uid)) {
                            salvagePriceUser = partialPrice
                        }
                        salvagePrice = partialPrice
                        if (salvagePrice <= 0.00) {
                            salvagePrice = 0.00
                        }
                        partialPayments[fFirebaseUser.uid] = salvagePriceUser
                        currentPayment?.salvagePrice = salvagePrice
                        currentPayment?.partialPayments = partialPayments
                        dataSnapshot.ref.setValue(currentPayment)
                    } else {
                        salvagePrice = partialPrice
                        if (salvagePrice <= 0.00) {
                            salvagePrice = 0.00
                        }
                        partialPayments[fFirebaseUser!!.uid] = partialPrice
                        val payment = Payment(price, salvagePrice, partialPayments)
                        dataSnapshot.ref.setValue(payment)
                    }
                    updateSalvagePriceInWish(salvagePrice, wishId, wishlistId)

                    if (markedAsFavorite != null) {
                        for ((userId) in markedAsFavorite) {
                            getFavoriteListIdFromUserId(userId) { favoriteListId: String? ->
                                updateSalvagePriceInWish(salvagePrice, wishId, favoriteListId)
                            }
                        }
                    }

                }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun deletePayment(wishId: String?, wishlistId: String?, markedAsFavorite: Map<String?, Boolean?>?) {
        FirebaseDatabase.getInstance().getReference("${DB_PATH_PAYMENTS}$wishId").removeValue()

        val salvagePrice = 0.0
        updateSalvagePriceInWish(salvagePrice, wishId, wishlistId)
        if (markedAsFavorite != null) {
            for ((userId) in markedAsFavorite) {
                getFavoriteListIdFromUserId(userId) { favoriteListId: String? ->
                    updateSalvagePriceInWish(salvagePrice, wishId, favoriteListId)
                }
            }
        }
    }

    fun getPayment(wishId: String?, firebaseCallback: (Payment?) -> Unit) {
        FirebaseDatabase.getInstance().getReference("${DB_PATH_PAYMENTS}$wishId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val payment = dataSnapshot.getValue(Payment::class.java)
                    firebaseCallback.invoke(payment)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getFavoriteListIdFromUserId(
        userId: String?,
        firebaseCallback: (String?) -> Unit
    ) {
        FirebaseDatabase.getInstance().getReference("${DB_PATH_SETTINGS}$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userSetting = dataSnapshot.getValue(UserSetting::class.java)
                    firebaseCallback.invoke(userSetting!!.favoriteListId)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun updateSalvagePriceInWish(salvagePrice: Double, wishId: String?, wishlistId: String?) {
        FirebaseDatabase.getInstance().getReference("$DB_PATH_WISHES$wishlistId/$wishId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val currentWish = dataSnapshot.getValue(Wish::class.java)
                    currentWish?.salvagePrice = salvagePrice
                    dataSnapshot.ref.setValue(currentWish)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    interface FirebaseCallback {
        fun onCallback(payment: Payment?)
    }

    companion object {
        private const val DB_PATH_PAYMENTS = "/payments/"
        private const val DB_PATH_WISHES = "/wishes/"
        private const val DB_PATH_SETTINGS = "/settings/"
    }
}