package aromko.de.wishlist.activity

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import aromko.de.wishlist.R
import aromko.de.wishlist.utilities.PhotoHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import de.hdodenhof.circleimageview.CircleImageView

class ProfilActivity : AppCompatActivity() {
    var photoHelper: PhotoHelper? = null
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var ivProfileImage: CircleImageView? = null
    private var flProgressBarHolder: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle(R.string.txtEditProfile)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        ivProfileImage = findViewById(R.id.civImage)
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder)
        val fFirebaseUser = FirebaseAuth.getInstance().currentUser
        etName?.setText(fFirebaseUser!!.displayName)
        etEmail?.setText(fFirebaseUser!!.email)
        photoHelper = PhotoHelper(this)
        photoHelper!!.requestProfilePicture(fFirebaseUser!!.uid)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun saveProfile(view: View?) {
        flProgressBarHolder!!.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(etName!!.text.toString())
                .setPhotoUri(Uri.parse(user!!.uid))
                .build()
        user.updateProfile(profileUpdates)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        if (ivProfileImage!!.tag.toString() == "imageChanged") {
                            val bitmap = (ivProfileImage!!.drawable as BitmapDrawable).bitmap
                            photoHelper!!.uploadImage(bitmap, "", user.uid, null, null, null)
                        } else {
                            Toast.makeText(applicationContext, R.string.txtSuccessfulSave, Toast.LENGTH_LONG).show()
                            flProgressBarHolder!!.visibility = View.GONE
                            finish()
                        }
                    }
                }
    }

    fun showPhotoSelectionDialog(view: View?) {
        photoHelper!!.startPhotoSelectionDialog()
    }

    fun addImageFromLocalStorage(v: View?) {
        photoHelper!!.requestImageFromStorage()
    }

    fun dispatchTakePictureIntent(view: View?) {
        photoHelper!!.requestImageFromCapture()
    }

    fun downloadImage(view: View?) {
        photoHelper!!.downloadImageFromWeb()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoHelper!!.onActivityResult(requestCode, resultCode, data)
    }
}