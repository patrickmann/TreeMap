// This dialog opens when the user taps on the InfoWindow of a map marker. The user can modify the
// existing DB record or delete it.

package com.pmann.treemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class EditDialogFragment extends DialogFragment {

    private Marker mMarker;

    public void setMarker(Marker pMarker) {
        mMarker = pMarker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long rowID = Map.getRowID(mMarker);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_dialog, null);

        final TextView type = (TextView) dialogView.findViewById(R.id.txt_type);
        final TextView subtype = (TextView) dialogView.findViewById(R.id.txt_subtype);
        final TextView comment = (TextView) dialogView.findViewById(R.id.txt_comment);

        type.setText(DB.helper().getStrValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_TYPE, rowID));
        subtype.setText(DB.helper().getStrValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_SUBTYPE, rowID));
        comment.setText(DB.helper().getStrValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_COMMENT, rowID));

        final CheckBox cbShortlist = (CheckBox) dialogView.findViewById(R.id.chk_shortlist);
        final CheckBox cbFollowup = (CheckBox) dialogView.findViewById(R.id.chk_followup);
        final CheckBox cbHarvest = (CheckBox) dialogView.findViewById(R.id.chk_harvest);
        final CheckBox cbPrune = (CheckBox) dialogView.findViewById(R.id.chk_prune);
        final CheckBox cbScion = (CheckBox) dialogView.findViewById(R.id.chk_scion);

        int flag = DB.helper().getIntValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_FLAG, rowID);
        if ((flag & DBHelper.MASK_SHORTLIST) > 0) cbShortlist.setChecked(true);
        if ((flag & DBHelper.MASK_FOLLOWUP) > 0) cbFollowup.setChecked(true);
        if ((flag & DBHelper.MASK_HARVEST) > 0) cbHarvest.setChecked(true);
        if ((flag & DBHelper.MASK_PRUNE) > 0) cbPrune.setChecked(true);
        if ((flag & DBHelper.MASK_SCION) > 0) cbScion.setChecked(true);

        builder.setView(dialogView)
                .setTitle("Modify Entry")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (DB.helper().deleteRow(DBHelper.TABLE_TREES, rowID)) {
                            simpleToast("Record deleted");
                            mMarker.remove();
                        } else {
                            simpleToast("Delete failed!");
                            Log.e(MapsActivity.APP_NAME, "DB failure: delete");
                        }
                    }
                })

                .setNegativeButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newType = type.getText().toString();
                        String newSubtype = subtype.getText().toString();
                        String newComment = comment.getText().toString();

                        int flag = 0;
                        if (cbShortlist.isChecked()) flag |= DBHelper.MASK_SHORTLIST;
                        if (cbFollowup.isChecked()) flag |= DBHelper.MASK_FOLLOWUP;
                        if (cbHarvest.isChecked()) flag |= DBHelper.MASK_HARVEST;
                        if (cbPrune.isChecked()) flag |= DBHelper.MASK_PRUNE;
                        if (cbScion.isChecked()) flag |= DBHelper.MASK_SCION;

                        if (DB.helper().updateRow(DBHelper.TABLE_TREES, rowID, newType, newSubtype, newComment, flag)) {
                            simpleToast("Record updated");
                            mMarker.setTitle(newType + ": " + newSubtype);
                            mMarker.setSnippet(newComment);
                            mMarker.setIcon(BitmapDescriptorFactory.defaultMarker(Tree.hueByType(newType)));
                        } else {
                            simpleToast("Update failed!");
                            Log.e(MapsActivity.APP_NAME, "DB failure: update");
                        }
                    }
                })

                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog - do nothing
                    }
                });

        return builder.create();
    }

    private void simpleToast(String msg) {
        Toast toast = Toast.makeText(
                getActivity().getApplicationContext(),
                msg,
                Toast.LENGTH_SHORT
        );
        toast.show();
    }
}
