// TreeMap: a simple location-aware app that helps identify, locate, and manage trees in the
// urban environment.
//
// Main Activity of the application. This class registers all the necessary callback interfaces
// for the Google maps UI. The actual implementation is delegated to other classes.

package com.pmann.treemap;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

public class MapsActivity extends Activity
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.maps_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_filter:
                openFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openFilter(){
        FilterDialogFragment dialog = new FilterDialogFragment();
        dialog.show(getFragmentManager(), "FilterDialogFragment");
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

        //resetDB();
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
    // In order to receive location updates, call requestLocationUpdates() in the
    // onConnected() method.
    public void onLocationChanged(Location location) {
    }

    public void onConnected(Bundle connectionHint) {
//      If we need to track location continuously, request location updates here
//      mMap.requestLocationUpdates();

        mMap.moveToLastLocation();
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

    @SuppressWarnings("unused")
    public void resetDB() {
        DB.helper().flushDB();
        DB.helper().populateDB();
    }

    private void dumpDB() {
        DB.helper().dumpTable(DBHelper.TABLE_TREES);
    }

}
