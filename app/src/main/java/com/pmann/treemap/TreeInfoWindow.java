package com.pmann.treemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Adapter to show a custom InfoWindow when the user taps a marker.
 */
class TreeInfoWindow implements GoogleMap.InfoWindowAdapter {
    final private View mView;

    //Since this is a dialog it's OK to pass null as the root parameter
    @SuppressLint("InflateParams")
    public TreeInfoWindow (Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.tree_info_window, null);
    }

    public View getInfoWindow (Marker marker) {
        return null;  //use the default frame with custom contents
    }

    public View getInfoContents (Marker marker){
        TextView tvTitle = (TextView)mView.findViewById(R.id.tv_title);
        tvTitle.setText(marker.getTitle());

        TextView tvBody = (TextView)mView.findViewById(R.id.tv_body);
        tvBody.setText(marker.getSnippet());

        return mView;
    }
}
