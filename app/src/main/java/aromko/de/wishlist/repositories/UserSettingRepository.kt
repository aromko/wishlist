package aromko.de.wishlist.repositories

import aromko.de.wishlist.model.UserSetting
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserSettingRepository {
    var firebaseDatabase = FirebaseDatabase.getInstance()
    operator fun get(userId: String?, firebaseCallback: (aromko.de.wishlist.model.UserSetting) -> kotlin.Unit) {
        firebaseDatabase.getReference(DB_PATH_SETTINGS).child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userSetting: UserSetting? = UserSetting("")
                if (dataSnapshot.exists()) {
                    userSetting = dataSnapshot.getValue(UserSetting::class.java)
                }
                firebaseCallback.invoke(userSetting!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun insert(userId: String?, favoriteListId: String?) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_SETTINGS).child(userId!!).setValue(UserSetting(favoriteListId))
    }

    interface FirebaseCallback {
        fun onCallback(userSetting: UserSetting?)
    }

    companion object {
        private const val DB_PATH_SETTINGS = "/settings/"
    }
}