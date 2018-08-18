package aromko.de.wishlist.viewModel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.WishList;
import aromko.de.wishlist.tasks.AppExecutors;

public class WishViewModel extends ViewModel {
    private static DatabaseReference wishes_ref;
    private final FirebaseQueryLiveData liveData;
    private final MediatorLiveData<List<Wish>> listsLiveData = new MediatorLiveData<>();

    public WishViewModel() {
        liveData = new FirebaseQueryLiveData(wishes_ref);
    }

    ;

    public WishViewModel(Application mApplication, final String wishlistId) {

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
                                Wish wish = snapshot.getValue(Wish.class);
                                wish.setWishId(snapshot.getKey().toString());
                                wish.setWishlistId(wishlistId);
                                lists.add(wish);
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

    public void updateWish(String wishlistId, Wish wish) {
    }

    public String insertWish(String wishlistId, Wish wish) {
        String key = FirebaseDatabase.getInstance().getReference("/wishes").push().getKey();
        //Map<String, Object> postValuesinsert = list.toMap();

        //Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put(key, postValues);

        //mDatabase.getReference("/lists").updateChildren(childUpdates);
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + key).setValue(wish);

        FirebaseDatabase.getInstance().getReference("/wishLists/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList currentWishlist = dataSnapshot.getValue(WishList.class);
                int counter = 1;
                counter += currentWishlist.getWishCounter();
                currentWishlist.setWishCounter(counter);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return key;
    }

    public void setWishAsFavorite(String wishlistId, String wishId, Wish wish, String favoriteListId) {
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + wishId).setValue(wish);
        int counter = 0;
        if (wish.getMarkedAsFavorite() != null && wish.getMarkedAsFavorite().get(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(true)) {
            FirebaseDatabase.getInstance().getReference("/wishes/" + favoriteListId + "/" + wishId).setValue(wish);
            counter = 1;
        } else {
            FirebaseDatabase.getInstance().getReference("/wishes/" + favoriteListId + "/" + wishId).removeValue();
            counter = -1;
        }

        final int finalCounter = counter;
        FirebaseDatabase.getInstance().getReference("/wishLists/" + favoriteListId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList wishList = dataSnapshot.getValue(WishList.class);
                wishList.setWishCounter(wishList.getWishCounter() + finalCounter);
                if (wishList.getWishCounter() < 0) {
                    wishList.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void selectWish(String wishlistId, String wishId, final FirebaseCallback firebaseCallback) {
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wish wish = dataSnapshot.getValue(Wish.class);
                firebaseCallback.onCallback(wish);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteWish(String wishId, String wishlistId, final String favoriteListId) {
        final int counter = 1;
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + wishId).removeValue();
        FirebaseDatabase.getInstance().getReference("/wishLists/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList wishList = dataSnapshot.getValue(WishList.class);
                wishList.setWishCounter(wishList.getWishCounter() - counter);
                if (wishList.getWishCounter() < 0) {
                    wishList.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("/wishes/" + favoriteListId + "/" + wishId).removeValue();
        FirebaseDatabase.getInstance().getReference("/wishLists/" + favoriteListId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                WishList wishList = dataSnapshot.getValue(WishList.class);
                wishList.setWishCounter(wishList.getWishCounter() - counter);
                if (wishList.getWishCounter() < 0) {
                    wishList.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updatePhotoUrl(String wishlistId, String wishkey, final Uri uri) {
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + wishkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wish wish = dataSnapshot.getValue(Wish.class);
                wish.setPhotoUrl(uri.toString());
                dataSnapshot.getRef().setValue(wish);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface FirebaseCallback {
        void onCallback(Wish wish);
    }


}
