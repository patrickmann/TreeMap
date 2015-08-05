// Class to manage a Google map API client with the associated markers

package com.pmann.treemap;

import android.database.Cursor;
import android.location.Location;
import android.support.v4.util.SimpleArrayMap;
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

public class Map {
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private MapsActivity mMapsActivity;

    // These settings will give you updates at the maximal rates currently possible
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    // Map marker IDs to associated DB record IDs. This tells us which record to use when
    // operating on a given marker.
    private static SimpleArrayMap<String, Long> mMarkerMap = null;
    public static long getRowID(String pMarkerID) {
        long result = -1;
        if (mMarkerMap != null) {
            Long rowID = mMarkerMap.get(pMarkerID);
            if (rowID != null)
                result = rowID;
            else
                Log.e(MapsActivity.APP_NAME, "No DB record " + pMarkerID);
        }
        return result;
    }

    public Map (MapsActivity pMapsActivity) {
        mMapsActivity = pMapsActivity;
        mGoogleApiClient = new GoogleApiClient.Builder(mMapsActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mMapsActivity)
                .addOnConnectionFailedListener(mMapsActivity)
                .build();
        mMarkerMap = new SimpleArrayMap<String, Long>();
    }

    public void init (GoogleMap pMap){
        mMap = pMap;
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setInfoWindowAdapter(new TreeInfoWindow(mMapsActivity.getApplicationContext()));
        mMap.setOnInfoWindowClickListener(mMapsActivity);

        createMarkers();
    }

    public void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                mMapsActivity);
    }

    public void connect (){
        mGoogleApiClient.connect();
    }

    public void disconnect (){
        mGoogleApiClient.connect();
    }

    public void addMarker(long pRowID, double pLat, double pLng, String pType, String pSubtype, String pComment){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pLat, pLng))
                .icon(BitmapDescriptorFactory.defaultMarker(Tree.hueByType(pType)))
                .title(pType + ": " + pSubtype)
                .snippet(pComment));
        mMarkerMap.put(marker.getId(), pRowID); //associate DB ID with marker ID
    }

    private void createMarkers(){
        Cursor cursor = DB.helper().getValues(
                DBHelper.TABLE_TREES, DBHelper.COLUMN_LAT, DBHelper.COLUMN_LONG,
                DBHelper.COLUMN_TYPE, DBHelper.COLUMN_SUBTYPE, DBHelper.COLUMN_COMMENT);

        while (!cursor.isAfterLast()) {
            addMarker(
                    cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5));

/*
            String strTitle = cursor.getString(3) + ": " + cursor.getString(4);
            String strSnippet = cursor.getString(5);
            float hue = Tree.hueByType(cursor.getString(3));
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cursor.getDouble(1), cursor.getDouble(2)))
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    .title(strTitle)
                    .snippet(strSnippet));

            mMarkerMap.put(marker.getId(), cursor.getInt(0)); //associate DB ID with marker ID
*/

            cursor.moveToNext();
        }
        cursor.close();
    }

    public Location getCurrentLocation () {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

}
