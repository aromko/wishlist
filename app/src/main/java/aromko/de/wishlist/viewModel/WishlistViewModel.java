package aromko.de.wishlist.viewModel;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.Wishlist;
import aromko.de.wishlist.tasks.AppExecutors;

public class WishlistViewModel extends ViewModel {

    private static final String DB_PATH_WISHLISTS = "wishLists";
    private static final DatabaseReference LISTS_REF = FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS);

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(LISTS_REF);
    private final MediatorLiveData<List<Wishlist>> listsLiveData = new MediatorLiveData<>();

    private FirebaseUser fFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public WishlistViewModel() {
        listsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final List<Wishlist> lists = new ArrayList<>();
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Wishlist wishlist = snapshot.getValue(Wishlist.class);
                                wishlist.setKey(snapshot.getKey().toString());
                                String currentUid = fFirebaseUser.getUid();
                                if (wishlist.getAllowedUsers() != null && wishlist.getAllowedUsers().containsKey(currentUid) && wishlist.getAllowedUsers().get(currentUid).equals(true)) {
                                    lists.add(wishlist);
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
        String wishlistId = FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS).push().getKey();
        Map<String, Object> allowedUser = new HashMap<>();
        allowedUser.put(fFirebaseUser.getUid(), true);
        Wishlist wishlist = new Wishlist(text, System.currentTimeMillis() / 1000, allowedUser, 0, isFavortieList);
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).setValue(wishlist);
        return wishlistId;
    }

    @NonNull
    public LiveData<List<Wishlist>> getListsLiveData() {
        return listsLiveData;
    }

    public void addUserToWishlist(String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist currentWishlist = dataSnapshot.getValue(Wishlist.class);
                Map<String, Object> allowedUsers = new HashMap<>();
                if (currentWishlist.getAllowedUsers() != null) {
                    allowedUsers.putAll(currentWishlist.getAllowedUsers());
                }
                allowedUsers.put(fFirebaseUser.getUid(), true);
                currentWishlist.setAllowedUsers(allowedUsers);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateList(String wishlistId, final String name) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist currentWishlist = dataSnapshot.getValue(Wishlist.class);
                currentWishlist.setName(name);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteList(String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist currentWishlist = dataSnapshot.getValue(Wishlist.class);
                currentWishlist.getAllowedUsers().remove(fFirebaseUser.getUid());
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
