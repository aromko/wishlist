package aromko.de.wishlist.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import aromko.de.wishlist.model.WishList;
import aromko.de.wishlist.tasks.AppExecutors;

public class WishListViewModel extends ViewModel {

    private static final DatabaseReference LISTS_REF =
            FirebaseDatabase.getInstance().getReference("/wishLists");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(LISTS_REF);
    private final MediatorLiveData<List<WishList>> listsLiveData = new MediatorLiveData<>();

    public WishListViewModel() {
        listsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final List<WishList> lists = new ArrayList<>();
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                WishList wishList = snapshot.getValue(WishList.class);
                                wishList.setKey(snapshot.getKey().toString());
                                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                if (wishList.getAllowedUsers() != null && wishList.getAllowedUsers().containsKey(currentUid) && wishList.getAllowedUsers().get(currentUid).equals(true)) {
                                    lists.add(wishList);
                                }
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

    public String insertList(String text, boolean isFavortieList) {
        String key = FirebaseDatabase.getInstance().getReference("/wishLists").push().getKey();
        Map<String, Object> allowedUser = new HashMap<>();
        allowedUser.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
        WishList wishList = new WishList(text, System.currentTimeMillis() / 1000, allowedUser, 0, isFavortieList);
        //Map<String, Object> postValues = list.toMap();

        //Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put(key, postValues);

        //mDatabase.getReference("/lists").updateChildren(childUpdates);
        FirebaseDatabase.getInstance().getReference("/wishLists/" + key).setValue(wishList);
        return key;
    }

    @NonNull
    public LiveData<List<WishList>> getListsLiveData() {
        return listsLiveData;
    }

    public void addUserToWishlist(String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/wishLists/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList currentWishlist = dataSnapshot.getValue(WishList.class);
                Map<String, Object> allowedUsers = new HashMap<>();
                if (currentWishlist.getAllowedUsers() != null) {
                    allowedUsers.putAll(currentWishlist.getAllowedUsers());
                }
                allowedUsers.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
                currentWishlist.setAllowedUsers(allowedUsers);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateList(String wishlistId, final String name) {
        FirebaseDatabase.getInstance().getReference("/wishLists/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList currentWishlist = dataSnapshot.getValue(WishList.class);
                currentWishlist.setName(name);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteList(String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/wishLists/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList currentWishlist = dataSnapshot.getValue(WishList.class);
                currentWishlist.getAllowedUsers().remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
