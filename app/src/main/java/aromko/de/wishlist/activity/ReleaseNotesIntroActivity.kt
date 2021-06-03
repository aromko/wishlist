package aromko.de.wishlist.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import aromko.de.wishlist.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.google.firebase.auth.FirebaseAuth

class ReleaseNotesIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_release_notes))

    }

    override fun onDonePressed(currentFragment: Fragment?) {
        val fFirebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = getSharedPreferences(fFirebaseAuth.currentUser!!.uid, MODE_PRIVATE)
        sharedPreferences.edit().putString("showReleaseNotesIntro", getString(R.string.app_version))
            .commit()
        finish()
    }
}