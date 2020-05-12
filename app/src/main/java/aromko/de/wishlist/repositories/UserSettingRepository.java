package aromko.de.wishlist.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import aromko.de.wishlist.model.UserSetting;

public class UserSettingRepository {

    private static final String DB_PATH_SETTINGS = "/settings/";

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public void get(final String userId, final FirebaseCallback firebaseCallback) {
        firebaseDatabase.getReference(DB_PATH_SETTINGS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserSetting userSetting =  new UserSetting("");
                if(dataSnapshot.exists()){
                    userSetting = dataSnapshot.getValue(UserSetting.class);
                }
                firebaseCallback.onCallback(userSetting);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void insert(String userId, String favoriteListId) {
        FirebaseDatabase.getInstance().getReference("/" + DB_PATH_SETTINGS).child(userId).setValue(new UserSetting(favoriteListId));
    }

    public interface FirebaseCallback {
        void onCallback(UserSetting userSetting);
    }

}
