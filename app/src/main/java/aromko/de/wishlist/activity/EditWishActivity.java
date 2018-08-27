package aromko.de.wishlist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.utilities.PhotoHelper;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditWishActivity extends AppCompatActivity {

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
    private WishViewModel wishViewModel = new WishViewModel();
    private String wishlistId;
    private String wishId;
    private TextView tvLocation;
    private double longitude;
    private double latitude;
    private String placeId;
    private boolean isImageSet;

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.wishstrength_selection_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spWishstrength.setAdapter(adapter);

        photoHelper = new PhotoHelper(this);

        Intent myIntent = getIntent();
        wishlistId = myIntent.getStringExtra("wishlistId");
        wishId = myIntent.getStringExtra("wishId");

        wishViewModel.selectWish(wishlistId, wishId, new WishViewModel.FirebaseCallback() {
            @Override
            public void onCallback(Wish wish) {

                etTitle.setText(wish.getTitle());
                etPrice.setText(String.valueOf(wish.getPrice()).replace(".", ","));
                etUrl.setText(wish.getUrl());
                etDescription.setText(wish.getDescription());
                spWishstrength.setSelection((int) wish.getWishstrength());
                isImageSet = wish.isImageSet();
                if (isImageSet && wish.getPhotoUrl() != null) {
                    photoHelper.requestProductPicture(wish.getPhotoUrl());
                }

                longitude = wish.getLongitude();
                latitude = wish.getLatitude();
                if (wish.getPlaceId() != null) {
                    Places.getGeoDataClient(getApplicationContext()).getPlaceById(wish.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            if (task.isSuccessful()) {
                                PlaceBufferResponse places = task.getResult();
                                Place myPlace = places.get(0);
                                tvLocation.setText(myPlace.getName() + "\n" + myPlace.getAddress());
                                places.release();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.txtNoPlaceFound, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
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
                    }
                    break;
            }
        }
    }

    public void saveWish(View view) {
        flProgressBarHolder.setVisibility(View.VISIBLE);
        Bitmap bitmap = null;
        if (ivProductImage.getDrawable() != null) {
            bitmap = ((BitmapDrawable) ivProductImage.getDrawable()).getBitmap();
        }

        double price = 0.00;
        if (!etPrice.getText().toString().isEmpty()) {
            price = Double.valueOf(etPrice.getText().toString().replace(",", "."));
        }
        Wish wish = new Wish(etTitle.getText().toString(), price, etUrl.getText().toString(), etDescription.getText().toString(), Long.valueOf(spWishstrength.getSelectedItemId()), isImageSet, System.currentTimeMillis() / 1000, longitude, latitude, price, placeId, "");
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


    public void placePicker(View view) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }
}
