package com.capone.lavorato.mlpark;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MPR = 1;

    //permessi da richiedere
    private String[] all_permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private GoogleMap mMap;
    private LatLng myLocation = new LatLng(0, 0);
    private String FILENAME = "posizione_parcheggio";
    LocationManager lm;
    boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkPermission(all_permissions, MPR);
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Pulsante per segnare il parcheggio
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
                else Toast.makeText(getApplicationContext(),"Localizzazione non pronta", Toast.LENGTH_LONG).show();;
            }
        });

        //Pulsante per recuperare il parcheggio
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

        if (!checkLocation()){
            return;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the yeah case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    //Funzione per aggiungere marcatore
    public void addMarker (LatLng location) throws IOException {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromBitmap(iconScaler(400,400))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) (0.9*mMap.getMaxZoomLevel())));
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

        uploadFile(location.latitude, location.longitude);

        //Conferma a video delle coordinate
        Toast.makeText(this,"Parcheggio Memorizzato", Toast.LENGTH_SHORT).show();

        MyDialog dialog = new MyDialog();
        dialog.show(getFragmentManager(),"123");
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

        LatLng location = new LatLng(Double.parseDouble(coordinate.nextToken()),Double.parseDouble(coordinate.nextToken()));

        Toast.makeText(this,"Parcheggio del "+coordinate.nextToken()+" alle ore "+coordinate.nextToken(), Toast.LENGTH_LONG).show();


        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromBitmap(iconScaler(400,400))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) (0.9*mMap.getMaxZoomLevel())));
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
                        Toast.makeText(getApplicationContext(),"Foto non esistente", Toast.LENGTH_LONG).show();
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
                .setMessage("L'impostazione di localizzazione GPS è settata su 'Off'.\nAbilitare la Localizzazione per " +
                        "usare questa app")
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

    private Bitmap iconScaler(int height, int width){

        BitmapDrawable  bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.poffy_car);

        Bitmap b = bitmapdraw.getBitmap();

        Bitmap bigmarker = Bitmap.createScaledBitmap(b,width,height,false);

        return bigmarker;
    }

    public void downloadFile(View view) {
        Intent intent = new Intent(this, RetrieveFile.class);
        startActivity(intent);
    }


    public void uploadFile(Double latitude, Double longitude) {

        Intent intent = new Intent(this, UploadFile.class);
        intent.putExtra("latitudine", latitude);
        intent.putExtra("longitudine", longitude);
        startActivity(intent);
    }

    public void checkPermission(String[] permission, int request_id){

        if (ContextCompat.checkSelfPermission(this, permission[0]) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission[0])) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    ActivityCompat.requestPermissions(this, permission,request_id);

                    // No explanation needed, we can request the permission.
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MPR:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
