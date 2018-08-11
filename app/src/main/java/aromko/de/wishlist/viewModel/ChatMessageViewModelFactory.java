package aromko.de.wishlist.viewModel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class ChatMessageViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private String wishId;
    private String uId;


    public ChatMessageViewModelFactory(Application application, String wishId, String uId) {
        mApplication = application;
        this.wishId = wishId;
        this.uId = uId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ChatMessageViewModel(mApplication, wishId, uId);
    }
}
