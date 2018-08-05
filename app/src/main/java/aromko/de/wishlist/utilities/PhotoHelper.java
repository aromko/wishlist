package aromko.de.wishlist.utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import aromko.de.wishlist.R;

import static android.app.Activity.RESULT_OK;

public class PhotoHelper {

    private AlertDialog dialog;
    private EditText etDownloadUrl;
    private ImageView ivProduct;
    private Activity mContext;

    static final int REQUEST_IMAGE_FROM_STORAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    public PhotoHelper(Activity mContext) {
        this.mContext = mContext;
    }

    public void startPhotoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View v = mContext.getLayoutInflater().inflate(R.layout.dialog_photo_selection, null);
        etDownloadUrl = v.findViewById(R.id.etDownloadurl);
        builder.setView(v);
        ivProduct = mContext.findViewById(R.id.ivProductImage);
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
            ivProduct.setImageBitmap(myImage);
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
            }
        }
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
