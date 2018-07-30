package aromko.de.wishlist.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.viewModel.WishViewModel;

public class WishActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_FROM_STORAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int PLACE_PICKER_REQUEST = 3;
    private ImageButton btnAddPhoto;
    private ImageView ivProduct;
    private EditText txtTitle;
    private EditText txtPrice;
    private EditText txtUrl;
    private EditText txtDescription;
    private Spinner spWishstrength;
    private FrameLayout flProgressBarHolder;
    private EditText tvDownloadUrl;
    private WishViewModel wishViewModel;
    private String wishlistId;
    private AlertDialog dialog;
    private TextView tvLocation;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Wunsch hinzufügen");

        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        ivProduct = findViewById(R.id.ivProductImage);
        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtUrl = findViewById(R.id.txtUrl);
        txtDescription = findViewById(R.id.txtDescription);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPhotoSelectionDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_photo_selection, null);
        tvDownloadUrl = (EditText) v.findViewById(R.id.downloadurl);
        builder.setView(v);
        dialog = builder.create();
        dialog.show();
    }

    public void addImageFromStorage(View view) {
        dialog.cancel();
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
    }

    public void dispatchTakePictureIntent(View view) {
        dialog.cancel();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void downloadImage(View view) {
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage;

        String url = tvDownloadUrl.getText().toString();
        try {
            myImage = task.execute(url).get();
            ivProduct.setImageBitmap(myImage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        dialog.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_FROM_STORAGE:
                    if (data != null) {
                        Uri uri = data.getData();
                        int rotate = 0;
                        try {
                            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
                            ExifInterface exif = new ExifInterface(inputStream);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotate = 270;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotate = 180;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotate = 90;
                                    break;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ivProduct.setRotation(rotate);
                        ivProduct.setImageURI(uri);
                        ivProduct.setTag("imageChanged");
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivProduct.setImageBitmap(imageBitmap);
                    ivProduct.setTag("imageChanged");
                    break;
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
        Bitmap bitmap = ((BitmapDrawable) ivProduct.getDrawable()).getBitmap();

        boolean isImageSet = false;
        if (ivProduct.getTag().toString().equals("imageChanged")) {
            isImageSet = true;
        }
        Wish wish = new Wish(txtTitle.getText().toString(), Double.valueOf(txtPrice.getText().toString().replace(",", ".")), txtUrl.getText().toString(), txtDescription.getText().toString(), Long.valueOf(spWishstrength.getSelectedItemId()), isImageSet, System.currentTimeMillis() / 1000, longitude, latitude);
        String wishkey = wishViewModel.insertWish(wishlistId, wish);
        if (wishkey.isEmpty() || !ivProduct.getTag().toString().equals("imageChanged")) {
            Toast.makeText(getApplicationContext(), "Wunsch wurde erfolgreich hinzugefügt.", Toast.LENGTH_LONG).show();
            flProgressBarHolder.setVisibility(View.GONE);
            finish();
        } else {
            uploadImage(bitmap, wishkey);
        }
    }

    public void uploadImage(Bitmap bitmap, String wishkey) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
        StorageReference storageRef = storage.getReference(wishkey);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("rotation", Float.valueOf(ivProduct.getRotation()).toString())
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                switch (errorCode) {
                    case -13000:
                        errorMessage += "Unbekannter Fehler.";
                }
                flProgressBarHolder.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "Wunsch wurde erfolgreich hinzugefügt.", Toast.LENGTH_LONG).show();
                flProgressBarHolder.setVisibility(View.GONE);
                finish();
            }
        });

    }

    public void placePicker(View view) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();

                    Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                    return myBitmap;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
