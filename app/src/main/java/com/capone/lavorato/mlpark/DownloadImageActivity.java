package com.capone.lavorato.mlpark;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class DownloadImageActivity extends BaseDemoActivity {

    private static final String TAG = "EditImageActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "LPPMLPark_image.jpg"))
                .build();

        Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(queryCallback);
    }

    final ResultCallback<DriveApi.MetadataBufferResult> queryCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
            MetadataBuffer mb = metadataBufferResult.getMetadataBuffer();

            Log.e("Conteggio Buffer", ""+mb.getCount());

            if (mb.getCount() == 0){
                Log.e("Buffer", "VUOTO");
                showMessage("Non è presente alcuna immagine su Drive");
            }else{
                Log.e("Buffer", "PIENO");
                DriveFile myfile = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();

                new RetrieveImageAsyncTask(DownloadImageActivity.this).execute(myfile);
            }
            mb.release();
        }
    };

    public class RetrieveImageAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public RetrieveImageAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {

            DriveFile file = args[0];

            DriveApi.DriveContentsResult driveContentsResult = file.open(
                    getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            Log.e("Resource ID del file passato ad AsyncTask", file.getDriveId().getResourceId());
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            InputStream inputStream = driveContents.getInputStream();

            Intent i = new Intent();
            i.setAction(android.content.Intent.ACTION_VIEW);

            //i.setDataAndType(Uri.fromFile(driveContents.getDriveId().asDriveFile()), "image/*");

            //lancia intent galleria per visualizzare la foto
            startActivity(i);

            //LatLng location = new LatLng(Double.parseDouble(coordinate.nextToken()), Double.parseDouble(coordinate.nextToken()));

            Intent intent = new Intent();

            //intent.putExtra("latitude", Double.parseDouble(coordinate.nextToken()));
            //intent.putExtra("longitude", Double.parseDouble(coordinate.nextToken()));

            setResult(RESULT_OK, intent);

            driveContents.discard(getGoogleApiClient());
            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Error while reading contents");
                return;
            }
            showMessage("File correttamente scaricato");
            finish();
        }
    }
}
