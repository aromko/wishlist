package aromko.de.wishlist.utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import aromko.de.wishlist.R;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class PhotoHelper {

    private static final String FIREBASE_STORAGE_BUCKET = "gs://wishlist-app-aromko.appspot.com";
    private static final int REQUEST_IMAGE_FROM_STORAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
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
            if (myImage != null) {
                civImage.setImageBitmap(myImage);
            } else {
                civImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_image_available));
                Toast.makeText(mContext, R.string.txtNoImageSourceFound, Toast.LENGTH_LONG).show();
            }
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
                        civImage.setTag(mContext.getString(R.string.txtImageChanged));
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    civImage.setImageBitmap(imageBitmap);
                    civImage.setTag(mContext.getString(R.string.txtImageChanged));
                    break;
            }
        }
    }

    public void uploadImage(Bitmap bitmap, final String wishkey, String userId, final WishViewModel wishViewModel, final String wishlistId) {
        String reference = wishkey;
        if (userId != null) {
            reference = userId;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET);
        final StorageReference storageRef = storage.getReference(reference);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                flProgressBarHolder.setVisibility(View.GONE);
                Toast.makeText(mContext.getApplicationContext(), handleFirebaseStorageExceptions(exception), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(mContext.getApplicationContext(), R.string.txtSuccessfulSave, Toast.LENGTH_LONG).show();
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
        FirebaseStorage STORAGE = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET);

        STORAGE.getReference(uId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {

                Picasso.get()
                        .load(String.valueOf(uri))
                        .transform(new CircleTransform())
                        .resize(200, 200)
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into((ImageView) mContext.findViewById(R.id.civImage));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("requestProfilePicture", handleFirebaseStorageExceptions(exception));
            }
        });
    }

    private String handleFirebaseStorageExceptions(@NonNull Exception exception) {
        int errorCode = ((StorageException) exception).getErrorCode();
        String errorMessage = exception.getMessage();

        switch (errorCode) {
            case StorageException.ERROR_UNKNOWN:
                errorMessage += " " + mContext.getString(R.string.txtUnknownError);
                break;
            case StorageException.ERROR_BUCKET_NOT_FOUND:
                break;
            case StorageException.ERROR_CANCELED:
                break;
            case StorageException.ERROR_INVALID_CHECKSUM:
                break;
            case StorageException.ERROR_NOT_AUTHENTICATED:
                break;
            case StorageException.ERROR_NOT_AUTHORIZED:
                break;
            case StorageException.ERROR_OBJECT_NOT_FOUND:
                errorMessage += " " + mContext.getString(R.string.txtObjectNotFound);
                break;
            case StorageException.ERROR_PROJECT_NOT_FOUND:
                break;
            case StorageException.ERROR_QUOTA_EXCEEDED:
                break;
            case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                break;
        }
        return errorMessage;
    }

    public void requestProductPicture(String photoUrl) {
        Picasso.get()
                .load(Uri.parse(photoUrl))
                .networkPolicy(NetworkPolicy.OFFLINE)
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
