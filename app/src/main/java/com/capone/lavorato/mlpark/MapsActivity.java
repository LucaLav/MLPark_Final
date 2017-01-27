package com.capone.lavorato.mlpark;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import static java.lang.Double.parseDouble;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private LatLng myLocation = new LatLng(0, 0);
    //private Location initLocation;
    private String FILENAME = "posizione_parcheggio";
    private LocationManager lm;
    boolean hasLocation = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int DOWNLOAD_FILE = 2;
    static final int UPLOAD_IMAGE = 3;
    private int counter = 0;
    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //  Intro App Initialize SharedPreferences
        checkFirstRun();
    }

    @Override
    protected void onStart(){
        super.onStart();

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListenerGPS = new LocationListener() { //Listener GPS

            @Override
            public void onLocationChanged(Location location) {
                hasLocation = true;
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,(float) (0.9*mMap.getMaxZoomLevel())));
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        LocationListener locationListenerNet = new LocationListener() { //Listener NETWORK

            @Override
            public void onLocationChanged(Location location) {
                hasLocation = true;
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);

        //Per mostrare la mappa di Google Maps a schermo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Pulsante per memorizzare il parcheggio e la foto, ed effettuare l'upload su Drive
        ImageButton button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (hasLocation){
                    try {
                        addMarker(myLocation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(getApplicationContext(),R.string.errore_localizzazione, Toast.LENGTH_LONG).show();;

            }
        });

        //Pulsante per recuperare la posizione in locale
        ImageButton button2 = (ImageButton) findViewById(R.id.imageButton2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    retrieveMarker();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Pulsante per recuperare la posizione da Drive
        ImageButton button3 = (ImageButton) findViewById(R.id.imageButton3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();
            }
        });

        //Pulsante per recupero foto da Drive
        ImageButton button5 = (ImageButton) findViewById(R.id.imageButton5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences fotoDrive = getSharedPreferences("fotoDrive", MODE_PRIVATE);
                if (fotoDrive.getBoolean("fotoDrive",true))
                {
                    Intent imageFromDrive = new Intent(getApplicationContext(), DownloadImageActivity.class);
                    startActivity(imageFromDrive);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Foto non presente su Drive", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Pulsante per info App
        ImageButton button4 = (ImageButton) findViewById(R.id.imageButton4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MapsActivity.this)
                        .setTitle(R.string.info_dialog_title)
                        .setMessage(R.string.info_dialog_text);
                builder.create();
                builder.show();
            }
        });

        ImageButton button6 = (ImageButton) findViewById(R.id.imageButton6);
        button6.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                counter = 0;
                RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.CENTER_IN_PARENT);
                showcaseView = new ShowcaseView.Builder(MapsActivity.this)
                        .setTarget(new ViewTarget(findViewById(R.id.imageButton)))
                        .setContentTitle("Setta Parcheggio")
                        .setContentText("Premi per salvare il parcheggio in locale")
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (counter) {
                                    case 0:
                                        showcaseView.setShowcase(new ViewTarget(findViewById(R.id.imageButton2)), true);
                                        showcaseView.setContentTitle("Recupera Parcheggio");
                                        showcaseView.setContentText("Premi per recuperare il parcheggio in locale");
                                        showcaseView.setButtonText("Avanti");
                                        break;

                                    case 1:
                                        showcaseView.setShowcase(new ViewTarget(findViewById(R.id.imageButton3)), true);
                                        showcaseView.setContentTitle("Recupera Coordinate Drive");
                                        showcaseView.setContentText("Premi per recuperare le coordinate da Google Drive");
                                        showcaseView.setButtonText("Avanti");
                                        break;

                                    case 2:
                                        showcaseView.setShowcase(new ViewTarget(findViewById(R.id.imageButton5)), true);
                                        showcaseView.setContentTitle("Recupera Foto Drive");
                                        showcaseView.setContentText("Premi per recuperare la foto da Google Drive");
                                        showcaseView.setButtonText("Avanti");
                                        break;

                                    case 3:
                                        showcaseView.setShowcase(new ViewTarget(findViewById(R.id.imageButton4)), true);
                                        showcaseView.setContentTitle("Info App");
                                        showcaseView.setContentText("Premi per visualizzare le info");
                                        showcaseView.setButtonText("Avanti");
                                        break;

                                    case 4:
                                        showcaseView.setShowcase(new ViewTarget(findViewById(R.id.imageButton6)), true);
                                        showcaseView.setContentTitle("Aiuto");
                                        showcaseView.setContentText("Puoi premere qui per rivedere le istruzioni");
                                        showcaseView.setButtonText("Ok");
                                        break;
                                    case 5:
                                        showcaseView.hide();
                                        break;
                                }
                                counter++;
                            }

                        })
                        .build();
                showcaseView.setButtonPosition(lps);
                showcaseView.setButtonText("Avanti");
                showcaseView.setBlocksTouches(true);
            }
        });

        if (!checkLocation()){
            return;
        }
    }

    public void uploadImageFile(File photo) {

        Intent imgToDrive = new Intent(this, EditImageActivity.class);
        imgToDrive.putExtra("imageFile",photo);

        startActivityForResult(imgToDrive,UPLOAD_IMAGE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }/* else {
            initLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            LatLng init_loc = new LatLng(initLocation.getLatitude(),initLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(init_loc,(float) (0.75*mMap.getMaxZoomLevel())));
        }*/
    }

    //Funzione per aggiungere marcatore
    public void addMarker (LatLng location) throws IOException {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromBitmap(iconScaler(400,400))));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,(float) (0.9*mMap.getMaxZoomLevel())));

        recuperaFotoListener();

        //Prende la data in quel momento e la salva insieme alle coordinate
        SimpleDateFormat orario_parcheggio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar c = Calendar.getInstance();
        String parkdate = orario_parcheggio.format(c.getTime());
        String coordEparkdate = Double.toString(location.latitude)+"\n"+Double.toString(location.longitude)+"\n"+parkdate;

        //Salva file con coordinate
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(coordEparkdate.getBytes());
        fos.close();

        //upload file localizzazione su Drive
        uploadFile(location.latitude, location.longitude);

        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {

        File photoFile = null;
        //INTENT CREAZIONE FOTO NUOVA
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    //ELIMINA FOTO VECCHIA
    private void deleteFoto(){

        File Dirvecchiafoto = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(Dirvecchiafoto.isDirectory()){

            String[] children = Dirvecchiafoto.list();

            for (int i = 0; i < children.length; i++)
            {
                new File(Dirvecchiafoto, children[i]).delete();
            }

        }

    }

    //Crea File Immagine
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File (storageDir, "lastpark.jpg");

        return image;
    }

    //Funzione che recupera marcatore
    public void retrieveMarker() throws IOException {
        int n = 0;
        FileInputStream fis;
        fis = openFileInput(FILENAME);
        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        while ((n = fis.read(buffer)) != -1)
        {
            fileContent.append(new String(buffer, 0, n));
        }

        StringTokenizer coordinate = new StringTokenizer(fileContent.toString());

        LatLng location = new LatLng(parseDouble(coordinate.nextToken()), parseDouble(coordinate.nextToken()));
        Toast.makeText(this,"Parcheggio del "+coordinate.nextToken()+" alle ore "+coordinate.nextToken(), Toast.LENGTH_LONG).show();

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromBitmap(iconScaler(400,400))));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,(float) (0.9*mMap.getMaxZoomLevel())));
        recuperaFotoListener(); //funzione che recupera la foto dal Marker (solo se esiste)
    }

    //LISTENER PER RECUPERARE LA FOTO DAL MARKER
    public void recuperaFotoListener(){

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //Recupera percorso foto
                File DirFoto = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if(DirFoto.isDirectory()) {

                    String[] children = DirFoto.list();

                    if (children.length!=0) {
                        File parkImage = new File((DirFoto.toString()) + '/' + children[0]);
                        Intent i = new Intent();
                        i.setAction(android.content.Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(parkImage), "image/*");

                        //lancia intent galleria per visualizzare la foto
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),R.string.errore_foto, Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }

    private boolean isLocationEnabled() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    //Alert che ti invita ad attivare la localizzazione se disattivata (check location)
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Abilita Localizzazione")
                .setMessage(R.string.errore_localizzazione_disattivata)
                .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    //funzione per ingrandire marker e personalizzarlo
    private Bitmap iconScaler(int height, int width){

        BitmapDrawable  bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.marker_car);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap bigmarker = Bitmap.createScaledBitmap(b,width,height,false);

        return bigmarker;
    }

    public void downloadFile() {

        Intent intent = new Intent(this, DownloadContentsActivity.class);
        startActivityForResult(intent, DOWNLOAD_FILE);

    }

    public void uploadFile(Double latitude, Double longitude) {

        Intent intent = new Intent(this, EditContentsActivity.class);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_FILE && resultCode == RESULT_OK && data != null) {

            LatLng ll = new LatLng(data.getDoubleExtra("latitude", 0.0),data.getDoubleExtra("longitude", 0.0));

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(ll).title("La tua auto(i)").icon(BitmapDescriptorFactory.fromBitmap(iconScaler(400,400))));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll,(float) (0.9*mMap.getMaxZoomLevel())));
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            SharedPreferences fotoDrive = getSharedPreferences("fotoDrive", MODE_PRIVATE);
            fotoDrive.edit().putBoolean("fotoDrive",true).apply();

            File[] listFiles= getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
            uploadImageFile(listFiles[0]);
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            SharedPreferences fotoDrive = getSharedPreferences("fotoDrive", MODE_PRIVATE);
            fotoDrive.edit().putBoolean("fotoDrive",false).apply();

            deleteFoto();
        }

        if(requestCode == UPLOAD_IMAGE){//&& resultCode == RESULT_OK) {

            //findViewById(R.id.loadingPanel).setVisibility(View.GONE); //animazione caricamento

        }

    }

    private void checkFirstRun() {

        final String PREFS_NAME = "AppPreference";
        final String PREF_VERSION_CODE_KEY = "versionCode";
        final int DOESNT_EXIST = -1;


        // Get current version code
        int currentVersionCode = 0;
        try {
            Log.e("VersionCode", "Sono nella try");
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Retrieve version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check primo avvio
        if (currentVersionCode == savedVersionCode) {
            // Avvio normale
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            // Primo avvio, schermata di intro
            Intent i = new Intent(getApplicationContext(), SlideShow.class);
            startActivity(i);


        } else if (currentVersionCode > savedVersionCode) {
            Intent i = new Intent(getApplicationContext(), SlideShow.class);
            startActivity(i);

        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();

    }

}
