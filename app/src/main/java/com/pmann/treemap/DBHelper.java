package com.pmann.treemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

@SuppressWarnings({"SameParameterValue", "SpellCheckingInspection"})

/**
 * <p>SQLite helper class. It takes care of the actual DB access and encapsulates things
 * like table and column names.</p>
 * <p>
 * Table columns are pretty self-explanatory, except FLAG, which is a 32-bit field that tracks up
 * to 32 boolean values per record. Semantics of the bit field are defined as:</p>
 * <ul>
 * <li>Bit 1: short listed</li>
 * <li>Bit 2: flagged for follow up</li>
 * <li>Bit 3: needs harvesting</li>
 * <li>Bit 4: needs pruning</li>
 * <li>Bit 5: collect scion wood</li>
 * <li>Bit 6 ... : unassignedpublic</li>
 * </ul>
 */
class DBHelper extends SQLiteOpenHelper implements BaseColumns {
    public static final int MASK_SHORTLIST = 0x0001;
    public static final int MASK_FOLLOWUP = 0x0002;
    public static final int MASK_HARVEST = 0x0004;
    public static final int MASK_PRUNE = 0x0008;
    public static final int MASK_SCION = 0x0010;

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // String constants for DB access. These are not localizable or related to the view, so
    // they don't belong in the string resource file.
    private static final String DATABASE_NAME = "TreeMap.db";
    public static final String TABLE_TREES = "trees";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LONG = "long";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_SUBTYPE = "subtype";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_FLAG = "flag";
    @SuppressWarnings("WeakerAccess")
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

    /**
     * Create the DB tables, if they don't already exist
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(MapsActivity.APP_NAME, "OnCreate");
        db.execSQL(SQL_CREATE_TREES);
    }

    /**
     * Take appropriate action when the app indicates it is using a newer version of the DB.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(MapsActivity.APP_NAME, "OnUpgrade");
        flushDB();
    }

    /**
     * Take appropriate action when the app indicates it is using an older version of the DB.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(MapsActivity.APP_NAME, "OnDowngrade");
    }

    /**
     * Select the specified columns of a table into a cursor. We always include the row ID as the first column.
     * @param tableName
     * @param colName a variable number of column names that are to be included
     * @return a cursor for the selected data
     */
    public Cursor getValues(String tableName, String... colName) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT " + BaseColumns._ID);
        for (String c : colName){
            sb.append(",").append(c);
        }
        sb.append(" FROM ").append(tableName);

        Cursor res = db.rawQuery(sb.toString(), null);
        res.moveToFirst();
        return res;
    }

    /**
     * Obtain a single string value for a given row ID
     * @param table
     * @param column get value from this column
     * @param rowID get value from this record
     * @return desired string value or null, if it could not be retrieved
     */
    public String getStrValue(String table, String column, long rowID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + column + " FROM " + table + " WHERE " + BaseColumns._ID + "=" + rowID, null);
        if (res.getCount() != 1) {
            res.close();
            return null;
        }
        res.moveToFirst();
        String result = res.getString(0);
        res.close();
        return result;
    }

    /**
     * Obtain a single int value for a given row ID
     * @param table
     * @param column get value from this column
     * @param rowID get value from this record
     * @return desired int value or -1, if it could not be retrieved
     */
    public int getIntValue (String table, String column, long rowID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + column + " FROM " + table + " WHERE " + BaseColumns._ID + "=" + rowID, null);
        if (res.getCount() != 1) {
            res.close();
            return -1;
        }
        res.moveToFirst();
        int result = res.getInt(0);
        res.close();
        return result;
    }

    /**
     * Return row IDs of all records matching the given selection criteria
     * @param table
     * @param criteria any valid SQL expression
     * @return cursor for the selected data
     */
    public Cursor selectRecords (String table, String criteria) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + BaseColumns._ID + " FROM " + table + " WHERE " + criteria, null);
        res.moveToFirst();
        return res;
    }

    /**
     * Create a new DB record
     * @param pLat
     * @param pLong
     * @param pType
     * @param pSubtype
     * @param pComment
     * @param pFlag
     * @return row ID of new record or -1, if an error occurred
     */
    public long insertTree(double pLat, double pLong, String pType, String pSubtype, String pComment, int pFlag) {
        Log.d(MapsActivity.APP_NAME, "InsertTree");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LAT, pLat);
        contentValues.put(COLUMN_LONG, pLong);
        contentValues.put(COLUMN_TYPE, pType);
        contentValues.put(COLUMN_SUBTYPE, pSubtype);
        contentValues.put(COLUMN_COMMENT, pComment);
        contentValues.put(COLUMN_FLAG, pFlag);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(TABLE_TREES, null, contentValues);
    }

    /**
     * Update a DB record with the given values
     * @param table
     * @param rowID
     * @param type
     * @param subtype
     * @param comment
     * @param flag
     * @return true, if record was updated
     */
    public boolean updateRow (String table, long rowID, String type, String subtype, String comment, int flag){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_TYPE, type);
        values.put(DBHelper.COLUMN_SUBTYPE, subtype);
        values.put(DBHelper.COLUMN_COMMENT, comment);
        values.put(DBHelper.COLUMN_FLAG, flag);

        String selection = BaseColumns._ID + "=?";
        String [] selectionArgs = {String.valueOf(rowID)};

        int modifiedRows = db.update(table, values, selection, selectionArgs);
        return (1 == modifiedRows);
    }

    /**
     * Update a DB record with the given values
     * @param table
     * @param rowID
     * @param lat
     * @param lng
     * @return true, if record was updated
     */
    public boolean updateRow (String table, long rowID, double lat, double lng){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_LAT, lat);
        values.put(DBHelper.COLUMN_LONG, lng);

        String selection = BaseColumns._ID + "=?";
        String [] selectionArgs = {String.valueOf(rowID)};

        int modifiedRows = db.update(table, values, selection, selectionArgs);
        return (1 == modifiedRows);
    }

    /**
     * Delete the specified DB record
     * @param table
     * @param rowID
     * @return true, if record was deleted
     */
    public boolean deleteRow(String table, long rowID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(table, "_id=" + rowID, null);
        return (1 == deletedRows);
    }

    /**
     * Dump all records from a table to log
     * @param tableName
     */
    public void dumpTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + tableName, null);

        int nCols = res.getColumnCount();
        StringBuilder sb = new StringBuilder();
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

        Log.d(MapsActivity.APP_NAME, sb.toString());
    }

    /**
     * Add test records to the DB
     */
    public void populateDB() {
        Log.d(MapsActivity.APP_NAME, "populateDB");

        insertTree(47.5588789, -122.2695519, "Apple", "Spitzenberg", "2015 bumper crop", 1);
        insertTree(47.5587872,-122.2692267, "Apple", "Winesap", "Come back next year for scion collection", 3);
        insertTree(47.5573659, -122.2729869, "Pear", "Seckel", "Very tasty aldf alsdkfj aldskfj aslfk lk l ladkjf l aldkfj  ladkf l aldkfj lak", 7);
    }

    /**
     * Delete existing DB and recreate an empty DB.
     */
    public void flushDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_TREES);
        onCreate(db);
    }
}