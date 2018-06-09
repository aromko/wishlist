package aromko.de.wishlist.viewModel;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import aromko.de.wishlist.database.FirebaseQueryLiveData;
import aromko.de.wishlist.model.HotStock;
import aromko.de.wishlist.tasks.AppExecutors;

public class HotStockViewModel extends ViewModel {
    private static final DatabaseReference HOT_STOCK_REF =
            FirebaseDatabase.getInstance().getReference("/hotstock");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(HOT_STOCK_REF);
    private final MediatorLiveData<HotStock> hotStockLiveData = new MediatorLiveData<>();

    public HotStockViewModel() {
        hotStockLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    new AppExecutors().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            hotStockLiveData.postValue(dataSnapshot.getValue(HotStock.class));
                        }
                    });
                } else {
                    hotStockLiveData.setValue(null);
                }
            }
        });
    }

    private class Deserializer implements Function<DataSnapshot, HotStock> {
        @Override
        public HotStock apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(HotStock.class);
        }
    }

    @NonNull
    public LiveData<HotStock> getHotStockLiveData() {
        return hotStockLiveData;
    }
}
