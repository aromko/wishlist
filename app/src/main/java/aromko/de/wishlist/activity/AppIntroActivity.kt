package aromko.de.wishlist.activity

import android.Manifest
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import aromko.de.wishlist.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPagerBuilder
import com.google.firebase.auth.FirebaseAuth

class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isWizardMode = true
        setProgressIndicator()
        showIntroSlides()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        val fFirebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = getSharedPreferences(fFirebaseAuth.currentUser!!.uid, MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("showAppIntro", false).commit()
        finish()
    }

    private fun showIntroSlides() {

        val backgroundColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        val pageOne = SliderPagerBuilder()
                .title(getString(R.string.appIntroWelcome))
                .description(getString(R.string.appIntroWelceomDesc))
                .imageDrawable(R.drawable.ic_launcher)
                .backgroundColor(backgroundColor)
                .build()

        val pageTwo = SliderPagerBuilder()
                .description(getString(R.string.appIntroFavoriteDesc))
                .imageDrawable(R.drawable.ic_favorite_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageThree = SliderPagerBuilder()
                .description(getString(R.string.appIntroShareDesc))
                .imageDrawable(R.drawable.ic_person_add_white_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageFour = SliderPagerBuilder()
                .title(getString(R.string.appIntroPermission))
                .description(getString(R.string.appIntroPermissionDesc))
                .imageDrawable(R.drawable.ic_permission_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageFive = SliderPagerBuilder()
                .description(getString(R.string.appIntroNavigationDesc))
                .imageDrawable(R.drawable.ic_map_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageSix = SliderPagerBuilder()
                .description(getString(R.string.appIntroPhotoDesc))
                .imageDrawable(R.drawable.ic_add_photo_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageSeven = SliderPagerBuilder()
                .title(getString(R.string.appIntroEnding))
                .imageDrawable(R.drawable.ic_launcher)
                .backgroundColor(backgroundColor)
                .build()


        addSlide(AppIntroFragment.newInstance(pageOne))
        addSlide(AppIntroFragment.newInstance(pageTwo))
        addSlide(AppIntroFragment.newInstance(pageThree))
        addSlide(AppIntroFragment.newInstance(pageFour))
        addSlide(AppIntroFragment.newInstance(pageFive))
        addSlide(AppIntroFragment.newInstance(pageSix))
        addSlide(AppIntroFragment.newInstance(pageSeven))

        askForPermissions(
                permissions = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                slideNumber = 5,
                required = false)

        askForPermissions(
                permissions = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                slideNumber = 6,
                required = false)
    }
}