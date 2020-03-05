package com.deb.notify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    //initialize variable
    private GoogleMap mMap;
    LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    Marker marker;
    LocationListener locationListener;
    CheckBox mCheckBox;
    Button btDraw,btClear,btLoc;
    Polygon mPolygon = null;
    List<LatLng> mLatLngs = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    private  static  final int Request_Code=101;


    int red = 0,green =0,blue =0;


    @Override
    protected void onStart() {
        super.onStart();
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (mPolygon == null) return;;

                    mPolygon.setFillColor(Color.rgb(red,green,blue));
                }else {
                    mPolygon.setFillColor(Color.TRANSPARENT);
                }
            }
        });
        btDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPolygon != null) mPolygon.remove();
                PolygonOptions polygonOptions = new PolygonOptions().addAll(mLatLngs).clickable(true);

                mPolygon = mMap.addPolygon(polygonOptions);
                mPolygon.setTag("First Location");
                mPolygon.setStrokeColor(Color.rgb(red,green,blue));
                if(mCheckBox.isChecked())
                {
                    mPolygon.setFillColor(Color.BLACK);
                }
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPolygon != null) mPolygon.remove();
                for(Marker marker:markerList) marker.remove();
                mLatLngs.clear();
                markerList.clear();
                mCheckBox.setChecked(false);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Assign Variable
        mCheckBox = findViewById(R.id.check_box);
        btDraw = findViewById(R.id.bt_draw);
        btClear = findViewById(R.id.bt_clear);
        btLoc = findViewById(R.id.bt_loc);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else{

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LocationHelper helper = new LocationHelper(location.getLongitude(),location.getLatitude());

                    FirebaseDatabase.getInstance().getReference("CurrentLocation").setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("Saved","Location saved");
                            }else {
                                Log.d("Saved","Location not saved");
                            }

                        }
                    });

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //get the location name from latitude and longitude
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addresses =
                                geocoder.getFromLocation(latitude, longitude, 1);
                        String result = addresses.get(0).getLocality()+":";
                        result += addresses.get(0).getCountryName();
                        LatLng latLng = new LatLng(latitude, longitude);
                        if (marker != null){
                            marker.remove();
                            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.fromResource(R.drawable.purple)));


                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                        else{
                            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.fromResource(R.drawable.purple)));


                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LocationHelper helper = new LocationHelper(location.getLongitude(),location.getLatitude());

                    FirebaseDatabase.getInstance().getReference("CurrentLocation").setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MapsActivity.this,"Location Saved",Toast.LENGTH_SHORT);
                                Log.d("Saved","Location saved");
                            }else {
                                Toast.makeText(MapsActivity.this,"Location not saved",Toast.LENGTH_SHORT);
                                Log.d("Saved","Location not saved");
                            }

                        }
                    });
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //get the location name from latitude and longitude
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addresses =
                                geocoder.getFromLocation(latitude, longitude, 1);
                        String result = addresses.get(0).getLocality()+":";
                        result += addresses.get(0).getCountryName();
                        LatLng latLng = new LatLng(latitude, longitude);
                        if (marker != null){
                            marker.remove();
                            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.fromResource(R.drawable.purple)));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                        else{
                            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.fromResource(R.drawable.purple)));


                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(14.0f);
        CameraUpdateFactory.scrollBy(6, 6);
        mMap .setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                int i=1;
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true);
                Marker marker = mMap.addMarker(markerOptions);
                Log.d("Marker","" + latLng.longitude + " " + latLng.latitude);
                mLatLngs.add(latLng);
               markerList.add(marker);
                for(int j=0;j<mLatLngs.size();j++)
                {
                    LatLng latLng1 = new LatLng( mLatLngs.get(j).longitude,  mLatLngs.get(j).latitude);
                    polylocation loc = new polylocation(j + "st point",latLng1.longitude,latLng1.latitude);
                    FirebaseDatabase.getInstance().getReference("Marked Location").child(i+"st polygon").child(j+"points").setValue(loc);

                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

}
