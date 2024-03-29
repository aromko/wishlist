package aromko.de.wishlist.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import aromko.de.wishlist.database.FirebaseQueryLiveData
import aromko.de.wishlist.model.ChatMessage
import aromko.de.wishlist.model.Wishlist
import aromko.de.wishlist.tasks.AppExecutors
import com.google.firebase.database.*
import java.util.*

class ChatMessageViewModel : ViewModel {
    private val liveData: FirebaseQueryLiveData
    val listsLiveData = MediatorLiveData<List<ChatMessage?>?>()

    constructor() {
        liveData = FirebaseQueryLiveData(lists_ref)
    }

    constructor(mApplication: Application?, wishId: String?) {
        message_record_path = DB_PATH_MESSAGES + wishId
        lists_ref = FirebaseDatabase.getInstance().getReference(message_record_path!!)
        liveData = FirebaseQueryLiveData(lists_ref)
        listsLiveData.addSource(liveData) { dataSnapshot: DataSnapshot? ->
            if (dataSnapshot != null) {
                val lists: MutableList<ChatMessage?> = ArrayList()
                AppExecutors().mainThread().execute {
                    for (snapshot in dataSnapshot.children) {
                        val chatMessage = snapshot.getValue(ChatMessage::class.java)
                        chatMessage?.wishId = wishId
                        lists.add(chatMessage)
                    }
                    listsLiveData.postValue(lists)
                }
            } else {
                listsLiveData.setValue(null)
            }
        }
    }

    fun getListsLiveData(): LiveData<List<ChatMessage?>?> {
        return listsLiveData
    }

    fun insertMessage(
        displayName: String?,
        messageText: String?,
        uid: String?,
        wishName: String?,
        wishListId: String?
    ) {
        FirebaseDatabase.getInstance().getReference("/wishLists/$wishListId")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentWishlist = dataSnapshot.getValue(Wishlist::class.java)
                    val allowedUsers: MutableMap<String?, Any?> = HashMap()
                    if (currentWishlist?.allowedUsers != null) {
                        allowedUsers.putAll(currentWishlist.allowedUsers!!)
                    }
                    val chatMessageId =
                        FirebaseDatabase.getInstance().getReference(message_record_path!!)
                            .push().key
                    val chatMessage =
                        ChatMessage(
                            displayName,
                            messageText,
                            System.currentTimeMillis() / 1000,
                            uid,
                            allowedUsers,
                            wishName,
                            wishListId
                        )
                    FirebaseDatabase.getInstance()
                        .getReference("$message_record_path/$chatMessageId")
                        .setValue(chatMessage)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

    }

    companion object {
        private const val DB_PATH_MESSAGES = "/messages/"
        private var lists_ref: DatabaseReference? = null
        private var message_record_path: String? = null
    }
}