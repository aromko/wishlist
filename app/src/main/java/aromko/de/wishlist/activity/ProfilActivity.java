package aromko.de.wishlist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import aromko.de.wishlist.R;
import aromko.de.wishlist.utilities.PhotoHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ProfilActivity";

    private EditText etName;
    private EditText etEmail;
    private CircleImageView ivProfileImage;
    private FrameLayout flProgressBarHolder;

    PhotoHelper photoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profil bearbeiten");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivProfileImage = findViewById(R.id.civImage);
        flProgressBarHolder = findViewById(R.id.flProgressBarHolder);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        etName.setText(user.getDisplayName());
        etEmail.setText(user.getEmail());

        photoHelper = new PhotoHelper(this);
        photoHelper.requestProfilePicture(user.getUid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveProfile(View view){
        flProgressBarHolder.setVisibility(View.VISIBLE);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString())
                .setPhotoUri(Uri.parse(user.getUid()))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Bitmap bitmap = ((BitmapDrawable) ivProfileImage.getDrawable()).getBitmap();
                            photoHelper.uploadImage(bitmap, "", user.getUid());
                            /*
                            finish();
                            startActivity(new Intent(ProfilActivity.this, MainActivity.class));
                            */
                        }
                    }
                });
    }

    public void showPhotoSelectionDialog(View view){
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
        photoHelper.onActivityResult(requestCode,resultCode,data);
    }
}
