package aromko.de.wishlist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.utilities.PhotoHelper;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;


public class EditWishActivity extends AppCompatActivity {

    private static int AUTOCOMPLETE_REQUEST_CODE = 3;
    private static List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
    private ImageButton btnAddPhoto;
    private CircleImageView ivProductImage;
    private EditText etTitle;
    private EditText etPrice;
    private EditText etUrl;
    private EditText etDescription;
    private Spinner spWishstrength;
    private FrameLayout flProgressBarHolder;
    private WishViewModel wishViewModel = new WishViewModel();
    private String wishlistId;
    private String wishId;
    private TextView tvLocation;
    private double longitude;
    private double latitude;
    private String placeId;
    private boolean isImageSet;
    private ImageButton btnDeleteImage;
    private String photoUrl;

    PhotoHelper photoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wish);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.txtEditWish);

        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        ivProductImage = findViewById(R.id.civImage);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etUrl = findViewById(R.id.etUrl);
        etDescription = findViewById(R.id.etDescription);
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder);
        spWishstrength = findViewById(R.id.spWishstrength);
        tvLocation = findViewById(R.id.tvLocation);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.wishstrength_selection_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spWishstrength.setAdapter(adapter);

        photoHelper = new PhotoHelper(this);

        Intent myIntent = getIntent();
        wishlistId = myIntent.getStringExtra("wishlistId");
        wishId = myIntent.getStringExtra("wishId");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyD-KB0cNj0mGvmhNQ2FyFXIXDRKeHDV5Ck");
        }

        PlacesClient placesClient = Places.createClient(this);

        wishViewModel.selectWish(wishlistId, wishId, wish -> {

            etTitle.setText(wish.getTitle());
            etPrice.setText(String.valueOf(wish.getPrice()).replace(".", ","));
            etUrl.setText(wish.getUrl());
            etDescription.setText(wish.getDescription());
            spWishstrength.setSelection((int) wish.getWishstrength());
            isImageSet = wish.isImageSet();
            if (wish.getPhotoUrl() != null) {
                photoUrl = wish.getPhotoUrl();
            } else {
                photoUrl = "";
            }

            if (isImageSet && !photoUrl.equals("")) {
                photoHelper.requestProductPicture(photoUrl);
            }

            longitude = wish.getLongitude();
            latitude = wish.getLatitude();
            if (wish.getPlaceId() != null) {
                FetchPlaceRequest request = FetchPlaceRequest.builder(wish.getPlaceId(), PLACE_FIELDS)
                        .build();
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    tvLocation.setText(setLocationText(place));
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Toast.makeText(getApplicationContext(), R.string.txtNoPlaceFound, Toast.LENGTH_LONG).show();
                    }
                });
            }

            btnDeleteImage.setTag(photoUrl);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPhotoSelectionDialog(View view) {
        photoHelper.startPhotoSelectionDialog();
    }

    public void addImageFromLocalStorage(View v) {
        photoHelper.requestImageFromStorage();
    }

    public void removeImageFromView(View view) {
        photoHelper.removeImageFromView();
    }

    public void dispatchTakePictureIntent(View view) {
        photoHelper.requestImageFromCapture();
    }

    public void downloadImage(View view) {
        photoHelper.downloadImageFromWeb();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                tvLocation.setText(setLocationText(place));
                placeId = place.getId();
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(getApplicationContext(), R.string.txtErrorPlaces + " " + status, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    public void saveWish(View view) {
        flProgressBarHolder.setVisibility(View.VISIBLE);
        Bitmap bitmap = null;
        if (ivProductImage.getDrawable() != null) {
            bitmap = ((BitmapDrawable) ivProductImage.getDrawable()).getBitmap();
        }

        if (ivProductImage.getTag().toString().equals(getString(R.string.txtImageChanged))) {
            isImageSet = true;
        } else if (ivProductImage.getTag().toString().equals(getString(R.string.txtImageDeleted)) && !"".equals(photoUrl)) {
            photoHelper.deleteImageFromFirebaseStorageFromUrl(btnDeleteImage.getTag().toString());
            isImageSet = false;
            photoUrl = "";
        }

        double price = 0.00;
        if (!etPrice.getText().toString().isEmpty()) {
            price = Double.valueOf(etPrice.getText().toString().replace(",", "."));
        }
        Wish wish = new Wish(etTitle.getText().toString(), price, etUrl.getText().toString(), etDescription.getText().toString(), Long.valueOf(spWishstrength.getSelectedItemId()), isImageSet, System.currentTimeMillis() / 1000, longitude, latitude, price, placeId, photoUrl);
        wishViewModel.updateWish(wishlistId, wishId, wish);
        if (!ivProductImage.getTag().toString().equals(getString(R.string.txtImageChanged))) {
            Toast.makeText(getApplicationContext(), R.string.txtSuccessfulChangedWish, Toast.LENGTH_LONG).show();
            flProgressBarHolder.setVisibility(View.GONE);
            finish();
        } else {
            String userId = null;
            if (bitmap != null) {
                photoHelper.uploadImage(bitmap, wishId, userId, wishViewModel, wishlistId);
            }
        }
    }

    public void placePicker(View view) {
        //s. https://developers.google.com/places/android-sdk/client-migration#field-masks
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, PLACE_FIELDS)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }


    private String setLocationText(Place place) {
        return place.getName() + "\n" + place.getAddress();
    }
}
