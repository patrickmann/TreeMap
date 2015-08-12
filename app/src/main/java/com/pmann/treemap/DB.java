package com.pmann.treemap;

import android.util.Log;
import android.content.Context;

/**
 * <p>Class to manage a singleton instance of the DBHelper class.</p>
 * <p>
 * Creating a DBHelper instance requires a Context. This is not always readily available, e.g. in
 * an AlertDialog. So we provide a global instance of the helper which we instantiate in the main
 * activity. This is cleaner than passing context all over the place.</p>
 */
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
