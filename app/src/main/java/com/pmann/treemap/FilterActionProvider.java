package com.pmann.treemap;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

public class FilterActionProvider extends ActionProvider {

    private Context mContext;

    public FilterActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    public View onCreateActionView() {
        // Inflate the action view to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.filter_action_provider, null);
        return view;
    }
}
