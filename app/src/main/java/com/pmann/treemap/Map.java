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

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Manage a Google map API client with its associated markers
 * <p>A singleton instance of this class is made available through {@code MapsActivity.getMap()}</p>
 */
public class Map {
    final private GoogleApiClient mGoogleApiClient;
    final private MapsActivity mMapsActivity;
    private GoogleMap mMap;

    // Settings to determine location update frequency and desired accuracy
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

    /**
     * Determine which DB record corresponds to a given marker
     *
     * @param pMarker the selected marker
     * @return row ID of the corresponding DB record, or -1 if not found
     */
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

    /**
     * Initialize the map with callbacks, markers, etc. Set an initial zoom level
     * and show the current location on the map.
     *
     * @param pMapsActivity the associated view object
     * @param pMap          the GoogleMap object
     */
    public Map(MapsActivity pMapsActivity, GoogleMap pMap) {
        mMapsActivity = pMapsActivity;
        mMap = pMap;

        mGoogleApiClient = new GoogleApiClient.Builder(mMapsActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mMapsActivity)
                .addOnConnectionFailedListener(mMapsActivity)
                .build();
        mGoogleApiClient.connect();
        mMarkerMap = new ArrayMap<>();
        mFlagMap = new ArrayMap<>();

        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setInfoWindowAdapter(new TreeInfoWindow(mMapsActivity.getApplicationContext()));
        mMap.setOnInfoWindowClickListener(mMapsActivity);

        createMarkers();
    }

    /**
     * Call this to receive continuous location updates via the {@code MapsActivity.onLocationChanged()} callback
     */
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

    /**
     * Create a map marker and add it to the internal management data structures
     *
     * @param pRowID   DB record ID
     * @param pLat     latitude
     * @param pLng     longitude
     * @param pType    type of tree - this can be any string
     * @param pSubtype subtype of tree - this can be any string
     * @param pComment another string field for comments, etc.
     * @param pFlag    bit vector that encodes up to 32 flag settings. See DBHelper class for details.
     */
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

    /**
     * Loop through the DB and create a marker for each record
     */
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

    /**
     * Get the last known latitude and longitude
     *
     * @return location, or null if it could not be determined
     */
    public Location getCurrentLocation() {
        Log.d(MapsActivity.APP_NAME, "getCurrentLocation()");
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc == null)
            Log.e(MapsActivity.APP_NAME, "Failed to get location");
        return loc;
    }

    /**
     * Get last known location,  but adjust if necessary to prevent collision with locations
     * already stored in the database.
     *
     * @return current (adjusted) location; or null, in case of error
     */
    public Location getAdjustedLocation() {
        Location loc = getCurrentLocation();
        if (loc == null)
            return loc;

        // Note that when we adjust for a collision, we may end up causing a new collision.
        // So we must disambiguate repeatedly until we succeed, or until we time out.
        for (int i = 1; i <= 3; i++)
            if (makeUnique(loc))
                return loc;             //location is unique

        Log.e(MapsActivity.APP_NAME, "Failed to make location unique");
        return null;                    //failed to make location unique
    }

    /**
     * Move the Google Maps view to the last known location
     */
    public void moveToLastLocation() {
        Location loc = getCurrentLocation();
        if (loc != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
    }

    /**
     * Ensure that a given set of markers is turned on. Note that we don't adjust the map
     * view so they may not actually be currently displayed.
     *
     * @param rowIDs set of markers to be made visible
     */
    public void setVisible(TreeSet<Long> rowIDs) {
        if (rowIDs == null) return; // ignore - probably a result of a typo in the entry form

        Set<java.util.Map.Entry<Marker, Long>> markers = mMarkerMap.entrySet();
        for (java.util.Map.Entry<Marker, Long> entry : markers) {
            Marker m = entry.getKey();
            m.setVisible(rowIDs.contains(entry.getValue()));
        }
    }

    /**
     * Turn on markers that meet the given filter criteria; turn off all others. Note that we don't adjust the map
     * view here.
     *
     * @param flagFilter bit vector that encodes the desired criteria. See DBHelper class for details.
     */
    public void setVisible(int flagFilter) {
        if (flagFilter == 0 || flagFilter == 0xFFFF) return;

        Set<java.util.Map.Entry<Marker, Integer>> markers = mFlagMap.entrySet();
        for (java.util.Map.Entry<Marker, Integer> entry : markers) {
            Marker m = entry.getKey();
            int f = entry.getValue();
            m.setVisible((f & flagFilter) != 0);
        }
    }

    /**
     * Make all markers visible, regardless of filter criteria
     */
    public void showAll() {
        for (Marker m : mMarkerMap.keySet()) {
            m.setVisible(true);
        }
    }

    /**
     * Ensure that the provided location does not correspond to any existing marker. If
     * necessary, add a small random offset.
     *
     * @param loc location
     * @return false if location needed to be adjusted; true if it was already unique.
     */
    final private static double LOC_OFFSET = 0.00005;
    final private Random mRandom = new Random(); //don't care about seed for this application

    private boolean makeUnique(Location loc) {
        StringBuilder crit = new StringBuilder();
        crit.append(DBHelper.COLUMN_LAT).append('=').append(loc.getLatitude());
        crit.append(" AND ").append(DBHelper.COLUMN_LONG).append('=').append(loc.getLongitude());
        Cursor cursor = DB.helper().selectRecords(DBHelper.TABLE_TREES, crit.toString());

        if (cursor.getCount() == 0)
            return true;    //no collision, no adjustment

        double rndFactorLat = mRandom.nextDouble() * (mRandom.nextBoolean()? 1 : -1);
        double offsetLat = LOC_OFFSET * rndFactorLat;

        double rndFactorLong = mRandom.nextDouble() * (mRandom.nextBoolean()? 1 : -1);
        double offsetLong = LOC_OFFSET * rndFactorLong;

        loc.setLatitude(loc.getLatitude() + offsetLat);
        loc.setLongitude(loc.getLongitude() + offsetLong);

        return false;   //location was not unique and needed to be adjusted
    }
}
