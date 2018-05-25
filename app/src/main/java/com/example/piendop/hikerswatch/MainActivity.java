package com.example.piendop.hikerswatch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //global variables
    LocationManager locationManager;//location manager is used to manage location
    LocationListener locationListener;//location listener is used to listen to the change in location
    //and do action based on it

    //prepare a thing to show when user deny or allow to get users' location
    //use onRequestPermissionResult method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // parameter: request code: number of requests
        //permission and result of the request

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //check the request that is accepted or not
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
            startListening();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*segment code to get user's location*/
        //this is main activity class, LocationManager manage the location
        locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Location listener listen to changes in location
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Since MarshMallow we need users' permission for location

        //check if device run SDK<23
        if(Build.VERSION.SDK_INT<23){
            startListening();
        }else{

            //first we start checking if we have a permission
            //==> use class ContextCompact to check (context,permission)
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                //ASK FOR PERMISSION
                //ActivityCompat.requestPermission(context, String array message, number of requests)
                //just ask 1 time so 1 in parameter
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            }else{
                //we have a permission so need to check or its the first time we open the app
                startListening();
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location!=null)
                    updateLocation(location);
            }
        }
    }

    private void updateLocation(Location location) {
        /*Convert coordinates to address*/
        //create a geocoder object
        Geocoder geocoder;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        //create a list of addresses
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            //check if list is empty or not ==> use try catch
            if(addressList != null && addressList.size()>0){
                //get the address of hiker
                Address address = addressList.get(0);
                //find text views of latitude, longitude and address
                TextView latitude = findViewById(R.id.latitude);
                TextView longitude= findViewById(R.id.longitude);
                TextView addressInfo = findViewById(R.id.address);
                //set text to latitude longitude and address
                latitude.setText("Latitude: "+address.getLatitude());
                longitude.setText("Longitude: "+address.getLongitude());
                addressInfo.setText("Address: "+address.getAddressLine(0));
                //find text view of altitude and accuracy
                TextView altitude = findViewById(R.id.altitude);
                TextView accuracy = findViewById(R.id.accuracy);
                /*Segment code to get altitude*/
                LocationProvider locationProvider;//create a location provider for locationManager

                locationProvider=locationManager.getProvider(LocationManager.GPS_PROVIDER);//location provider
                // is the provider of locationManager

                locationProvider.supportsAltitude();//make the location provider support altitude

                altitude.setText("Altitude: "+location.getAltitude());
                accuracy.setText("Accuracy: "+locationProvider.getAccuracy());
            }else if(addressList.size()==0){//sometimes geocode cannot get address
                //location is some places on the sea randomly
                TextView addressInfo = findViewById(R.id.address);
                addressInfo.setText("Could not find address");
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void startListening() {
        //if we have a permission, we need to hear a location
        //==> use location manager ==> make it global
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
    }
}
