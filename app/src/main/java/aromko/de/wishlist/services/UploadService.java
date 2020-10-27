package aromko.de.wishlist.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import aromko.de.wishlist.R;
import aromko.de.wishlist.viewModel.WishViewModel;

public class UploadService extends JobIntentService {

    static final int JOB_ID = 1000;
    public static final String WISHKEY = "wishkey";
    public static final String WISHLISTID = "wishlistId";
    public static final String REFERENCE = "reference";
    public static final String DATA = "data";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "aromko.de.wishlist.MainActivity";

    private int result = Activity.RESULT_CANCELED;
    private WishViewModel wishViewModel = new WishViewModel();

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, UploadService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SimpleJobIntentService", "JOb started");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i("SimpleJobIntentService", "Executing work: " + intent);
        final String wishkey = intent.getStringExtra(WISHKEY);
        final String wishlistId = intent.getStringExtra(WISHLISTID);
        final String reference = intent.getStringExtra(REFERENCE);
        byte[] data = intent.getByteArrayExtra(DATA);
        final String FIREBASE_STORAGE_BUCKET = "gs://" + getApplicationContext().getString(R.string.google_storage_bucket);

        FirebaseStorage storage = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET);
        final StorageReference storageRef = storage.getReference(reference);

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            handleFirebaseStorageExceptions(exception);
            publishResults(result);
        }).addOnSuccessListener(taskSnapshot -> {
            if (wishlistId != null && wishkey != null) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> wishViewModel.updatePhotoUrl(wishlistId, wishkey, uri));
            }
            publishResults(Activity.RESULT_OK);
        });
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
    }

    private void publishResults(int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    private String handleFirebaseStorageExceptions(@NonNull Exception exception) {
        int errorCode = ((StorageException) exception).getErrorCode();
        String errorMessage = exception.getMessage();

        switch (errorCode) {
            case StorageException.ERROR_UNKNOWN:
                errorMessage += " " + R.string.txtUnknownError;
                break;
            case StorageException.ERROR_BUCKET_NOT_FOUND:
            case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
            case StorageException.ERROR_QUOTA_EXCEEDED:
            case StorageException.ERROR_PROJECT_NOT_FOUND:
            case StorageException.ERROR_NOT_AUTHORIZED:
            case StorageException.ERROR_NOT_AUTHENTICATED:
            case StorageException.ERROR_INVALID_CHECKSUM:
            case StorageException.ERROR_CANCELED:
                break;
            case StorageException.ERROR_OBJECT_NOT_FOUND:
                errorMessage += " " + R.string.txtObjectNotFound;
                break;
        }
        return errorMessage;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SimpleJobIntentService", "JOb started");
    }

}
