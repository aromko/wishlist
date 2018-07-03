package aromko.de.wishlist.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.viewModel.WishViewModel;

public class WishActivity extends AppCompatActivity {

    private ImageButton btnAddPhoto;
    private ImageView ivProduct;
    private EditText txtTitle;
    private EditText txtPrice;
    private EditText txtUrl;
    private EditText txtDescription;
    private Spinner spWishstrength;
    private FrameLayout flProgressBarHolder;

    private WishViewModel wishViewModel;
    private String wishlistId;

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.wishstrength_selection_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
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

    public void addImageFromStorage(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
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
                    ivProduct.setImageURI(uri);
                    ivProduct.setTag("imageChanged");
                    ivProduct.setRotation(rotate);
                }
            }
        }
    }

    public void saveWish(View view) {
        flProgressBarHolder.setVisibility(View.VISIBLE);
        Bitmap bitmap = ((BitmapDrawable) ivProduct.getDrawable()).getBitmap();

        Wish wish = new Wish(txtTitle.getText().toString(), Double.valueOf(txtPrice.getText().toString()), txtUrl.getText().toString(), txtDescription.getText().toString(), Long.valueOf(spWishstrength.getSelectedItemId()), true, false, System.currentTimeMillis() / 1000);
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
}
