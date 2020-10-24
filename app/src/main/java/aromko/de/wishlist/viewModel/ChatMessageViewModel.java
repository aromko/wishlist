package aromko.de.wishlist.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.ChatMessage;
import aromko.de.wishlist.tasks.AppExecutors;

public class ChatMessageViewModel extends ViewModel {

    private static final String DB_PATH_MESSAGES = "/messages/";
    private static DatabaseReference lists_ref;
    private static String message_record_path;
    private final FirebaseQueryLiveData liveData;
    private final MediatorLiveData<List<ChatMessage>> listsLiveData = new MediatorLiveData<>();

    public ChatMessageViewModel() {
        liveData = new FirebaseQueryLiveData(lists_ref);
    }

    public ChatMessageViewModel(Application mApplication, final String wishId) {
        message_record_path = DB_PATH_MESSAGES + wishId;
        lists_ref = FirebaseDatabase.getInstance().getReference(message_record_path);
        liveData = new FirebaseQueryLiveData(lists_ref);
        listsLiveData.addSource(liveData, dataSnapshot -> {
            if (dataSnapshot != null) {
                final List<ChatMessage> lists = new ArrayList<>();
                new AppExecutors().mainThread().execute(() -> {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                        chatMessage.setWishId(wishId);
                        lists.add(chatMessage);
                    }
                    listsLiveData.postValue(lists);
                });

            } else {
                listsLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<List<ChatMessage>> getListsLiveData() {
        return listsLiveData;
    }

    public void insertMessage(String displayName, String messageText) {
        String chatMessageId = FirebaseDatabase.getInstance().getReference(message_record_path).push().getKey();
        ChatMessage chatMessage = new ChatMessage(displayName, messageText, System.currentTimeMillis() / 1000);
        FirebaseDatabase.getInstance().getReference(message_record_path + "/" + chatMessageId).setValue(chatMessage);
    }
}
