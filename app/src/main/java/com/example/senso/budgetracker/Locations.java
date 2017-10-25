package com.example.senso.budgetracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class Locations extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        DBHelper dbh = new DBHelper(this);
        List<Pair<LatLng, String>> locatedExpense = dbh.retrivePositions();
        LatLng position ;
        LatLng finalPosition;
        String type;

        for (int i=0 ; i < locatedExpense.size() ; i++) {

            position = locatedExpense.get(i).getLeft();
            type = locatedExpense.get(i).getRight();

            if ( type.equals("Spesa Normale") ) {

                MarkerOptions expenseSpot = new MarkerOptions()
                        .title(type)
                        .position(position)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_if_13_2255040_1));
                mMap.addMarker(expenseSpot);

            } else {

                MarkerOptions expenseSpot = new MarkerOptions()
                        .title(type)
                        .position(position)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_if_13_2255040_2));
                mMap.addMarker(expenseSpot);

            }

        }

        Intent intent = getIntent();

        if ( intent.getStringExtra("from").equals("expense_activity") ) {

            //map request comes from expense activity, zoom to the expense
            Bundle bundle = intent.getParcelableExtra("bundle");
            LatLng expense_position = bundle.getParcelable("position");

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(expense_position, 15));

        } // else do nothing, i.e. display without zoom every marker in the planisphere

    }



    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
