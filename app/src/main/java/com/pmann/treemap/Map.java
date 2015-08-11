// Class to manage a Google map API client with the associated markers

package com.pmann.treemap;

import android.database.Cursor;
import android.location.Location;
import android.util.ArrayMap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;
import java.util.TreeSet;

public class Map {
    final private GoogleApiClient mGoogleApiClient;
    final private MapsActivity mMapsActivity;
    private GoogleMap mMap;

    // These settings will give you updates at the maximal rates currently possible
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(10000)         // 10 seconds
            .setFastestInterval(500)    // 500ms
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    // Map markers to associated DB record IDs. This tells us which record to use when
    // operating on a given marker.
    private static ArrayMap<Marker, Long> mMarkerMap = null;

    // Map marker to flag value. This makes it easy to show and hide markers based on the
    // flag settings.
    private static ArrayMap<Marker, Integer> mFlagMap = null;

    public static long getRowID(Marker pMarker) {
        long result = -1;
        if (mMarkerMap != null) {
            Long rowID = mMarkerMap.get(pMarker);
            if (rowID != null)
                result = rowID;
            else
                Log.e(MapsActivity.APP_NAME, "No DB record " + pMarker.getTitle());
        }
        return result;
    }

    public Map(MapsActivity pMapsActivity) {
        mMapsActivity = pMapsActivity;
        mGoogleApiClient = new GoogleApiClient.Builder(mMapsActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mMapsActivity)
                .addOnConnectionFailedListener(mMapsActivity)
                .build();
        mMarkerMap = new ArrayMap<>();
        mFlagMap = new ArrayMap<>();
    }

    public void init(GoogleMap pMap) {
        mMap = pMap;
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setInfoWindowAdapter(new TreeInfoWindow(mMapsActivity.getApplicationContext()));
        mMap.setOnInfoWindowClickListener(mMapsActivity);

        createMarkers();
    }

    // Call this to receive continuous location updates via the MapsActivity.onLocationChanged() callback
    @SuppressWarnings("unused")
    public void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                mMapsActivity);
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.connect();
    }

    public void addMarker(
            long pRowID, double pLat, double pLng, String pType, String pSubtype, String pComment, int pFlag) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pLat, pLng))
                .icon(BitmapDescriptorFactory.defaultMarker(Tree.hueByType(pType)))
                .title(pType + ": " + pSubtype)
                .snippet(pComment));
        mMarkerMap.put(marker, pRowID); //associate DB ID with marker reference
        mFlagMap.put(marker, pFlag);    //save flag value for efficient filtering
    }

    private void createMarkers() {
        Cursor cursor = DB.helper().getValues(
                DBHelper.TABLE_TREES, DBHelper.COLUMN_LAT, DBHelper.COLUMN_LONG,
                DBHelper.COLUMN_TYPE, DBHelper.COLUMN_SUBTYPE, DBHelper.COLUMN_COMMENT, DBHelper.COLUMN_FLAG);

        while (!cursor.isAfterLast()) {
            addMarker(
                    cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6));
            cursor.moveToNext();
        }
        cursor.close();
    }

    public Location getCurrentLocation() {
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc == null)
            Log.e(MapsActivity.APP_NAME, "Failed to get location");
        return loc;
    }

    public void moveToLastLocation() {
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
        else
            Log.e(MapsActivity.APP_NAME, "getLastLocation() failed");
    }

    public void setVisible(TreeSet<Long> rowIDs) {
        if (rowIDs == null) return; // ignore - probably a result of a typo in the entry form

        Set<java.util.Map.Entry<Marker, Long>> markers = mMarkerMap.entrySet();
        for (java.util.Map.Entry<Marker, Long> entry : markers) {
            Marker m = entry.getKey();
            m.setVisible(rowIDs.contains(entry.getValue()));
        }
    }

    public void setVisible(int flagFilter) {
        if (flagFilter == 0 || flagFilter == 0xFFFF) return;

        Set<java.util.Map.Entry<Marker, Integer>> markers = mFlagMap.entrySet();
        for (java.util.Map.Entry<Marker, Integer> entry : markers) {
            Marker m = entry.getKey();
            int f = entry.getValue();
            m.setVisible((f & flagFilter) != 0);
        }
    }

    public void showAll() {
        for (Marker m : mMarkerMap.keySet()) {
            m.setVisible(true);
        }
    }

}
