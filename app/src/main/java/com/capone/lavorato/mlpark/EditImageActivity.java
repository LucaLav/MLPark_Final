package com.capone.lavorato.mlpark;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by luca on 14/01/17.
 */

public class EditImageActivity extends BaseDemoActivity {
    private static final String TAG = "EditImageActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        Query query_img = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "LPPMLPark_image.jpg"))
                .build();

        Drive.DriveApi.query(getGoogleApiClient(), query_img).setResultCallback(queryImgCallback);
    }

    final ResultCallback<DriveApi.MetadataBufferResult> queryImgCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
            MetadataBuffer mb = metadataBufferResult.getMetadataBuffer();

            Log.e("Conteggio Buffer", ""+mb.getCount());

            if (mb.getCount() == 0){
                Log.e("Buffer", "VUOTO");
                Intent intent = new Intent(getApplicationContext(), CreateImageActivity.class);
                intent.putExtra("imageFile", getIntent().getParcelableExtra("imageFile"));
                startActivity(intent);
            }else{
                Log.e("Buffer", "PIENO");
                DriveFile myfile = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();
                new EditImageActivity.EditImageAsyncTask(EditImageActivity.this).execute(myfile);
            }
            mb.release();
        }
    };

    public class EditImageAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditImageAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            Log.e("Resource ID del file passato ad AsyncTask", file.getDriveId().getResourceId());
            try {
                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                Log.e("Status", ""+driveContentsResult.getStatus()
                );
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                Bitmap image = getIntent().getParcelableExtra("imageFile");
                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                outputStream.write((bitmapStream.toByteArray()));

                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();


                Log.e("STATUS_CREAZIONE", "Sta per aprire la cartella Root e creare il file");

                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Errore nell'upload dell'immagine");
                return;
            }
            showMessage("File correttamente modificato");
            finish();
        }
    }


}
