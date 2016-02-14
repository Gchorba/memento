package com.example.gene.helper;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GpsActivity extends Activity implements LocationListener {
 
        TextView t1, t2, t3, t4;
        EditText e1, e2, e3;
        Button b1;
        LocationManager locationManager;
        Location location;
        String bestProvider = "", reversedLocation = "", address;
        Geocoder geocoder, reverseGeocoder;
        AlarmManager alarmManager;
        private double r; // km
        private double longitude, latitude, longitudeChecked, latitudeChecked, longitudeVar, latitudeVar,
        dLatitude, dLongitude, distance, longitudeReversed, latitudeReversed;
        Context context = GpsActivity.this;
        boolean canGetLocation = false;
        boolean isGpsEnabled = false;
        boolean isNetworkEnabled = false;
        private DatabaseManager dbManager;
        private SQLiteDatabase db;
        private boolean onoff;
        private static final String DROPOFF_ADDR = "One Embarcadero Center, San Francisco";
        private static final float DROPOFF_LAT = 37.795079f;
        private static final float DROPOFF_LONG = -122.397805f;
        private static final String DROPOFF_NICK = "Boston";
        private static final String PICKUP_ADDR = "1455 Market Street, San Francisco";
        private static final float PICKUP_LAT = 37.775304f;
        private static final float PICKUP_LONG = -122.417522f;
        private static final String PICKUP_NICK = "Fidelity Investor Center";
        private static final String UBERX_PRODUCT_ID = "a1111c8c-c720-46c3-8534-2fcdd730040d";

        private static final long MIN_TIME = 1000 * 10; // 10 seconds
        private static final long MIN_DISTANCE = 100; // meters

 
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_gps);
 
                t1 = (TextView) findViewById(R.id.gps_TextView1);
                t2 = (TextView) findViewById(R.id.gps_TextView2);
                t3 = (TextView) findViewById(R.id.gps_TextView3);
                t4 = (TextView) findViewById(R.id.gps_TextView4);
                b1 = (Button)   findViewById(R.id.gps_btn1);

                String clientId = getString(R.string.client_id);
                if (clientId.equals("insert_your_client_id_here")) {
                        throw new IllegalArgumentException("Please enter your client ID in client_id in res/values/strings.xml");
                }

                RequestButton uberButtonBlack = (RequestButton) findViewById(R.id.uber_button_black);

                RideParameters rideParameters = new RideParameters.Builder()
                        .setProductId(UBERX_PRODUCT_ID)
                        .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                        .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                        .build();

                uberButtonBlack.setRideParameters(rideParameters);

                //geocoder is for getting address from coordinates
                //reverseGeocoder is for getting coordinates from address
                geocoder = new Geocoder(this, Locale.getDefault());
                reverseGeocoder = new Geocoder(this, Locale.getDefault());
 
                getDataDB();
 
                t4.setText("History: \n");
                
                onoff = true;
                // on click start checking location
                b1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	if (onoff){
                                b1.setText(R.string.main_j2);
                        		onoff = false;
                        		startGettingLocation();
                        	}
                        	else{
                        		onoff = true;
                        		stopUsingGps();
                        		Intent i = new Intent(getApplicationContext(), MainActivity.class);
            			        startActivity(i);                      		
                        	}
                            
                        }                      
                });
        }
 
        // get radius from DB
        public void getDataDB(){
                dbManager = new DatabaseManager(this);
                db = dbManager.getReadableDatabase();
 
                String[] kolumny = {"address","range"};
                Cursor cursor = db.query("startpoint", kolumny, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                        address = cursor.getString(0);
                        r = Double.parseDouble(cursor.getInt(1) + "")/1000;
                }
                cursor.close();
                db.close();
                addressToLatLon(address);
        }
 
        // send SMS to all contacts in database
        public void sendSms (String message) {
                       
                SmsManager sms = null;
                sms = SmsManager.getDefault();
               
                SQLiteDatabase db = dbManager.getReadableDatabase();
                String kolumny[] = {"telefon"+""};
                Cursor cursor = db.query("telefony", kolumny, null, null, null, null, null);
                if(cursor.moveToFirst()){
                        do {
                                sms.sendTextMessage((cursor.getString(0)), null, message, null, null);
                        } while(cursor.moveToNext());
                }
                cursor.close();
                db.close();
        }
       
        // translate string contains address to lat/lon
        public void addressToLatLon(String address){
                double[] temp = new double[2];
                temp = translateReverse(address);
                latitudeChecked = temp[0];
                longitudeChecked = temp[1];
        }
 
        // method called on every location update
        @Override
        public void onLocationChanged(Location location) {
                getLocation();
                try {
                    Thread.sleep(1000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                updateTextViews();
                checkLocation();
                translateLongitude();              
        }
 
        // check if user is in proper area
        public void checkLocation(){  
                latitudeVar = latitudeChecked; 
                longitudeVar = longitudeChecked;
 
                longitude = longitude * 111.32;
                longitude = longitude * Math.cos(latitude);
                latitude *= 110.54;
                longitudeVar = longitudeVar * 111.32 * Math.cos(latitudeVar);
                latitudeVar *= 110.54;
 
                dLatitude = latitude - latitudeVar;
                dLongitude = longitude - longitudeVar;
 
                distance = Math.sqrt(Math.pow(dLatitude, 2) + Math.pow(dLongitude, 2));
                if (distance > r){
                        Toast.makeText(getApplicationContext(), "Out of range, distance from center: " + String.valueOf((int) (distance * 1000)), Toast.LENGTH_LONG).show();
                        sendSms("Out of range, distance from center: " + String.valueOf((int) (distance * 1000)));
                }
                else {
                        Toast.makeText(getApplicationContext(), "In range, distance from center: " + String.valueOf((int) (distance * 1000)), Toast.LENGTH_LONG).show();
                }
        }
 
        // convert address to coordinates
        public double[] translateReverse(String reversedLocation){
 
                try{
                        List<Address> reverseAddresses = reverseGeocoder.getFromLocationName(reversedLocation, 1);
                        if(reverseAddresses.size() > 0) {
                                latitudeReversed = reverseAddresses.get(0).getLatitude();
                                longitudeReversed = reverseAddresses.get(0).getLongitude();
                        }
                }
                catch (IOException e) {
                        Toast.makeText(getApplicationContext(),
                                e.toString(),
                                Toast.LENGTH_LONG).show();
                }
 
                double[] ret = new double[2];
                ret[0] = latitudeReversed;
                ret[1] = longitudeReversed;
 
                return ret;
        }
 
        // get actual coordinates from gps or network
        public Location getLocation(){
                try{
                        if(isNetworkEnabled){
                                bestProvider = "network";
                                if(locationManager != null){
                                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                }
                                if(location != null){
                                        longitude = location.getLongitude();
                                        latitude = location.getLatitude();
                                }
                        }
                        if(isGpsEnabled){
                                bestProvider = "gps";
                                if(locationManager != null){
                                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                }
                                if(location != null){
                                        longitude = location.getLongitude();
                                        latitude = location.getLatitude();
                                }
                        }
                }
                catch(Exception e){
                        e.printStackTrace();
                }
 
                return location;
        }
 
        // get coordinates the first time and set location updates
        public void startGettingLocation(){
                try{
                        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
                        if( isGpsEnabled || isNetworkEnabled){
                                this.canGetLocation = true;
 
                                if(isNetworkEnabled){
                                        bestProvider = "network";
                                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                                }
                                if(isGpsEnabled){
                                        bestProvider = "gps";
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                                }
                        }
                }
                catch(Exception e){
                        e.printStackTrace();
                }
        }
 
        // update text views, for user information and debug
        public void updateTextViews(){
 
                t1.setText("Provider: " + bestProvider);
                t2.setText("Latitude: " + getLongitude());
                t3.setText("Longitude: " + getLatitude()	);
 
                t4.setText(t4.getText() + "" + getLongitude() + " / " + getLatitude() + "\n");
 
        }
 
        // convert coordinates to address
        public void translateLongitude(){
 
                List<Address> addresses = null;
                try {
                        addresses = geocoder.getFromLocation(getLatitude(), getLongitude(), 1);
                        String result = "Address: " + "\n";
 
                        if (addresses != null && addresses.size() > 0){
                                Address address = addresses.get(0);
 
                                for(int i = 0; i < address.getMaxAddressLineIndex(); i++){
                                        result += address.getAddressLine(i) + "\n";
                                }
                                result += "\n";
                                t4.setText(t4.getText() + result + "\n");
                        }
                        else{
                                t4.setText(t4.getText() + "Can't find address \n");
                        }
                }
                catch (IOException e) {
                        Toast.makeText(getApplicationContext(),
                                e.toString(),
                                Toast.LENGTH_LONG).show();
                }
 
        }
 
        // turn off checking location
        public void stopUsingGps(){
                if(locationManager != null){
                        locationManager.removeUpdates(GpsActivity.this);
                }
        }
 
        // get longitude from current location
        public double getLongitude(){
                if(location != null){
                        longitude = location.getLongitude();
                }
                return longitude;
        }
 
        // get latitude from current location
        public double getLatitude(){
                if(location != null){
                        latitude = location.getLatitude();
                }
                return latitude;
        }
 
        // check if can get location from any provider
        public boolean canGetLocation(){
                return this.canGetLocation;
        }
 
        // need to override these 3 methods to implement LocationListener
        @Override
        public void onProviderDisabled(String arg0) {
                // Auto-generated method stub
        }
 
        @Override
        public void onProviderEnabled(String arg0) {
                // Auto-generated method stub
        }
 
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                // Auto-generated method stub
        }
 
}