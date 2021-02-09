package aromko.de.wishlist.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class ChatMessageViewModelFactory(private val mApplication: Application, private val wishId: String?) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatMessageViewModel(mApplication, wishId) as T
    }
}