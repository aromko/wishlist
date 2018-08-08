package aromko.de.wishlist.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.ChatMessage;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.WishList;
import aromko.de.wishlist.tasks.AppExecutors;

public class ChatMessageViewModel extends ViewModel {

    private static final DatabaseReference LISTS_REF =
            FirebaseDatabase.getInstance().getReference("/messages");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(LISTS_REF);
    private final MediatorLiveData<List<ChatMessage>> listsLiveData = new MediatorLiveData<>();

    public ChatMessageViewModel() {
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
}
