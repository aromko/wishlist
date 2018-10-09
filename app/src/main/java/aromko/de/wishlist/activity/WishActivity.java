package aromko.de.wishlist.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.utilities.PhotoHelper;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class WishActivity extends AppCompatActivity {

    static final int PLACE_PICKER_REQUEST = 3;
    PhotoHelper photoHelper;
    private ImageButton btnAddPhoto;
    private CircleImageView ivProductImage;
    private EditText etTitle;
    private EditText etPrice;
    private EditText etUrl;
    private EditText etDescription;
    private Spinner spWishstrength;
    private FrameLayout flProgressBarHolder;
    private WishViewModel wishViewModel;
    private String wishlistId;
    private TextView tvLocation;
    private double longitude;
    private double latitude;
    private String placeId;
    private String sharedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.txtAddWish);

        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        ivProductImage = findViewById(R.id.civImage);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etUrl = findViewById(R.id.etUrl);
        etDescription = findViewById(R.id.etDescription);
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder);
        spWishstrength = findViewById(R.id.spWishstrength);
        tvLocation = findViewById(R.id.tvLocation);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.wishstrength_selection_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spWishstrength.setAdapter(adapter);

        wishViewModel = ViewModelProviders.of(this).get(WishViewModel.class);

        Intent myIntent = getIntent();
        wishlistId = myIntent.getStringExtra("wishlistId");
        sharedText = myIntent.getStringExtra("sharedText");
        if (sharedText != null) {
            etTitle.setText(sharedText);
        }

        photoHelper = new PhotoHelper(this);
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

    public void addImageFromStorage(View v) {
        photoHelper.requestImageFromStorage();
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

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case PLACE_PICKER_REQUEST:
                    if (resultCode == RESULT_OK) {
                        Place place = PlacePicker.getPlace(this, data);
                        tvLocation.setText(place.getName() + "\n" + place.getAddress());
                        longitude = place.getLatLng().longitude;
                        latitude = place.getLatLng().latitude;
                        placeId = place.getId();
                    }
                    break;
            }
        }
    }

    public void saveWish(View view) {
        flProgressBarHolder.setVisibility(View.VISIBLE);
        Bitmap bitmap = ((BitmapDrawable) ivProductImage.getDrawable()).getBitmap();

        boolean isImageSet = false;
        if (ivProductImage.getTag().toString().equals(getString(R.string.txtImageChanged))) {
            isImageSet = true;
        }
        double price = 0.00;
        if (!etPrice.getText().toString().isEmpty()) {
            price = Double.valueOf(etPrice.getText().toString().replace(",", "."));
        }
        Wish wish = new Wish(etTitle.getText().toString(), price, etUrl.getText().toString(), etDescription.getText().toString(), Long.valueOf(spWishstrength.getSelectedItemId()), isImageSet, System.currentTimeMillis() / 1000, longitude, latitude, price, placeId, "");
        String wishkey = wishViewModel.insertWish(wishlistId, wish);
        if (wishkey.isEmpty() || !ivProductImage.getTag().toString().equals(getString(R.string.txtImageChanged))) {
            Toast.makeText(getApplicationContext(), R.string.txtWishSuccessfulAdded, Toast.LENGTH_LONG).show();
            flProgressBarHolder.setVisibility(View.GONE);
            finish();
        } else {
            String userId = null;
            photoHelper.uploadImage(bitmap, wishkey, userId, wishViewModel, wishlistId);
        }
    }


    public void placePicker(View view) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }
}
