package aromko.de.wishlist.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import aromko.de.wishlist.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

private const val TITLE_TAG = "preferencesActivityTitle"

class SettingsActivity : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.preferences, MainPreferences())
                    .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.txtSettings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
            caller: PreferenceFragmentCompat,
            pref: Preference,
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.preferences, fragment)
                .addToBackStack(null)
                .commit()
        title = pref.title
        return true
    }

    class MainPreferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_mainfile, rootKey)
        }
    }

    class GeneralPreferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_general, rootKey)

            val fFirebaseAuth = FirebaseAuth.getInstance()
            val sharedPreferences = activity?.getSharedPreferences(fFirebaseAuth.currentUser!!.uid, MODE_PRIVATE)
            val editTextPref: EditTextPreference? = findPreference("favoriteListId")
            editTextPref?.summary = sharedPreferences?.getString("favoriteListId", "")
        }
    }

    class NotificationPreferences : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_notifications, rootKey)

            val wishlistNotification: SwitchPreference? =
                findPreference("notifications_wishlist_active")
            wishlistNotification?.onPreferenceChangeListener = this
            val chatNotification: SwitchPreference? =
                findPreference("notifications_chat_messages_active")
            chatNotification?.onPreferenceChangeListener = this

        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            when (preference?.key) {
                "notifications_wishlist_active" -> handleNotificationTopic(newValue, "wishlistNotifications")
                "notifications_chat_messages_active" -> handleNotificationTopic(newValue, "chatNotifications")
                else -> {
                    Log.i("SettingsActivity", "No topic for setting " + preference?.key + " defined.")
                }
            }

            return true
        }

        private fun handleNotificationTopic(newValue: Any?, topic: String) {
            if (newValue as Boolean) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            }
        }
    }
}