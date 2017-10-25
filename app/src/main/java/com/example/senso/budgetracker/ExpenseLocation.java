package com.example.senso.budgetracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.Manifest;

import java.io.IOException;
import java.util.List;

public class ExpenseLocation extends AppCompatActivity
                             implements OnMapReadyCallback,
                                        GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        LocationListener,
                                        SavePositionDialog.ButtonClickedDialogListener {


    GoogleMap gmap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    static Marker mCurrLocationMarker = null;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (isGoogleServicesAvail()) {

            setContentView(R.layout.activity_expense_location);
            initMap();
        }
        else{
            setContentView(R.layout.no_gservices);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_location_float);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //store expense spot in the db
                if (mCurrLocationMarker != null) {
                    SavePositionDialog spd = new SavePositionDialog();
                    spd.show(getSupportFragmentManager(), "Dialog Fragment");
                } else {
                    Toast.makeText(view.getContext(), "Non hai selezionato alcun luogo", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    public boolean isGoogleServicesAvail() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    public void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }


    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;

        int permissionCheck = ContextCompat.checkSelfPermission(ExpenseLocation.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ExpenseLocation.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(ExpenseLocation.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            }

        }else {
            buildGoogleApiClient();
            goToLocationWithZandM(0.0,0.0,15,"equatore");
            gmap.setMyLocationEnabled(true);
        }

        //handle marker dragging
        if (gmap != null) {
            gmap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }
                @Override
                public void onMarkerDrag(Marker marker) {

                }
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    //get the nearest address to the marker
                    Geocoder gc = new Geocoder(ExpenseLocation.this);
                    LatLng ll = marker.getPosition();
                    List<android.location.Address> list = null;
                    try {
                        list = gc.getFromLocation((double)ll.latitude, (double)ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    android.location.Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.showInfoWindow();
                }
            });
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public void goToLocationWithZandM(double lat, double lng,float zoom, String location) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        MarkerOptions expenseSpot = new MarkerOptions()
                .title(location)
                .position(ll)
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_if_13_2255040_1))
                .draggable(true);

        Marker es = gmap.addMarker(expenseSpot);
        mCurrLocationMarker = es;
        gmap.moveCamera(update);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        gmap.setMyLocationEnabled(true);

                    } else {
                        Log.d("tag", "Permissions denied, damn u son i am not NSA");
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }

                    return;
                }

            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }



    @Override
    public void onConnectionSuspended(int i) {

    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        goToLocationWithZandM(location.getLatitude(), location.getLongitude(), 15, "posizione corrente");
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }



    public void geoLocate(View v) throws IOException {
        EditText et = (EditText) findViewById(R.id.geo_finder);
        String location = et.getText().toString();

        //handle geocoding
        Geocoder gc = new Geocoder(this);
        List<android.location.Address> aList = null;
        int i = 0;
        try {
            aList = gc.getFromLocationName(location, 1);
            //handle geoCoder known issue
            while (aList.size()==0 && i < 3) {
                aList = gc.getFromLocationName(location, 1);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (aList != null) {
            android.location.Address address = aList.get(0);
            String locality = address.getLocality();
            Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();
            goToLocationWithZandM(lat,lng,15, location);
        }
        else {
            et.setError( "Prova con un altro luogo" );
            Toast.makeText(this, "Luogo non trovato", Toast.LENGTH_LONG).show();
        }
    }



    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void onFinishDialog(boolean saved) {

        if (saved) {

            Intent intent = new Intent();
            Bundle arg = new Bundle();
            arg.putParcelable("latlng", mCurrLocationMarker.getPosition());
            intent.putExtra("bundle", arg);
            setResult(ExpenseLocation.RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "Posiziona il Marker nel punto in cui hai effettuato la spesa!",Toast.LENGTH_SHORT).show();
        }
    }
}
