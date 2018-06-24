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
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.tasks.AppExecutors;

public class WishViewModel extends ViewModel {
    private static DatabaseReference wishes_ref;
    private final FirebaseQueryLiveData liveData;
    private final MediatorLiveData<List<Wish>> listsLiveData = new MediatorLiveData<>();

    public WishViewModel() {
        liveData = new FirebaseQueryLiveData(wishes_ref);
    }

    ;

    public WishViewModel(Application mApplication, String wishlistId) {

        wishes_ref = FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId);
        liveData = new FirebaseQueryLiveData(wishes_ref);
        listsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final List<Wish> lists = new ArrayList<>();
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                lists.add(snapshot.getValue(Wish.class));
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
    public LiveData<List<Wish>> getListsLiveData() {
        return listsLiveData;
    }

    public void updateWish(String text) {
        Log.i("uodate", text);
    }

    public void insertWish(String wishlistId) {
        String key = FirebaseDatabase.getInstance().getReference("/wishes").push().getKey();
        Wish wish = new Wish("TESTXXXX", 100.00);
        //Map<String, Object> postValuesinsert = list.toMap();

        //Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put(key, postValues);

        //mDatabase.getReference("/lists").updateChildren(childUpdates);
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + key).setValue(wish);
    }
}
