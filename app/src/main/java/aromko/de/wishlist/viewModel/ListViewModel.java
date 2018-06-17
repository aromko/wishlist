package aromko.de.wishlist.viewModel;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.Lists;
import aromko.de.wishlist.tasks.AppExecutors;

public class ListViewModel extends ViewModel {

    private static final DatabaseReference LISTS_REF =
            FirebaseDatabase.getInstance().getReference("/lists");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(LISTS_REF);
    private final MediatorLiveData<List<Lists>> listsLiveData = new MediatorLiveData<>();

    public ListViewModel() {
        listsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final List<Lists> lists = new ArrayList<>();
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                lists.add(snapshot.getValue(Lists.class));
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

    public void insertList(){
        String key = FirebaseDatabase.getInstance().getReference("/lists").push().getKey();
        Lists list = new Lists(4, 6, "Freiburg");
        //Map<String, Object> postValues = list.toMap();

        //Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put(key, postValues);

        //mDatabase.getReference("/lists").updateChildren(childUpdates);
        FirebaseDatabase.getInstance().getReference("/lists/" + key).setValue(list);
    }

    @NonNull
    public LiveData<List<Lists>> getListsLiveData() {
        return listsLiveData;
    }
}
