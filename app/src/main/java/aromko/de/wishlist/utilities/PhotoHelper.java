package aromko.de.wishlist.utilities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
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
import aromko.de.wishlist.services.UploadService;
import aromko.de.wishlist.viewModel.WishViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PhotoHelper {

    private static final int REQUEST_IMAGE_FROM_STORAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private AlertDialog dialog;
    private EditText etDownloadUrl;
    private CircleImageView civImage;
    private FrameLayout flProgressBarHolder;
    private Activity mContext;
    private Uri imageUri;

    public PhotoHelper(Activity mContext) {
        this.mContext = mContext;
    }

    public void startPhotoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View v = mContext.getLayoutInflater().inflate(R.layout.dialog_photo_selection, null);
        etDownloadUrl = v.findViewById(R.id.etDownloadurl);
        civImage = mContext.findViewById(R.id.civImage);
        flProgressBarHolder = mContext.findViewById(R.id.flProgressBarHolder);
        builder.setView(v);
        dialog = builder.create();
        dialog.show();
    }

    public void requestImageFromStorage() {
        cancelDialog();
        mContext.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_IMAGE_FROM_STORAGE);
    }

    public void requestImageFromCapture() {
        cancelDialog();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = mContext.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        mContext.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public void downloadImageFromWeb() {
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage;

        String url = etDownloadUrl.getText().toString();
        try {
            myImage = task.execute(url).get();
            if (myImage != null) {
                civImage.setImageBitmap(myImage);
                civImage.setTag(mContext.getString(R.string.txtImageChanged));

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
                    Bitmap imageBitmap = null;
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(
                                mContext.getContentResolver(), imageUri);

                        civImage.setImageBitmap(imageBitmap);
                        civImage.setTag(mContext.getString(R.string.txtImageChanged));
                    } catch (IOException e) {
                        Log.e("REQUEST_IMAGE_CAPTURE", "setBitmap()", e);
                    }
                    break;
            }
        } else {
            if (resultCode == RESULT_CANCELED && requestCode == REQUEST_IMAGE_CAPTURE) {
                int rowsDeleted = mContext.getContentResolver().delete(imageUri, null, null);
                Log.d("PHOTOHELPER", rowsDeleted + " rows deleted");
            }
        }
    }

    public void uploadImage(Bitmap bitmap, final String wishkey, String userId, final WishViewModel wishViewModel, final String wishlistId, String favoriteListId) {
        String reference = wishkey;
        if (userId != null) {
            reference = userId;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] data = baos.toByteArray();

        Intent intent = new Intent(mContext, UploadService.class);
        intent.putExtra(UploadService.WISHKEY, wishkey);
        intent.putExtra(UploadService.WISHLISTID, wishlistId);
        intent.putExtra(UploadService.FAVORITELISTID, favoriteListId);
        intent.putExtra(UploadService.REFERENCE, reference);
        intent.putExtra(UploadService.DATA, data);
        UploadService.enqueueWork(mContext, intent);

        Toast.makeText(mContext.getApplicationContext(), R.string.txtSuccessfulSave, Toast.LENGTH_LONG).show();
        flProgressBarHolder.setVisibility(View.GONE);

        mContext.finish();

    }

    public void requestProfilePicture(String uId) {
        final String FIREBASE_STORAGE_BUCKET = "gs://" + mContext.getString(R.string.google_storage_bucket);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET);

        firebaseStorage.getReference(uId).getDownloadUrl().addOnSuccessListener(uri ->
                Picasso.get()
                        .load(String.valueOf(uri))
                        .transform(new CircleTransform())
                        .resize(200, 200)
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into((CircleImageView) mContext.findViewById(R.id.civImage)))
                .addOnFailureListener(exception -> Log.e("requestProfilePicture", handleFirebaseStorageExceptions(exception)));
    }

    public void removeImageFromView() {
        mContext.findViewById(R.id.civImage).setTag(mContext.getString(R.string.txtImageDeleted));
        mContext.findViewById(R.id.civImage).setBackgroundResource(R.drawable.no_image_available);
        Picasso.get()
                .load(String.valueOf(mContext.getDrawable(R.drawable.no_image_available)))
                .transform(new CircleTransform())
                .resize(200, 200)
                .centerCrop()
                .into((CircleImageView) mContext.findViewById(R.id.civImage));
    }

    public void deleteImageFromFirebaseStorageFromUrl(String photoUrl) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
        storageReference.delete().addOnSuccessListener(aVoid -> {
            Log.e("firebasestorage", "onSuccess: deleted file");
        }).addOnFailureListener(exception -> Log.e("firebasestorage", "onFailure: did not delete file"));
    }

    private String handleFirebaseStorageExceptions(@NonNull Exception exception) {
        int errorCode = ((StorageException) exception).getErrorCode();
        String errorMessage = exception.getMessage();

        switch (errorCode) {
            case StorageException.ERROR_UNKNOWN:
                errorMessage += " " + mContext.getString(R.string.txtUnknownError);
                break;
            case StorageException.ERROR_BUCKET_NOT_FOUND:
            case StorageException.ERROR_CANCELED:
            case StorageException.ERROR_INVALID_CHECKSUM:
            case StorageException.ERROR_NOT_AUTHENTICATED:
            case StorageException.ERROR_NOT_AUTHORIZED:
            case StorageException.ERROR_PROJECT_NOT_FOUND:
            case StorageException.ERROR_QUOTA_EXCEEDED:
            case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                break;
            case StorageException.ERROR_OBJECT_NOT_FOUND:
                errorMessage += " " + mContext.getString(R.string.txtObjectNotFound);
                break;
        }
        return errorMessage;
    }

    public void requestProductPicture(String photoUrl) {
        Picasso.get()
                .load(Uri.parse(photoUrl))
                .networkPolicy(NetworkPolicy.OFFLINE).stableKey(photoUrl)
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
