package aromko.de.wishlist.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment

class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isWizardMode = true
        setProgressIndicator()
        setImmersiveMode()
        addSlide(AppIntroFragment.newInstance("Welcome...", "This is the first slide of the example"))
        addSlide(AppIntroFragment.newInstance("...Let's get started!", "This is the last slide, I won't annoy you more :)"))
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        finish()
    }
}