package aromko.de.wishlist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import aromko.de.wishlist.R;
import aromko.de.wishlist.utilities.PhotoHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    PhotoHelper photoHelper;
    private EditText etName;
    private EditText etEmail;
    private CircleImageView ivProfileImage;
    private FrameLayout flProgressBarHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.txtEditProfile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivProfileImage = findViewById(R.id.civImage);
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder);

        final FirebaseUser fFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        etName.setText(fFirebaseUser.getDisplayName());
        etEmail.setText(fFirebaseUser.getEmail());

        photoHelper = new PhotoHelper(this);
        photoHelper.requestProfilePicture(fFirebaseUser.getUid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveProfile(View view) {
        flProgressBarHolder.setVisibility(View.VISIBLE);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString())
                .setPhotoUri(Uri.parse(user.getUid()))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (ivProfileImage.getTag().toString().equals("imageChanged")) {
                            Bitmap bitmap = ((BitmapDrawable) ivProfileImage.getDrawable()).getBitmap();
                            photoHelper.uploadImage(bitmap, "", user.getUid(), null, null, null);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.txtSuccessfulSave, Toast.LENGTH_LONG).show();
                            flProgressBarHolder.setVisibility(View.GONE);
                            finish();
                        }
                    }
                });
    }

    public void showPhotoSelectionDialog(View view) {
        photoHelper.startPhotoSelectionDialog();
    }

    public void addImageFromLocalStorage(View v) {
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
    }
}
