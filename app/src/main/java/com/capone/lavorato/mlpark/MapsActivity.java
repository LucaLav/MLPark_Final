package com.capone.lavorato.mlpark;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.Calendar;
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
import android.support.v7.app.AlertDialog;
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
import java.util.StringTokenizer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private LatLng myLocation = new LatLng(0, 0);
    private String FILENAME = "posizione_parcheggio";
    LocationManager lm;
    boolean hasLocation = false;
    boolean ready = false;
    private class LocationBackground extends AsyncTask<Context, Void, Void> {

        protected Void doInBackground(Context... params){
            //Wait 10 seconds to see if we can get a location from either network or GPS, otherwise stop
            Calendar cal = Calendar.getInstance();
            Long t = cal.getTimeInMillis();
            while (!hasLocation || !ready){//Calendar.getInstance().getTimeInMillis() - t < 15000) {
                try {
                    Log.i("Attesa", "Aspettiamo la posizione");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            return null;
        }
        protected void onPostExecute(Void val) {
            try {
                addMarker(myLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    LocationBackground lb = new LocationBackground();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb.execute(this);
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_maps);

        LocationListener locationListenerGPS = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                hasLocation = true;
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                long time = location.getTime();
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        LocationListener locationListenerNet = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                hasLocation = true;
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                long time = location.getTime();
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
        //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
        //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //pulsante per segnare il parcheggio

        ImageButton button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try {
                    ready = true;

                    /*if (hasLocation){
                        addMarker(myLocation);
                    }else Toast.makeText(getApplicationContext(),"Localizzazione fallita", Toast.LENGTH_LONG).show();*/
                /*} catch (IOException e) {
                    e.printStackTrace();
                }*/


            }
        });

        //pulsante per recuperare il parcheggio
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

    //funzione per aggiungere marcatore
    public void addMarker (LatLng location) throws IOException {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) (0.9*mMap.getMaxZoomLevel())));

        //PROVA LISTENER SU MARKER
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
                        Toast.makeText(getApplicationContext(),"Foto non Presente", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        //FINE PROVA LISTENER SU MARKER

        String coord = Double.toString(myLocation.latitude)+"\n"+Double.toString(myLocation.longitude);

        //salva file con coordinate
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(coord.getBytes());
        fos.close();

        //conferma a video
        Toast.makeText(this,"Parcheggio Memorizzato", Toast.LENGTH_SHORT).show();

        MyDialog dialog = new MyDialog();
        dialog.show(getFragmentManager(),"123");
    }

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

        Toast.makeText(this,fileContent, Toast.LENGTH_LONG).show();

        StringTokenizer coordinate = new StringTokenizer(fileContent.toString());

        LatLng location = new LatLng(Double.parseDouble(coordinate.nextToken()),Double.parseDouble(coordinate.nextToken()));

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto (i)").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) (0.9*mMap.getMaxZoomLevel())));

        //Listener sul Marker
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

}
