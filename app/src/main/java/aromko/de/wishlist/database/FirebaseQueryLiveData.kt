package aromko.de.wishlist.database

import android.os.Handler
import androidx.lifecycle.LiveData
import com.google.firebase.database.*

class FirebaseQueryLiveData : LiveData<DataSnapshot?> {
    private var query: Query? = null
    private val listener = MyValueEventListener()
    private val handler = Handler()
    private var listenerRemovePending = false
    private val removeListener = Runnable {
        query!!.removeEventListener(listener)
        listenerRemovePending = false
    }

    constructor(query: Query?) {
        this.query = query
    }

    constructor(ref: DatabaseReference?) {
        query = ref
    }

    override fun onActive() {
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener)
        } else {
            query!!.addValueEventListener(listener)
        }
        listenerRemovePending = false
    }

    override fun onInactive() {
        handler.postDelayed(removeListener, 2000)
        listenerRemovePending = true
    }

    private inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }
}