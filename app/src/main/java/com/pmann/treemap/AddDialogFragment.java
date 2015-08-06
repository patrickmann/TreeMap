// This dialog opens when the user taps on the 'add marker' button. The user can enter
// information to populate the new record, or abort if it was an inadvertent click.

package com.pmann.treemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AddDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dialog, null);

        final TextView type = (TextView) dialogView.findViewById(R.id.txt_type);
        final TextView subtype = (TextView) dialogView.findViewById(R.id.txt_subtype);
        final TextView comment = (TextView) dialogView.findViewById(R.id.txt_comment);

        builder.setView(dialogView)
                .setTitle("Create New Entry")

                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newType = type.getText().toString();
                        String newSubtype = subtype.getText().toString();
                        String newComment = comment.getText().toString();

                        Location loc = MapsActivity.getMap().getCurrentLocation();
                        if (loc != null) {
                            double lat = loc.getLatitude();
                            double lng = loc.getLongitude();
                            long newRowID = DB.helper().insertTree(lat, lng, newType, newSubtype, newComment, 0);

                            if (newRowID != -1) {
                                simpleToast("New record added");
                                MapsActivity.getMap().addMarker(newRowID, lat, lng, newType, newSubtype, newComment);
                            } else {
                                simpleToast("Creation failed!");
                                Log.e(MapsActivity.APP_NAME, "DB failure: insert");
                            }
                        } else {
                            simpleToast("Creation failed!");
                            Log.e(MapsActivity.APP_NAME, "Failed to get location!");
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
