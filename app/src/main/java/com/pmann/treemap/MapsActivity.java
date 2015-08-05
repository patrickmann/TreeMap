// Main Activity of the application. This class registers all the necessary callback interfaces
// for the Google maps UI. The actual implementation is delegated to other classes.

package com.pmann.treemap;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    public static final String APP_NAME = "TreeMap";

    private static Map mMap = null;
    public static Map getMap() {
        if (mMap == null) {
            Log.e("TreeMap", "Map not initialized");
        }
        return mMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        ImageButton btnAdd = (ImageButton) findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(APP_NAME, "btnAdd onClick");
                AddDialogFragment dialog = new AddDialogFragment();
                dialog.show(getFragmentManager(), "AddDialogFragment");
            }
        });

        DB.init(this);
        mMap = new Map(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        resetDB();
        dumpDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMap.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMap.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap pMap) {
        mMap.init(pMap);
    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        Log.d(APP_NAME, location.toString());
    }

    public void onConnected(Bundle connectionHint) {
        mMap.requestLocationUpdates();
    }

    public boolean onMarkerClick(Marker marker) {
        return false; //event not consumed - pop up the info window
    }

    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
        EditDialogFragment dialog = new EditDialogFragment();
        dialog.setMarker(marker);
        dialog.show(getFragmentManager(), "EditDialogFragment");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    public void resetDB() {
        DB.helper().flushDB();
        DB.helper().populateDB();
    }

    private void dumpDB() {
        DB.helper().dumpTable(DBHelper.TABLE_TREES);
    }

}
