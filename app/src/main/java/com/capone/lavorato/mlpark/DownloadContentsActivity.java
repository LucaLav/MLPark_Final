package com.capone.lavorato.mlpark;

import android.content.Context;
import android.content.Intent;
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
import java.util.StringTokenizer;

/**
 * An activity to illustrate how to edit contents of a Drive file.
 */
public class DownloadContentsActivity extends BaseDemoActivity {

    private static final String TAG = "EditContentsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "VERYLastParkPositionMLPARK.txt"))
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
                showMessage("Non è presente alcuna posizione su Drive");
            }else{
                Log.e("Buffer", "PIENO");
                DriveFile myfile = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();

                new RetrieveContentsAsyncTask(DownloadContentsActivity.this).execute(myfile);
            }
            mb.release();
        }
    };

    public class RetrieveContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public RetrieveContentsAsyncTask(Context context) {
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
            int n = 0;
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];

            try {
                while ((n = inputStream.read(buffer)) != -1)
                {
                    fileContent.append(new String(buffer, 0, n));
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException while reading from the stream");
            }
            StringTokenizer coordinate = new StringTokenizer(fileContent.toString());

            //LatLng location = new LatLng(Double.parseDouble(coordinate.nextToken()), Double.parseDouble(coordinate.nextToken()));

            Intent intent = new Intent();

            intent.putExtra("latitude", Double.parseDouble(coordinate.nextToken()));
            intent.putExtra("longitude", Double.parseDouble(coordinate.nextToken()));

            //getIntent().putExtra("latitude", Double.parseDouble(coordinate.nextToken()));
            //getIntent().putExtra("longitude", Double.parseDouble(coordinate.nextToken()));
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
            showMessage("File correttamente modificato");
        }
    }
}
