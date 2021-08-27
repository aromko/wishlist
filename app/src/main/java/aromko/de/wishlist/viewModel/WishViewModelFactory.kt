package aromko.de.wishlist.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class WishViewModelFactory(private val mApplication: Application, private val mParam: String?) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WishViewModel(mParam) as T
    }
}