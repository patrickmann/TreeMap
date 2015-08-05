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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;


public class EditDialogFragment extends DialogFragment {

    private Marker mMarker;

    public void setMarker(Marker pMarker) {
        mMarker = pMarker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long rowID = Map.getRowID(mMarker.getId());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_dialog, null);

        final TextView type = (TextView) dialogView.findViewById(R.id.txt_type);
        final TextView subtype = (TextView) dialogView.findViewById(R.id.txt_subtype);
        final TextView comment = (TextView) dialogView.findViewById(R.id.txt_comment);

        type.setText(DB.helper().getValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_TYPE, rowID));
        subtype.setText(DB.helper().getValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_SUBTYPE, rowID));
        comment.setText(DB.helper().getValue(DBHelper.TABLE_TREES, DBHelper.COLUMN_COMMENT, rowID));

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
                        if (DB.helper().updateRow(DBHelper.TABLE_TREES, rowID, newType, newSubtype, newComment)) {
                            simpleToast("Record updated");
                            mMarker.setTitle(newType + ": " + newSubtype);
                            mMarker.setSnippet(newComment);
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
