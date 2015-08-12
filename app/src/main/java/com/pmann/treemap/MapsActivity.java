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

/**
 * <h1>TreeMap</h1>
 * <p>A simple location-aware app that helps identify, locate, and manage trees in the
 * urban environment.</p>
 * <ul>
 * <li>Create record of tree at current location</li>
 * <li>Store and edit information about tree</li>
 * <li>Set flags to track related activities</li>
 * <li>Display a filtered map view, based on user-defined criteria</li>
 * </ul>
 * <p>
 * Main Activity of the application. This class initializes the Google maps UI and registers all
 * the necessary callback interfaces.</p>
 *
 * @author Patrick Mann
 */
public class MapsActivity extends Activity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    public static final String APP_NAME = "TreeMap";

    private static Map mMap = null;

    /**
     * The app uses a singleton instance of the Map class - there is only one instance
     * of the Google Maps UI and we need to interact with it at various places throughout
     * the app. It's cleaner to have a global accessor than to pass it around as a parameter.
     *
     * @return Map class singleton or null if not yet initialized
     */
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

    private void openFilter() {
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

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //resetDB();    //erase and recreate DB with test data
        dumpDB();       //dump contents of DB to log file
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) // map may not yet be initialized
            mMap.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) // map may not yet be initialized
            mMap.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap pMap) {
        Log.d(APP_NAME,"onMapReady()");
        mMap = new Map(this, pMap);
    }

    @Override
    // In order to receive location updates, call requestLocationUpdates() in the
    // onConnected() method.
    public void onLocationChanged(Location location) {
    }

    /**
     * This is where you would request continuous location updates via
     * {@code mMap.requestLocationUpdates()}. We don't do it to reduce battery usage.
     * @param connectionHint
     */
    public void onConnected(Bundle connectionHint) {
        Log.d(APP_NAME,"onConnected()");
//      mMap.requestLocationUpdates();
        mMap.moveToLastLocation();
    }

    /**
     * Called when user clicks on a map marker. We rely on the standard info window UI,
     * so we return false here.
     *
     * @param marker that was clicked on
     * @return true, if event was consumed; false if we want the info window to pop up
     */
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * Called when user clicks anywhere on the info window of a map marker.
     * <p>In this app we understand that to mean that the user wishes to modify or
     * delete the related DB record.</p>
     *
     * @param marker
     */
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
    /**
     * Erase and recreate the DB with test data. Be careful!
     */
    public void resetDB() {
        DB.helper().flushDB();
        DB.helper().populateDB();
    }

    /**
     * Dump the entire contents of the DB to the log
     */
    private void dumpDB() {
        DB.helper().dumpTable(DBHelper.TABLE_TREES);
    }
}
