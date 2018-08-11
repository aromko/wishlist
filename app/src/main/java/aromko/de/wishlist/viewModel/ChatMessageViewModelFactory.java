package aromko.de.wishlist.viewModel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class ChatMessageViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private String wishId;

    public ChatMessageViewModelFactory(Application application, String wishId) {
        mApplication = application;
        this.wishId = wishId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ChatMessageViewModel(mApplication, wishId);
    }
}
