package aromko.de.wishlist.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import aromko.de.wishlist.R
import aromko.de.wishlist.model.Wish
import aromko.de.wishlist.utilities.PhotoHelper
import aromko.de.wishlist.utilities.Validator
import aromko.de.wishlist.viewModel.WishViewModel
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class EditWishActivity : AppCompatActivity() {
    private var btnAddPhoto: ImageButton? = null
    private var ivProductImage: CircleImageView? = null
    private var etTitle: EditText? = null
    private var etPrice: EditText? = null
    private var etUrl: EditText? = null
    private var etDescription: EditText? = null
    private var spWishstrength: Spinner? = null
    private var flProgressBarHolder: FrameLayout? = null
    private val wishViewModel = WishViewModel()
    private var wishlistId: String? = null
    private var wishId: String? = null
    private var tvLocation: TextView? = null
    private var longitude = 0.0
    private var latitude = 0.0
    private var placeId: String? = null
    private var isImageSet = false
    private var btnDeleteImage: ImageButton? = null
    private var photoUrl: String? = null
    private var favoriteListId: String? = ""
    private var salvagePrice = 0.0
    var photoHelper: PhotoHelper? = null
    var awesomeValidation: AwesomeValidation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_wish)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle(R.string.txtEditWish)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        ivProductImage = findViewById(R.id.civImage)
        etTitle = findViewById(R.id.etTitle)
        etPrice = findViewById(R.id.etPrice)
        etUrl = findViewById(R.id.etUrl)
        etDescription = findViewById(R.id.etDescription)
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder)
        spWishstrength = findViewById(R.id.spWishstrength)
        tvLocation = findViewById(R.id.tvLocation)
        btnDeleteImage = findViewById(R.id.btnDeleteImage)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.wishstrength_selection_array, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spWishstrength?.adapter = adapter
        photoHelper = PhotoHelper(this)
        val myIntent = intent
        wishlistId = myIntent.getStringExtra("wishlistId")
        wishId = myIntent.getStringExtra("wishId")
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyD-KB0cNj0mGvmhNQ2FyFXIXDRKeHDV5Ck")
        }
        val placesClient = Places.createClient(this)
        wishViewModel.selectWish(wishlistId, wishId) { wish: Wish ->
            etTitle?.setText(wish.title)
            etPrice?.setText(wish.price.toString().replace(".", ","))
            etUrl?.setText(wish.url)
            etDescription?.setText(wish.description)
            spWishstrength?.setSelection(wish.wishstrength.toInt())
            isImageSet = wish.isImageSet
            photoUrl = if (wish.photoUrl != null) {
                wish.photoUrl
            } else {
                ""
            }
            if (isImageSet && photoUrl != "") {
                photoHelper!!.requestProductPicture(photoUrl)
            }
            longitude = wish.longitude
            latitude = wish.latitude
            if (wish.placeId != null) {
                val request = FetchPlaceRequest.builder(wish.placeId!!, PLACE_FIELDS)
                        .build()
                placesClient.fetchPlace(request).addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    tvLocation?.text = setLocationText(place)
                }.addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        val apiException = exception
                        Toast.makeText(applicationContext, R.string.txtNoPlaceFound, Toast.LENGTH_LONG).show()
                    }
                }
            }
            btnDeleteImage?.tag = photoUrl
            salvagePrice = wish.salvagePrice
        }
        val fFirebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = getSharedPreferences(fFirebaseAuth.currentUser!!.uid, MODE_PRIVATE)
        favoriteListId = sharedPreferences.getString("favoriteListId", "")
        awesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        awesomeValidation!!.addValidation(this, R.id.etTitle, RegexTemplate.NOT_EMPTY, R.string.invalid_title)
        awesomeValidation!!.addValidation(this, R.id.etPrice, "(^\\s*)|([0-9]+([,][0-9]{1,2})?)", R.string.invalid_price)
        awesomeValidation!!.addValidation(this, R.id.etUrl, "(^\\s*)|(^(http|https)://" + Patterns.WEB_URL + ")", R.string.invalid_url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showPhotoSelectionDialog(view: View?) {
        photoHelper!!.startPhotoSelectionDialog()
    }

    fun addImageFromLocalStorage(v: View?) {
        photoHelper!!.requestImageFromStorage()
    }

    fun removeImageFromView(view: View?) {
        photoHelper!!.removeImageFromView()
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
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                tvLocation!!.text = setLocationText(place)
                placeId = place.id
                longitude = place.latLng!!.longitude
                latitude = place.latLng!!.latitude
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Toast.makeText(applicationContext, R.string.txtErrorPlaces.toString() + " " + status, Toast.LENGTH_LONG).show()
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    fun saveWish(view: View?) {
        var url =  etUrl!!.text.toString()
        if (!url.isEmpty() && Validator.checkUrl(etUrl?.text.toString())) {
            etUrl?.setText("https://" + url)
        }

        if (awesomeValidation!!.validate()) {
            flProgressBarHolder!!.visibility = View.VISIBLE
            var bitmap: Bitmap? = null
            if (ivProductImage!!.drawable != null) {
                bitmap = (ivProductImage!!.drawable as BitmapDrawable).bitmap
            }
            if (ivProductImage!!.tag.toString() == getString(R.string.txtImageChanged)) {
                isImageSet = true
            } else if (ivProductImage!!.tag.toString() == getString(R.string.txtImageDeleted) && "" != photoUrl) {
                photoHelper!!.deleteImageFromFirebaseStorageFromUrl(btnDeleteImage!!.tag.toString())
                isImageSet = false
                photoUrl = ""
            }
            var price = 0.00
            if (!etPrice!!.text.toString().isEmpty()) {
                price = java.lang.Double.valueOf(etPrice!!.text.toString().replace(",", "."))
            }
            val wish = Wish(etTitle!!.text.toString(), price, etUrl!!.text.toString(), etDescription!!.text.toString(), java.lang.Long.valueOf(spWishstrength!!.selectedItemId), isImageSet, System.currentTimeMillis() / 1000, longitude, latitude, salvagePrice, placeId, photoUrl)
            wishViewModel.updateWish(wishlistId, wishId, wish, favoriteListId)
            if (ivProductImage!!.tag.toString() != getString(R.string.txtImageChanged)) {
                Toast.makeText(applicationContext, R.string.txtSuccessfulChangedWish, Toast.LENGTH_LONG).show()
                flProgressBarHolder!!.visibility = View.GONE
                finish()
            } else {
                val userId: String? = null
                if (bitmap != null) {
                    photoHelper!!.uploadImage(bitmap, wishId, userId, wishViewModel, wishlistId, favoriteListId)
                }
            }
        }
    }

    fun placePicker(view: View?) {
        //s. https://developers.google.com/places/android-sdk/client-migration#field-masks
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, PLACE_FIELDS)
                .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun setLocationText(place: Place): String {
        return """
            ${place.name}
            ${place.address}
            """.trimIndent()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
        private val PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
    }
}