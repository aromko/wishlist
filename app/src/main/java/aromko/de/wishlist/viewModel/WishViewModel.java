package aromko.de.wishlist.viewModel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.Wishlist;
import aromko.de.wishlist.tasks.AppExecutors;

public class WishViewModel extends ViewModel {
    private static final String DB_PATH_WISHES = "wishes";
    private static final String DB_PATH_WISHLISTS = "wishLists";
    private static DatabaseReference wishes_ref;
    private final FirebaseQueryLiveData liveData;
    private final MediatorLiveData<List<Wish>> listsLiveData = new MediatorLiveData<>();

    public WishViewModel() {
        liveData = new FirebaseQueryLiveData(wishes_ref);
    }

    public WishViewModel(Application mApplication, final String wishlistId) {

        wishes_ref = FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId);
        liveData = new FirebaseQueryLiveData(wishes_ref.orderByChild("timestamp"));
        listsLiveData.addSource(liveData, dataSnapshot -> {
            if (dataSnapshot != null) {
                final List<Wish> lists = new ArrayList<>();
                new AppExecutors().mainThread().execute(() -> {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Wish wish = snapshot.getValue(Wish.class);
                        wish.setWishId(snapshot.getKey());
                        wish.setWishlistId(wishlistId);
                        lists.add(wish);
                        Collections.sort(lists);
                    }
                    listsLiveData.postValue(lists);
                });

            } else {
                listsLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<List<Wish>> getListsLiveData() {
        return listsLiveData;
    }

    public void updateWish(String wishlistId, String wishId, final Wish wish) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wish savedWish = dataSnapshot.getValue(Wish.class);
                wish.setMarkedAsFavorite(savedWish.getMarkedAsFavorite());
                dataSnapshot.getRef().setValue(wish);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String insertWish(String wishlistId, Wish wish) {
        String wishId = FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES).push().getKey();

        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishId).setValue(wish);

        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist currentWishlist = dataSnapshot.getValue(Wishlist.class);
                int counter = 1;
                counter += currentWishlist.getWishCounter();
                currentWishlist.setWishCounter(counter);
                dataSnapshot.getRef().setValue(currentWishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return wishId;
    }

    public void setWishAsFavorite(String wishlistId, String wishId, Wish wish, String favoriteListId) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishId).setValue(wish);
        int counter;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + favoriteListId + "/" + wishId);
        if (wish.getMarkedAsFavorite() != null && wish.getMarkedAsFavorite().get(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(true)) {
            databaseReference.setValue(wish);
            counter = 1;
        } else {
            databaseReference.removeValue();
            counter = -1;
        }

        final int finalCounter = counter;
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + favoriteListId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist wishlist = dataSnapshot.getValue(Wishlist.class);
                wishlist.setWishCounter(wishlist.getWishCounter() + finalCounter);
                if (wishlist.getWishCounter() < 0) {
                    wishlist.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void selectWish(String wishlistId, String wishId, final FirebaseCallback firebaseCallback) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishId).removeValue();
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + wishlistId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist wishlist = dataSnapshot.getValue(Wishlist.class);
                wishlist.setWishCounter(wishlist.getWishCounter() - counter);
                if (wishlist.getWishCounter() < 0) {
                    wishlist.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + favoriteListId + "/" + wishId).removeValue();
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHLISTS + "/" + favoriteListId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wishlist wishlist = dataSnapshot.getValue(Wishlist.class);
                wishlist.setWishCounter(wishlist.getWishCounter() - counter);
                if (wishlist.getWishCounter() < 0) {
                    wishlist.setWishCounter(0);
                }
                dataSnapshot.getRef().setValue(wishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deleteImageFromFirebaseStorageByWishId(wishId);
    }

    public void deleteImageFromFirebaseStorageByWishId(String wishId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference desertRef = storageRef.child(wishId);
        desertRef.delete().addOnSuccessListener(aVoid -> {
            Log.i(this.getClass().getName(), String.format("File with wishId %s deleted successfully", wishId));
        }).addOnFailureListener(exception -> Log.e(this.getClass().getName(), String.format("File with wishId %s not deleted. More infos: %s", wishId, exception.getMessage())));
    }

    public void updatePhotoUrl(String wishlistId, String wishkey, final Uri uri) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_WISHES + "/" + wishlistId + "/" + wishkey).addListenerForSingleValueEvent(new ValueEventListener() {
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
