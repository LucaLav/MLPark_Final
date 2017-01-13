/**
 * Copyright 2014 Google Inc. All Rights Reserved.
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;
import java.io.OutputStream;

public class CreateFileInAppFolderActivity extends BaseDemoActivity {
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // create new contents resource
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }
    // [START drive_contents_callback]
    final private ResultCallback<DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle("VERYLastParkPositionMLPARK.txt")
                    .setMimeType("text/plain")
                    .build();

            Log.e("STATUS_CREAZIONE", "Sta per aprire la cartella Root e creare il file");

            Drive.DriveApi.getRootFolder(getGoogleApiClient())
                    .createFile(getGoogleApiClient(), changeSet, result
                            .getDriveContents()).setResultCallback(fileCallback);

        }
    };
    // [END drive_contents_callback]

    final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }

            showMessage("Created a file in Root Folder: "
                    + result.getDriveFile().getDriveId());
            DriveFile file = result.getDriveFile();
            new EditContentsAsyncTask(CreateFileInAppFolderActivity.this).execute(file);
        }
    };
    public class EditContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditContentsAsyncTask(Context context) {
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
                Log.e("Error", "IOException while appending to the output stream", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Error while editing contents");
                return;
            }
            showMessage("File correttamente creato e modificato");
            finish();
        }
    }
}
