package com.pmann.treemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class FilterDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.filter_dialog, null);

        final EditText txtType = (EditText) dialogView.findViewById(R.id.txt_type);
        final EditText txtSubtype = (EditText) dialogView.findViewById(R.id.txt_subtype);

        builder.setView(dialogView)
                .setTitle("Define Filter Criteria")

                .setPositiveButton("Apply Filter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String strType = txtType.getText().toString();
                        String strSubtype = txtSubtype.getText().toString();

                        StringBuilder criteria = new StringBuilder();
                        if (strType.length() > 0)
                            criteria.append(DBHelper.COLUMN_TYPE).append(" LIKE '").append(strType).append("'");
                        if (strSubtype.length() > 0)
                            criteria.append(DBHelper.COLUMN_SUBTYPE).append(" LIKE '").append(strSubtype).append("'");
                        Log.d(MapsActivity.APP_NAME, criteria.toString());
                        if (criteria.length() > 0) {
                            Cursor res = DB.helper().selectRecords(DBHelper.TABLE_TREES, criteria.toString());
                            TreeSet<Long> rowIDs = new TreeSet<Long>();
                            while (!res.isAfterLast()) {
                                rowIDs.add(res.getLong(0));
                                res.moveToNext();
                            }
                            res.close();

                            if (rowIDs.size() > 0)
                                MapsActivity.getMap().setVisible(rowIDs);
                        }
                    }
                })

                .setNeutralButton("Clear Filter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MapsActivity.getMap().showAll();
                    }
                });

        return builder.create();
    }
}
