// Dialog for entering filter criteria. Visibility of map markers is updated based on the
// active filter criteria. Filtering persists until the filter is explicitly reset.

package com.pmann.treemap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.TreeSet;

public class FilterDialogFragment extends DialogFragment {
    private ArrayList<CheckBox> mChkBoxList;
    private int mFlagFilter = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Since this is a dialog it's OK to pass null as the root parameter
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.filter_dialog, null);

        final EditText txtType = (EditText) dialogView.findViewById(R.id.txt_type);
        final EditText txtSubtype = (EditText) dialogView.findViewById(R.id.txt_subtype);
        initializeCheckboxes(dialogView);

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
                                    TreeSet<Long> rowIDs = new TreeSet<>();
                                    while (!res.isAfterLast()) {
                                        rowIDs.add(res.getLong(0));
                                        res.moveToNext();
                                    }
                                    res.close();

                                    MapsActivity.getMap().setVisible(rowIDs);
                                }
                                MapsActivity.getMap().setVisible(mFlagFilter);
                            }
                        }
                ).

                setNeutralButton("Clear Filter", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MapsActivity.getMap().showAll();
                            }
                        }

                );
        return builder.create();
    }

    // click listeners must be dynamically assigned in fragments; they can't be hooked up
    // in the layout XML

    private void initializeCheckboxes(View view) {
        CheckBox cb;
        mChkBoxList = new ArrayList<>();
        View.OnClickListener listener = new FilterListener();

        cb = (CheckBox) view.findViewById(R.id.chk_all);
        cb.setOnClickListener(listener);

        cb = (CheckBox) view.findViewById(R.id.chk_shortlist);
        cb.setOnClickListener(listener);
        mChkBoxList.add(cb);

        cb = (CheckBox) view.findViewById(R.id.chk_followup);
        cb.setOnClickListener(listener);
        mChkBoxList.add(cb);

        cb = (CheckBox) view.findViewById(R.id.chk_harvest);
        cb.setOnClickListener(listener);
        mChkBoxList.add(cb);

        cb = (CheckBox) view.findViewById(R.id.chk_prune);
        cb.setOnClickListener(listener);
        mChkBoxList.add(cb);

        cb = (CheckBox) view.findViewById(R.id.chk_scion);
        cb.setOnClickListener(listener);
        mChkBoxList.add(cb);
    }

    private class FilterListener implements View.OnClickListener {
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();

            // Update filter and view based on which checkbox was clicked
            switch (view.getId()) {
                case R.id.chk_all:
                    if (checked)
                        mFlagFilter = 0xFFFF;
                    else
                        mFlagFilter = 0;
                    for (CheckBox cb: mChkBoxList) {
                        cb.setChecked(checked);
                    }
                    break;
                case R.id.chk_followup:
                    if (checked)
                        mFlagFilter |= DBHelper.MASK_FOLLOWUP;
                    else
                        mFlagFilter &= ~DBHelper.MASK_FOLLOWUP;
                    break;
                case R.id.chk_harvest:
                    if (checked)
                        mFlagFilter |= DBHelper.MASK_HARVEST;
                    else
                        mFlagFilter &= ~DBHelper.MASK_HARVEST;
                    break;
                case R.id.chk_prune:
                    if (checked)
                        mFlagFilter |= DBHelper.MASK_PRUNE;
                    else
                        mFlagFilter &= ~DBHelper.MASK_PRUNE;
                    break;
                case R.id.chk_scion:
                    if (checked)
                        mFlagFilter |= DBHelper.MASK_SCION;
                    else
                        mFlagFilter &= ~DBHelper.MASK_SCION;
                    break;
                case R.id.chk_shortlist:
                    if (checked)
                        mFlagFilter |= DBHelper.MASK_SHORTLIST;
                    else
                        mFlagFilter &= ~DBHelper.MASK_SHORTLIST;
                    break;
            }
        }
    }
}
