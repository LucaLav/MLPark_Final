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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class CreateImageActivity extends BaseDemoActivity {
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
                            .setTitle("LPPMLPark_image.jpg")
                            .setMimeType("image/jpeg")
                            .build();

                    Log.e("STATUS_CREAZIONE IMG", "Sta per aprire la cartella Root e creare il file");

                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, result
                                    .getDriveContents()).setResultCallback(imgCallback);

                }
            };
    // [END drive_contents_callback]

    final private ResultCallback<DriveFileResult> imgCallback = new ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }

            showMessage("Created a file in Root Folder: "
                    + result.getDriveFile().getDriveId());
            DriveFile file = result.getDriveFile();
            new EditImageAsyncTask(CreateImageActivity.this).execute(file);
        }
    };

    public class EditImageAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditImageAsyncTask(Context context) {
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

                Log.e("ATTENZIONE", "sto in task async immagine");

                //Bitmap image = getIntent().getParcelableExtra("imageFile");
                File immagine = (File)getIntent().getExtras().get("imageFile");
                Bitmap image = BitmapFactory.decodeFile(immagine.getPath());

                DriveContents driveContents = driveContentsResult.getDriveContents();
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
                OutputStream outputStream = driveContents.getOutputStream();
                outputStream.write((bitmapStream.toByteArray()));
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
