// Main Activity of the application. This class takes care of initializing the map and responding
// when users tap a marker.

package com.pmann.treemap;

import android.app.DialogFragment;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener
{
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap = null;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        DB.init(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        resetDB();
        dumpDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setInfoWindowAdapter(new TreeInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        createMarkers();
    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        Log.d(getString(R.string.app_name), location.toString());
    }

    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
    }

    public boolean onMarkerClick(Marker marker) {
        return false; //event not consumed - pop up the info window
    }

    public void onInfoWindowClick(Marker marker){
        marker.hideInfoWindow();
        DialogFragment dialog = new EditDialogFragment();
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

    private void createMarkers(){
        Cursor cursor = DB.helper().getValues(DBHelper.TABLE_TREES, DBHelper.COLUMN_LAT, DBHelper.COLUMN_LONG,
                DBHelper.COLUMN_TYPE, DBHelper.COLUMN_SUBTYPE, DBHelper.COLUMN_COMMENT);

        while (!cursor.isAfterLast()) {
            String strTitle = cursor.getString(3) + ": " + cursor.getString(4);
            String strSnippet = cursor.getString(5);
            float hue = Tree.hueByType(cursor.getString(3));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cursor.getDouble(1), cursor.getDouble(2)))
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    .title(strTitle)
                    .snippet(strSnippet));
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void resetDB (){
        DB.helper().flushDB();
        DB.helper().populateDB();
    }

    private void dumpDB() {
        DB.helper().dumpTable(DBHelper.TABLE_TREES);
    }

}
