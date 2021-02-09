package aromko.de.wishlist.database

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebasePersistence : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}