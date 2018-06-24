package aromko.de.wishlist.model;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import aromko.de.wishlist.viewModel.WishViewModel;

public class WishModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private String mParam;


    public WishModelFactory(Application application, String param) {
        mApplication = application;
        mParam = param;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new WishViewModel(mApplication, mParam);
    }
}
