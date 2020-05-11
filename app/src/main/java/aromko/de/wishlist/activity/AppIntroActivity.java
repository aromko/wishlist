package aromko.de.wishlist.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class AppIntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWizardMode(true);
        setProgressIndicator();
        setImmersiveMode();
        addSlide(AppIntroFragment.newInstance( "Welcome...", "This is the first slide of the example"));
        addSlide(AppIntroFragment.newInstance("...Let's get started!", "This is the last slide, I won't annoy you more :)"));


    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        finish();
    }
}
