package com.capone.lavorato.mlpark;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
/**
 * upload a text file from device to Drive */
public class UploadFile extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "upload_file";
    private static final int REQUEST_CODE = 101;
    private File textFile;
    private GoogleApiClient googleApiClient;
    public static String drive_id;
    public static DriveId driveID;

    double latitude;// = getIntent().getDoubleExtra("latitudine", 0.0);
    double longitude;// = getIntent().getDoubleExtra("longitudine", 0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_file);

        latitude = getIntent().getDoubleExtra("latitudine", 0.0);
        longitude = getIntent().getDoubleExtra("longitudine", 0.0);

        Log.i(TAG, "Legal requirements if you use Google Drive in your app: "
                + GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(this)
                //GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this)
        );
        //the text file in our device's Download folder
        textFile = new File(Environment.getExternalStorageDirectory()
                + File.separator + "Download" + File.separator + "test.txt");
        /*build the api client*/
        buildGoogleApiClient();
    }

    /*connect client to Google Play Services*/
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "In onStart() - connecting...");
        googleApiClient.connect();
    }
    @Override
    protected void onResume(){
        super.onResume();
        googleApiClient.connect();
    }
    /*close connection to Google Play Services*/
    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            Log.i(TAG, "In onStop() - disConnecting...");
            googleApiClient.disconnect();
        }
    }

    /*Handles onConnectionFailed callbacks*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Sono in onActivityResult: REQUEST_CODE:" + requestCode +"Expected: "+REQUEST_CODE+ " RESULT_OK: " + resultCode + "Expected: " + RESULT_OK);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "In onActivityResult() - connecting...");
            googleApiClient.connect();
        }
    }

    /*handles connection callbacks*/
    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "in onConnected() - we're connected, let's do the work in the background...");
        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    /*handles suspended connection callbacks*/
    @Override
    public void onConnectionSuspended(int cause) {
        switch (cause) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }

    /*callback on getting the drive contents, contained in result*/
    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
                @Override
                public void onResult(DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();
                    new Thread() {
                        @Override
                        public void run() {
                            OutputStream outputStream = driveContents.getOutputStream();
                            addTextfileToOutputStream(outputStream);
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("coordinate")
                                    .setMimeType("text/plain")
                                    .setDescription("This is a coordinate file uploaded MLPark").setStarred(true).build();
                            Drive.DriveApi.getRootFolder(googleApiClient).createFile(googleApiClient, changeSet, driveContents).setResultCallback(fileCallback);
                        }

                    }.start();
                }
            };

    /*get input stream from text file, read it and put into the output stream*/
    private void addTextfileToOutputStream(OutputStream outputStream) {
        Log.i(TAG, "adding text file to outputstream...");
        String coord = Double.toString(latitude)+"\n"+Double.toString(longitude);
        //byte[] buffer = new byte[1024];
        //int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(textFile));
            //while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(coord.getBytes());//buffer, 0, bytesRead);
            //}
        } catch (IOException e) {
            Log.i(TAG, "problem converting input stream to output stream: " + e);
            e.printStackTrace();
        }
    }

    /*callback after creating the file, can get file info out of the result*/
    final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
                @Override
                public void onResult(DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating the file");
                        Toast.makeText(UploadFile.this,
                                "Error adding file to Drive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG, "File added to Drive");
                    Log.i(TAG, "Created a file with content: "
                            + result.getDriveFile().getDriveId());
                    Toast.makeText(UploadFile.this,
                            "File successfully added to Drive", Toast.LENGTH_SHORT).show();
                    final PendingResult<DriveResource.MetadataResult> metadata = result.getDriveFile().getMetadata(googleApiClient);
                    metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                        @Override
                        public void onResult(DriveResource.MetadataResult metadataResult) {
                            Metadata data = metadataResult.getMetadata();
                            Log.i(TAG, "Title: " + data.getTitle());
                            drive_id = data.getDriveId().encodeToString();
                            Log.i(TAG, "DrivId: " + drive_id);
                            driveID = data.getDriveId();
                            Log.i(TAG, "Description: " + data.getDescription().toString());
                            Log.i(TAG, "MimeType: " + data.getMimeType());
                            Log.i(TAG, "File size: " + String.valueOf(data.getFileSize()));
                        }
                    });
                }

            };

    /*callback when there there's an error connecting the client to the service.*/
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed");
        if (!result.hasResolution()) {
            Log.i(TAG, "NON C'E SOLUZIONE");
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0);
            //GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            Log.i(TAG, "trying to resolve the Connection failed error...");
            result.startResolutionForResult(this, REQUEST_CODE);
            Log.i(TAG, "Start Resolution for Result ha finito   "+result.isSuccess());

        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /*build the google api client*/
    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        }
    }
}