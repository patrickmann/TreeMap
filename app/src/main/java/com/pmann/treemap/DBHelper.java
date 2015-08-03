// SQLite helper class. It takes care of the actual DB access and encapsulates things
// like table and column names.

package com.pmann.treemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper implements BaseColumns {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TreeMap.db";

    public static final String TABLE_TREES = "trees";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LONG = "long";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_SUBTYPE = "subtype";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_FLAG = "flag";
    public static final String COLUMN_DATE = "date";

    private static final String SQL_CREATE_TREES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TREES + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LONG + " REAL," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_SUBTYPE + " TEXT," +
                    COLUMN_COMMENT + " TEXT," +
                    COLUMN_FLAG + " INTEGER," +
                    COLUMN_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    " )";

    private static final String SQL_DELETE_TREES =
            "DROP TABLE IF EXISTS " + TABLE_TREES;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("TreeMap", "OnCreate");
        db.execSQL(SQL_CREATE_TREES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("TreeMap", "OnUpgrade");
        flushDB();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("TreeMap", "OnDowngrade");
    }

    public Cursor getValues(String tableName, String... colName) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT " + BaseColumns._ID);
        for (int i=0; i<colName.length; i++){
            sb.append("," + colName[i]);
        }
        sb.append(" FROM " + tableName);

        Cursor res = db.rawQuery(sb.toString(), null);
        res.moveToFirst();
        return res;
    }

    public boolean existsEntry(String table, String column, String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + table + " WHERE " + column + "=" + value, null);
        return (res.getCount() > 0);
    }

    public boolean insertTree(double pLat, double pLong, String pType, String pSubtype, String pComment, int pFlag) {
        Log.d("TreeMap", "InsertTree");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LAT, pLat);
        contentValues.put(COLUMN_LONG, pLong);
        contentValues.put(COLUMN_TYPE, pType);
        contentValues.put(COLUMN_SUBTYPE, pSubtype);
        contentValues.put(COLUMN_COMMENT, pComment);
        contentValues.put(COLUMN_FLAG, pFlag);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(TABLE_TREES, null, contentValues);

        return (-1 != newRowId);
    }

    public boolean deleteString(String table, String column, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(table, "WHERE " + column + "=" + value, null);
        return (1 == deletedRows);
    }

    public boolean deleteRow(String table, long rowID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(table, "_id=" + rowID, null);
        return (1 == deletedRows);
    }

    public void dumpTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + tableName, null);

        int nCols = res.getColumnCount();
        StringBuffer sb = new StringBuffer();
        res.moveToFirst();
        while (!res.isAfterLast()) {
            for (int i = 0; i < nCols; i++) {
                sb.append(res.getString(i));
                sb.append("\t");
            }
            sb.append("\n");
            res.moveToNext();
        }
        res.close();

        Log.d("TreeMap", sb.toString());
    }

    public void populateDB() {
        Log.d("TreeMap", "populateDB");

        insertTree(47.5588789, -122.2695519, "Apple", "Spitzenberg", "2015 bumper crop", 0);
        insertTree(47.5587872,-122.2692267, "Apple", "Winesap", "Come back next year for scion collection", 0);
        insertTree(47.5573659, -122.2729869, "Pear", "Seckel", "Very tasty aldf alsdkfj aldskfj aslfk lk l ladkjf l aldkfj  ladkf l aldkfj lak", 0);
    }

    public void flushDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_TREES);
        onCreate(db);
    }
}