package com.tedngok.classmanagementsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener {

    private GoogleMap mMap;
    private Marker centerMarker;
    private TextView txtAddressName;
    private Button btnSubmit;
    private LatLng current_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtAddressName = (TextView) findViewById(R.id.txtAddressName);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        setAction();
    }

    private void setAction() {


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current_location != null){

                    Intent i =  new Intent();
                    i.putExtra(getString(R.string.jsonLng), current_location.longitude);
                    i.putExtra(getString(R.string.jsonLat), current_location.latitude);
                    i.putExtra(getString(R.string.jsonAddress), txtAddressName.getText().toString());
                    setResult(AddClassActivity.resAddAddressSuccess,i);
                    finish();
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
        LatLng target = null;
        CameraPosition cameraPosition = null;

        if (SearchClassesResultActivity.reqMap == getIntent().getAction()){
            btnSubmit.setVisibility(View.GONE);
            txtAddressName.setVisibility(View.GONE);

            Double lat = getIntent().getDoubleExtra(getString(R.string.jsonLat), 0.0);
            Double lng = getIntent().getDoubleExtra(getString(R.string.jsonLng), 0.0);
            String address_name = getIntent().getStringExtra(getString(R.string.jsonAddress));
            target = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions().position(target).title(address_name));
            cameraPosition = new CameraPosition.Builder()
                    .target(target)
                    .zoom(17)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {

            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraMoveCanceledListener(this);
            mMap.setOnCameraIdleListener(this);
            //tsui wan location
            target = new LatLng(22.3686, 114.1131);

            cameraPosition = new CameraPosition.Builder()
                    .target(target)
                    .zoom(10)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            LatLng current_location = mMap.getCameraPosition().target;
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(current_location)
            );
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onCameraMove() {
        current_location = mMap.getCameraPosition().target;
        centerMarker.setPosition(current_location);
    }

    private void updatePlaceName() {
        txtAddressName.setText(getPlaceName(current_location));
    }

    private String getPlaceName(LatLng location) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String address_name = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            address_name = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return address_name;
    }

    @Override
    public void onCameraMoveCanceled() {
        if (current_location != null) {
            updatePlaceName();
        }
    }

    @Override
    public void onCameraIdle() {
        if (current_location != null) {
            updatePlaceName();
        }
    }
}
