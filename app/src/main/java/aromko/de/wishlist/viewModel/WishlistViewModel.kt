package aromko.de.wishlist.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import aromko.de.wishlist.database.FirebaseQueryLiveData
import aromko.de.wishlist.model.Wishlist
import aromko.de.wishlist.tasks.AppExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class WishlistViewModel : ViewModel() {
    private val liveData = FirebaseQueryLiveData(LISTS_REF)
    val listsLiveData = MediatorLiveData<List<Wishlist?>?>()
    private val fFirebaseUser = FirebaseAuth.getInstance().currentUser
    fun insertList(text: String?, isFavortieList: Boolean): String? {
        val wishlistId = FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS").push().key
        val allowedUser: MutableMap<String?, Any?> = HashMap()
        allowedUser[fFirebaseUser!!.uid] = true
        val wishlist = Wishlist(text, System.currentTimeMillis() / 1000, allowedUser, 0, isFavortieList)
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId").setValue(wishlist)
        return wishlistId
    }

    fun getListsLiveData(): LiveData<List<Wishlist?>?> {
        return listsLiveData
    }

    fun addUserToWishlist(wishlistId: String?) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentWishlist = dataSnapshot.getValue(Wishlist::class.java)
                val allowedUsers: MutableMap<String?, Any?> = HashMap()
                if (currentWishlist?.allowedUsers != null) {
                    allowedUsers.putAll(currentWishlist.allowedUsers!!)
                }
                allowedUsers[fFirebaseUser!!.uid] = true
                currentWishlist?.allowedUsers = allowedUsers
                dataSnapshot.ref.setValue(currentWishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateList(wishlistId: String?, name: String?) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentWishlist = dataSnapshot.getValue(Wishlist::class.java)
                currentWishlist?.name = name
                dataSnapshot.ref.setValue(currentWishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun deleteList(wishlistId: String?) {
        FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS/$wishlistId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentWishlist = dataSnapshot.getValue(Wishlist::class.java)
                currentWishlist?.allowedUsers?.remove(fFirebaseUser!!.uid)
                dataSnapshot.ref.setValue(currentWishlist)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object {
        private const val DB_PATH_WISHLISTS = "wishLists"
        private val LISTS_REF = FirebaseDatabase.getInstance().getReference("/$DB_PATH_WISHLISTS")
    }

    init {
        listsLiveData.addSource(liveData) { dataSnapshot: DataSnapshot? ->
            if (dataSnapshot != null) {
                val lists: MutableList<Wishlist?> = ArrayList()
                AppExecutors().mainThread().execute {
                    for (snapshot in dataSnapshot.children) {
                        val wishlist = snapshot.getValue(Wishlist::class.java)
                        wishlist?.key = snapshot.key
                        val currentUid = fFirebaseUser!!.uid
                        if (wishlist?.allowedUsers != null && wishlist.allowedUsers!!.containsKey(currentUid) && wishlist.allowedUsers!![currentUid] == true) {
                            lists.add(wishlist)
                        }
                    }
                    listsLiveData.postValue(lists)
                }
            } else {
                listsLiveData.setValue(null)
            }
        }
    }
}