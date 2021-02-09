package aromko.de.wishlist.utilities

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import aromko.de.wishlist.R
import aromko.de.wishlist.services.UploadService
import aromko.de.wishlist.viewModel.WishViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ExecutionException

class PhotoHelper(private val mContext: Activity) {
    private var dialog: AlertDialog? = null
    private var etDownloadUrl: EditText? = null
    private var civImage: CircleImageView? = null
    private var flProgressBarHolder: FrameLayout? = null
    private var imageUri: Uri? = null
    fun startPhotoSelectionDialog() {
        val builder = AlertDialog.Builder(mContext)
        val v = mContext.layoutInflater.inflate(R.layout.dialog_photo_selection, null)
        etDownloadUrl = v.findViewById(R.id.etDownloadurl)
        civImage = mContext.findViewById(R.id.civImage)
        flProgressBarHolder = mContext.findViewById(R.id.flProgressBarHolder)
        builder.setView(v)
        dialog = builder.create()
        dialog!!.show()
    }

    fun requestImageFromStorage() {
        cancelDialog()
        mContext.startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_IMAGE_FROM_STORAGE)
    }

    fun requestImageFromCapture() {
        cancelDialog()
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = mContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        mContext.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun downloadImageFromWeb() {
        val task = ImageDownloader()
        val myImage: Bitmap?
        val url = etDownloadUrl!!.text.toString()
        try {
            myImage = task.execute(url).get()
            if (myImage != null) {
                civImage!!.setImageBitmap(myImage)
                civImage!!.tag = mContext.getString(R.string.txtImageChanged)
            } else {
                civImage!!.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.drawable.no_image_available))
                Toast.makeText(mContext, R.string.txtNoImageSourceFound, Toast.LENGTH_LONG).show()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        cancelDialog()
    }

    fun cancelDialog() {
        dialog!!.cancel()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_FROM_STORAGE -> if (data != null) {
                    val uri = data.data
                    civImage!!.setImageURI(uri)
                    civImage!!.tag = mContext.getString(R.string.txtImageChanged)
                }
                REQUEST_IMAGE_CAPTURE -> {
                    var imageBitmap: Bitmap? = null
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(
                                mContext.contentResolver, imageUri)
                        civImage!!.setImageBitmap(imageBitmap)
                        civImage!!.tag = mContext.getString(R.string.txtImageChanged)
                    } catch (e: IOException) {
                        Log.e("REQUEST_IMAGE_CAPTURE", "setBitmap()", e)
                    }
                }
            }
        } else {
            if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_IMAGE_CAPTURE) {
                val rowsDeleted = mContext.contentResolver.delete(imageUri!!, null, null)
                Log.d("PHOTOHELPER", "$rowsDeleted rows deleted")
            }
        }
    }

    fun uploadImage(bitmap: Bitmap?, wishkey: String?, userId: String?, wishViewModel: WishViewModel?, wishlistId: String?, favoriteListId: String?) {
        var reference = wishkey
        if (userId != null) {
            reference = userId
        }
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        val data = baos.toByteArray()
        val intent = Intent(mContext, UploadService::class.java)
        intent.putExtra(UploadService.WISHKEY, wishkey)
        intent.putExtra(UploadService.WISHLISTID, wishlistId)
        intent.putExtra(UploadService.FAVORITELISTID, favoriteListId)
        intent.putExtra(UploadService.REFERENCE, reference)
        intent.putExtra(UploadService.DATA, data)
        UploadService.enqueueWork(mContext, intent)
        Toast.makeText(mContext.applicationContext, R.string.txtSuccessfulSave, Toast.LENGTH_LONG).show()
        flProgressBarHolder!!.visibility = View.GONE
        mContext.finish()
    }

    fun requestProfilePicture(uId: String?) {
        val FIREBASE_STORAGE_BUCKET = "gs://" + mContext.getString(R.string.google_storage_bucket)
        val firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_BUCKET)
        firebaseStorage.getReference(uId!!).downloadUrl.addOnSuccessListener { uri: Uri ->
            Picasso.get()
                    .load(uri.toString())
                    .transform(CircleTransform())
                    .resize(200, 200)
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(mContext.findViewById<View>(R.id.civImage) as CircleImageView)
        }
                .addOnFailureListener { exception: Exception -> Log.e("requestProfilePicture", handleFirebaseStorageExceptions(exception)!!) }
    }

    fun removeImageFromView() {
        mContext.findViewById<View>(R.id.civImage).tag = mContext.getString(R.string.txtImageDeleted)
        mContext.findViewById<View>(R.id.civImage).setBackgroundResource(R.drawable.no_image_available)
        Picasso.get()
                .load(mContext.getDrawable(R.drawable.no_image_available).toString())
                .transform(CircleTransform())
                .resize(200, 200)
                .centerCrop()
                .into(mContext.findViewById<View>(R.id.civImage) as CircleImageView)
    }

    fun deleteImageFromFirebaseStorageFromUrl(photoUrl: String?) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl!!)
        storageReference.delete().addOnSuccessListener { aVoid: Void? -> Log.e("firebasestorage", "onSuccess: deleted file") }.addOnFailureListener { exception: Exception? -> Log.e("firebasestorage", "onFailure: did not delete file") }
    }

    private fun handleFirebaseStorageExceptions(exception: Exception): String? {
        val errorCode = (exception as StorageException).errorCode
        var errorMessage = exception.message
        when (errorCode) {
            StorageException.ERROR_UNKNOWN -> errorMessage += " " + mContext.getString(R.string.txtUnknownError)
            StorageException.ERROR_BUCKET_NOT_FOUND, StorageException.ERROR_CANCELED, StorageException.ERROR_INVALID_CHECKSUM, StorageException.ERROR_NOT_AUTHENTICATED, StorageException.ERROR_NOT_AUTHORIZED, StorageException.ERROR_PROJECT_NOT_FOUND, StorageException.ERROR_QUOTA_EXCEEDED, StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> {
            }
            StorageException.ERROR_OBJECT_NOT_FOUND -> errorMessage += " " + mContext.getString(R.string.txtObjectNotFound)
        }
        return errorMessage
    }

    fun requestProductPicture(photoUrl: String?) {
        Picasso.get()
                .load(Uri.parse(photoUrl))
                .networkPolicy(NetworkPolicy.OFFLINE).stableKey(photoUrl!!)
                .into(mContext.findViewById<View>(R.id.civImage) as CircleImageView)
    }

    inner class ImageDownloader : AsyncTask<String?, Void?, Bitmap?>() {
        override fun doInBackground(vararg urls: String?): Bitmap? {
            try {
                val url = URL(urls[0])
                try {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    val inputStream = connection.inputStream
                    return BitmapFactory.decodeStream(inputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }
    }

    companion object {
        private const val REQUEST_IMAGE_FROM_STORAGE = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }
}