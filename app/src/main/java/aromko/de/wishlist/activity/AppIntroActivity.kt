package aromko.de.wishlist.activity

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
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
                .title("Herzlich Willkommen zur Wishlist")
                .description("Nachfolgend werden dir ein paar der wichtigsten Funktionen vorgestellt.")
                .imageDrawable(R.drawable.ic_launcher)
                .backgroundColor(backgroundColor)
                .build()

        val pageTwo = SliderPagerBuilder()
                .description("Damit du nie deine wichtigsten Wünsche vergisst kannst du diese über das Herzymbol als Favorit kennzeichnen. Diese landen dann automatisch in deiner Favoritenliste.")
                .imageDrawable(R.drawable.ic_favorite_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageThree = SliderPagerBuilder()
                .description("Teile deine Wünsche mit deinen Freunden. Schicke deinen Freunden einen Link und schon können sie deine Liste mitbenutzen.")
                .imageDrawable(R.drawable.ic_person_add_white_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageFour = SliderPagerBuilder()
                .title("Berechtigungen für die App")
                .description("Damit du die App in vollen Zügen genießen kannst werden ein paar Berechtigungen benötigt. Möchtest du diese nicht, kann es sein, dass die App nicht immer ordnungsgemäß funktioniert. Die Berechtigungen kannst du auch nachträglich über die Einstellungen deines Handys erlauben.")
                .imageDrawable(R.drawable.ic_permission_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageFive = SliderPagerBuilder()
                .title("Navigation zum Wunschort")
                .description("Für jeden Wunsch kannst du einen Ort hinterlegen bei dem der Wunsch zu finden ist. Klickst du auf das kartensymbol wird automatisch die Navigation gestartet. Um dieses Feature nutzen zu können werden deine Standortdaten benötigt. Wishlist speichert deine Standortdaten nicht und gibt diese nur an die Navigationsapp weiter. ")
                .imageDrawable(R.drawable.ic_map_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageSix = SliderPagerBuilder()
                .title("Gib deinen Wünschen ein Gesicht")
                .description("In der Wihslist ist es möglich zu jedem Wunsch ein Bild zu hinterlegen. Dafür kannst du Bilder mit deiner Kamera machen, Bilder aus dem Internet herunterladen oder vorhandene Bilder verwenden. Damit dies möglich ist, wird Zugriff auf deine Kamera benötigt, sowie Schreib- und Leserechte auf deinen Speicher.")
                .imageDrawable(R.drawable.ic_add_photo_intro)
                .backgroundColor(backgroundColor)
                .build()

        val pageSeven = SliderPagerBuilder()
                .title("Viel Spaß beim Benutzen der App!")
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