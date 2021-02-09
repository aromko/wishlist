package aromko.de.wishlist.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.core.app.JobIntentService
import aromko.de.wishlist.R
import aromko.de.wishlist.viewModel.WishViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.UploadTask

class UploadService : JobIntentService() {
    private val result = Activity.RESULT_CANCELED
    private val wishViewModel = WishViewModel()
    override fun onCreate() {
        super.onCreate()
        Log.i("SimpleJobIntentService", "JOb started")
    }

    override fun onHandleWork(intent: Intent) {
        Log.i("SimpleJobIntentService", "Executing work: $intent")
        val wishkey = intent.getStringExtra(WISHKEY)
        val wishlistId = intent.getStringExtra(WISHLISTID)
        val favoritListId = intent.getStringExtra(FAVORITELISTID)
        val reference = intent.getStringExtra(REFERENCE)
        val data = intent.getByteArrayExtra(DATA)
        val FIREBASE_STORAGE_BUCKET = "gs://" + applicationContext.getString(R.string.google_storage_bucket)
        val storage = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET)
        val storageRef = storage.getReference(reference!!)
        val uploadTask = storageRef.putBytes(data!!)
        uploadTask.addOnFailureListener { exception: Exception ->
            handleFirebaseStorageExceptions(exception)
            publishResults(result)
        }.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
            if (wishlistId != null && wishkey != null) {
                storageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                    wishViewModel.updatePhotoUrl(wishlistId, wishkey, uri)
                    if (favoritListId != null) {
                        wishViewModel.updatePhotoUrl(favoritListId, wishkey, uri)
                    }
                }
            }
            publishResults(Activity.RESULT_OK)
        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime())
    }

    private fun publishResults(result: Int) {
        val intent = Intent(NOTIFICATION)
        intent.putExtra(RESULT, result)
        sendBroadcast(intent)
    }

    private fun handleFirebaseStorageExceptions(exception: Exception): String? {
        val errorCode = (exception as StorageException).errorCode
        var errorMessage = exception.message
        when (errorCode) {
            StorageException.ERROR_UNKNOWN -> errorMessage += " " + R.string.txtUnknownError
            StorageException.ERROR_BUCKET_NOT_FOUND, StorageException.ERROR_RETRY_LIMIT_EXCEEDED, StorageException.ERROR_QUOTA_EXCEEDED, StorageException.ERROR_PROJECT_NOT_FOUND, StorageException.ERROR_NOT_AUTHORIZED, StorageException.ERROR_NOT_AUTHENTICATED, StorageException.ERROR_INVALID_CHECKSUM, StorageException.ERROR_CANCELED -> {
            }
            StorageException.ERROR_OBJECT_NOT_FOUND -> errorMessage += " " + R.string.txtObjectNotFound
        }
        return errorMessage
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SimpleJobIntentService", "JOb started")
    }

    companion object {
        const val JOB_ID = 1000
        const val WISHKEY = "wishkey"
        const val WISHLISTID = "wishlistId"
        const val FAVORITELISTID = "favoriteListId"
        const val REFERENCE = "reference"
        const val DATA = "data"
        const val RESULT = "result"
        const val NOTIFICATION = "aromko.de.wishlist.MainActivity"
        fun enqueueWork(context: Context?, work: Intent?) {
            enqueueWork(context!!, UploadService::class.java, JOB_ID, work!!)
        }
    }
}