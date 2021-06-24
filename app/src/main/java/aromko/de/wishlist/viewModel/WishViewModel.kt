package aromko.de.wishlist.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import aromko.de.wishlist.database.FirebaseQueryLiveData
import aromko.de.wishlist.model.UserSetting
import aromko.de.wishlist.model.Wish
import aromko.de.wishlist.model.Wishlist
import aromko.de.wishlist.tasks.AppExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class WishViewModel : ViewModel {
    private val liveData: FirebaseQueryLiveData
    val listsLiveData = MediatorLiveData<List<Wish?>?>()

    constructor() {
        liveData = FirebaseQueryLiveData(wishes_ref)
    }

    constructor(mApplication: Application?, wishlistId: String?) {
        wishes_ref = FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId")
        liveData = FirebaseQueryLiveData(wishes_ref!!.orderByChild("timestamp"))
        listsLiveData.addSource(liveData) { dataSnapshot: DataSnapshot? ->
            if (dataSnapshot != null) {
                val lists:  ArrayList<Wish> = ArrayList()
                AppExecutors().mainThread().execute {
                    for (snapshot in dataSnapshot.children) {
                        val wish = snapshot.getValue(Wish::class.java)
                        wish?.wishId = snapshot.key
                        wish?.wishlistId = wishlistId
                        lists.add(wish!!)
                        lists.sort()
                    }
                    listsLiveData.postValue(lists)
                }
            } else {
                listsLiveData.setValue(null)
            }
        }
    }

    fun getListsLiveData(): LiveData<List<Wish?>?> {
        return listsLiveData
    }

    fun updateWish(wishlistId: String?, wishId: String?, wish: Wish, favoriteListId: String?) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedWish = dataSnapshot.getValue(Wish::class.java)
                    wish.markedAsFavorite = savedWish?.markedAsFavorite
                    dataSnapshot.ref.setValue(wish)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    fun updateFavoriteWishlist(
        favoriteListId: String?,
        wishId: String?,
        wish: Wish
    ) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$favoriteListId/$wishId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedWish = dataSnapshot.getValue(Wish::class.java)
                    if (savedWish != null) {
                        wish.markedAsFavorite = savedWish.markedAsFavorite
                        dataSnapshot.ref.setValue(wish)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    fun insertWish(wishlistId: String?, wish: Wish?): String? {
        val wishId = FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES").push().key
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishId")
            .setValue(wish)
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentWishlist = dataSnapshot.getValue(Wishlist::class.java)
                    var counter = 1
                    counter += currentWishlist?.wishCounter!!
                    currentWishlist.wishCounter = counter
                    dataSnapshot.ref.setValue(currentWishlist)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        return wishId
    }

    fun setWishAsFavorite(wishlistId: String?, wishId: String?, wish: Wish?, favoriteListId: String?) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishId").setValue(wish)
        val counter: Int
        val databaseReference = FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$favoriteListId/$wishId")
        counter = if (wish?.markedAsFavorite != null && wish.markedAsFavorite!![FirebaseAuth.getInstance().currentUser!!.uid] == true) {
            databaseReference.setValue(wish)
            1
        } else {
            databaseReference.removeValue()
            -1
        }
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$favoriteListId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wishlist = dataSnapshot.getValue(Wishlist::class.java)
                wishlist?.wishCounter = wishlist?.wishCounter!! + counter
                if (wishlist.wishCounter < 0) {
                    wishlist.wishCounter = 0
                }
                dataSnapshot.ref.setValue(wishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun selectWish(wishlistId: String?, wishId: String?, firebaseCallback: (Wish) -> Unit) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wish = dataSnapshot.getValue(Wish::class.java)
                firebaseCallback.invoke(wish!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun deleteWish(wishId: String?, wishlistId: String?, favoriteListId: String?) {
        val counter = 1
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishId").removeValue()
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wishlist = dataSnapshot.getValue(Wishlist::class.java)
                wishlist?.wishCounter = wishlist?.wishCounter!! - counter
                if (wishlist.wishCounter < 0) {
                    wishlist.wishCounter = 0
                }
                dataSnapshot.ref.setValue(wishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$favoriteListId/$wishId").removeValue()
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$favoriteListId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wishlist = dataSnapshot.getValue(Wishlist::class.java)
                wishlist?.wishCounter = wishlist?.wishCounter!! - counter
                if (wishlist.wishCounter < 0) {
                    wishlist.wishCounter = 0
                }
                dataSnapshot.ref.setValue(wishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        deleteImageFromFirebaseStorageByWishId(wishId)
    }

    fun deleteImageFromFirebaseStorageByWishId(wishId: String?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val desertRef = storageRef.child(wishId!!)
        desertRef.delete().addOnSuccessListener { aVoid: Void? -> Log.i(this.javaClass.name, String.format("File with wishId %s deleted successfully", wishId)) }.addOnFailureListener { exception: Exception -> Log.e(this.javaClass.name, String.format("File with wishId %s not deleted. More infos: %s", wishId, exception.message)) }
    }

    fun updatePhotoUrl(wishlistId: String, wishkey: String, uri: Uri) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHES/$wishlistId/$wishkey").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wish = dataSnapshot.getValue(Wish::class.java)
                wish?.photoUrl = uri.toString()
                dataSnapshot.ref.setValue(wish)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun getAllFavoriteWishlistsIdsFromMarkedAsFavoriteUsers(
        userId: String?,
        firebaseCallback: (UserSetting) -> Unit
    ) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_SETTINGS/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userSetting = dataSnapshot.getValue(UserSetting::class.java)
                    firebaseCallback.invoke(userSetting!!)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    interface FirebaseCallback {
        fun onCallback(wish: Wish?)
    }

    companion object {
        private const val DB_PATH_WISHES = "wishes"
        private const val DB_PATH_WISHLISTS = "wishLists"
        private var wishes_ref: DatabaseReference? = null
        private const val DB_PATH_SETTINGS = "settings"
    }
}