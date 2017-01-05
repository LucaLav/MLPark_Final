package com.capone.lavorato.mlpark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng myLocation;
    private String FILENAME = "posizione_parcheggio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //pulsante per segnare il parcheggio
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    addMarker(myLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        //pulsante per recuperare il parcheggio
        Button button2 = (Button) findViewById(R.id.button2);
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
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
               myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }


            public void onStatusChanged(String provider, int status, Bundle extras){}
            public void onProviderEnabled(String provider){}
            public void onProviderDisabled(String provider){}
        };

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


    }

    //funzione per aggiungere marcatore
    public void addMarker (LatLng location) throws IOException {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("La tua Auto").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) (0.9*mMap.getMaxZoomLevel())));

        String coord = Double.toString(myLocation.latitude)+"\n"+Double.toString(myLocation.longitude);

        //salva file con coordinate
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(coord.getBytes());
        fos.close();

        //conferma a video
        Toast.makeText(this,"posizione parcheggio memorizzata", Toast.LENGTH_SHORT).show();

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
}
