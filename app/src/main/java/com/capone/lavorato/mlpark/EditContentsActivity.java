/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.capone.lavorato.mlpark;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

public class EditContentsActivity extends BaseDemoActivity {

    private static final String TAG = "EditContentsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);}
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Query query_txt = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "VERYLastParkPositionMLPARK.txt"))
                .build();

        Drive.DriveApi.query(getGoogleApiClient(), query_txt).setResultCallback(queryTxtCallback);
    }

    final ResultCallback<DriveApi.MetadataBufferResult> queryTxtCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
            MetadataBuffer mb = metadataBufferResult.getMetadataBuffer();

            if (mb.getCount() == 0){
                Log.e("Buffer", "VUOTO");
                Intent intent = new Intent(getApplicationContext(), CreateFileInAppFolderActivity.class);
                intent.putExtra("latitude",getIntent().getDoubleExtra("latitudine",0.0));
                intent.putExtra("longitude",getIntent().getDoubleExtra("longitudine",0.0));

                startActivity(intent);
            }else{
                Log.e("Buffer", "PIENO");
                DriveFile myfile = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();
                new EditCoordinatesAsyncTask(EditContentsActivity.this).execute(myfile);
            }
            mb.release();
        }
    };

    public class EditCoordinatesAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditCoordinatesAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            try {
                DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();

                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                outputStream.write((getIntent().getDoubleExtra("latitude", 0.0)+"\n"+getIntent().getDoubleExtra("longitude", 0.0)).getBytes());

                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();

                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Errore durante l'upload del file");
                return;
            }
            showMessage("File correttamente modificato");
            finish();
        }
    }
}
