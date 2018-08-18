package aromko.de.wishlist.utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import aromko.de.wishlist.R;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class PhotoHelper {

    static final int REQUEST_IMAGE_FROM_STORAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private AlertDialog dialog;
    private EditText etDownloadUrl;
    private CircleImageView civImage;
    private FrameLayout flProgressBarHolder;
    private Activity mContext;

    public PhotoHelper(Activity mContext) {
        this.mContext = mContext;
    }

    public void startPhotoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View v = mContext.getLayoutInflater().inflate(R.layout.dialog_photo_selection, null);
        etDownloadUrl = v.findViewById(R.id.etDownloadurl);
        builder.setView(v);
        civImage = mContext.findViewById(R.id.civImage);
        flProgressBarHolder = mContext.findViewById(R.id.flProgressBarHolder);
        dialog = builder.create();
        dialog.show();
    }

    public void requestImageFromStorage() {
        cancelDialog();
        mContext.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_IMAGE_FROM_STORAGE);
    }

    public void requestImageFromCapture() {
        cancelDialog();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void downloadImageFromWeb() {
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage;

        String url = etDownloadUrl.getText().toString();
        try {
            myImage = task.execute(url).get();
            civImage.setImageBitmap(myImage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        cancelDialog();
    }

    public void cancelDialog() {
        dialog.cancel();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_FROM_STORAGE:
                    if (data != null) {
                        Uri uri = data.getData();
                        civImage.setImageURI(uri);
                        civImage.setTag("imageChanged");
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    civImage.setImageBitmap(imageBitmap);
                    civImage.setTag("imageChanged");
                    break;
            }
        }
    }

    public void uploadImage(Bitmap bitmap, final String wishkey, String userId, final WishViewModel wishViewModel, final String wishlistId) {
        String reference = wishkey;
        if (userId != null) {
            reference = userId;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
        final StorageReference storageRef = storage.getReference(reference);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("rotation", Float.valueOf(civImage.getRotation()).toString())
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
                Toast.makeText(mContext.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(mContext.getApplicationContext(), "Speichern erfolgreich.", Toast.LENGTH_LONG).show();
                flProgressBarHolder.setVisibility(View.GONE);
                if (wishViewModel != null) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            wishViewModel.updatePhotoUrl(wishlistId, wishkey, uri);
                        }
                    });
                }
                mContext.finish();
            }
        });

    }

    public void requestProfilePicture(String uId) {
        FirebaseStorage STORAGE = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
        STORAGE.getReference(uId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                Picasso.get()
                        .load(String.valueOf(uri))
                        .transform(new CircleTransform())
                        .resize(200, 200)
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into((ImageView) mContext.findViewById(R.id.civImage));
            }
        });
    }

    public void requestProductPicture(String photoUrl) {
        Picasso.get()
                .load(Uri.parse(photoUrl))
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into((CircleImageView) mContext.findViewById(R.id.civImage));

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
