package com.capone.lavorato.mlpark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class MyDialog extends DialogFragment{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Posizione Memorizzata correttamente")
                .setMessage("Vuoi scattare una foto all'auto parcheggiata per ritrovarla più semplicemente?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Apri fotocamera e scatta foto
                dispatchTakePictureIntent();
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Elimina foto scattate in precedenza
                deleteFoto();
            }
        });

        return builder.create();
    }

    //FOTO - INTENT che gestisce
    private void dispatchTakePictureIntent() {

        //Elimina foto scattate in precedenza
        deleteFoto();

        File photoFile = null;
        //INTENT CREAZIONE FOTO NUOVA
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this.getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    //Crea File Immagine
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File (storageDir, "lastpark.jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //ELIMINA FOTO VECCHIA
    private void deleteFoto(){

        File Dirvecchiafoto = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(Dirvecchiafoto.isDirectory()){

            String[] children = Dirvecchiafoto.list();

            for (int i = 0; i < children.length; i++)
            {
                new File(Dirvecchiafoto, children[i]).delete();
            }

        }

    }
}
