package aromko.de.wishlist.viewModel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.ChatMessage;
import aromko.de.wishlist.tasks.AppExecutors;

public class ChatMessageViewModel extends ViewModel {

    private static DatabaseReference lists_ref;
    private static String message_path;
    private final FirebaseQueryLiveData liveData;
    private final MediatorLiveData<List<ChatMessage>> listsLiveData = new MediatorLiveData<>();

    public ChatMessageViewModel() {
        liveData = new FirebaseQueryLiveData(lists_ref);
    };

    public ChatMessageViewModel(Application mApplication, final String wishId, final String uId) {
        message_path = "/messages/" + wishId + "/" + uId;
        lists_ref = FirebaseDatabase.getInstance().getReference(message_path);
        liveData = new FirebaseQueryLiveData(lists_ref);
        listsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final List<ChatMessage> lists = new ArrayList<>();
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                                chatMessage.setWishId(wishId);
                                chatMessage.setuId(uId);
                                lists.add(chatMessage);
                            }
                            listsLiveData.postValue(lists);
                        }
                    });

                } else {
                    listsLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<List<ChatMessage>> getListsLiveData() {
        return listsLiveData;
    }

    public void insertMessage(String wishId, String uId, String messageText) {
        Log.i("XXXXX", message_path);
        String key = FirebaseDatabase.getInstance().getReference(message_path).push().getKey();
        ChatMessage chatMessage =  new ChatMessage(messageText, System.currentTimeMillis() / 1000);
        FirebaseDatabase.getInstance().getReference(message_path + "/" + key).setValue(chatMessage);
    }
}
