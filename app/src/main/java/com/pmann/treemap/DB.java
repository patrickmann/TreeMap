// Creating a DBHelper instance requires a Context. This is not always readily available, e.g. in
// an AlertDialog. So we provide a global instance of the helper which we instantiate in the main
// activity. Seems better than passing context all over the place.

package com.pmann.treemap;

import android.util.Log;
import android.content.Context;

class DB {
    private static DBHelper mDBHelper = null;

    public static void init(Context context) {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(context);
        }
    }

    public static DBHelper helper () {
        if (mDBHelper == null) {
            Log.e("TreeMap", "DB not initialized");
        }
        return mDBHelper;
    }
}
