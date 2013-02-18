package com.remifayolle.android.shopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class ShopperDbAdapter {

	// Table name
    private static final String DATABASE_TABLE = "shoppertable";

	/* V1 attributes */
    public static final String KEY_DESC = "desc";
    public static final String KEY_ROWID = "_id";
    
    /* v3 attributes */
    public static final String KEY_ISDONE = "isdone";

    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;
    //private static final String TAG = "ShopperDbAdapterTAG";
    

    /**
     * DatabaseHelper
     *
     * My implementation of SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
    	private static final String DATABASE_NAME = "shopperdb";
        private static final int DATABASE_VERSION = 3;
        
        /* V1 and V2 database creation statement */
        @SuppressWarnings("unused")
		private static final String DATABASE_CREATEv2 = "CREATE TABLE " + DATABASE_TABLE +
        	"(" +
        	KEY_ROWID + " integer primary key autoincrement, " +
        	KEY_DESC + " text not null " +
        	");";
        
        /* V3 database creation statement */
        private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE +
            "(" +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_DESC + " text not null, " +
            KEY_ISDONE + " boolean " +
            ");";

        /**
         * Constructor
         */
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        /**
         * Create table in SQLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }


        /**
         * Upgrade table if version has changed
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	
        	for (int i = oldVersion; i < newVersion; i++)
        	{
        		switch(i)
        		{
        			case 1:
        				/*
        				 * Upgrade from version 1 to version 2
        				 * 
        				 * Simple deletion of current table (this is really BAD),
        				 * and creation from start of a new one.
        				 */
        	            //Log.w(TAG,"Upgrading database from version "+oldVersion+" to "+newVersion+", which will destroy all old data");
        	            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        	            onCreate(db);
        	            break;
        	            
        			case 2:
        				/*
        				 * Upgrade from version 2 to version 3
        				 * 
        				 * Simple deletion of current table (this is really BAD),
        				 * and creation from start of a new one.
        				 */
        				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD " + KEY_ISDONE + " boolean");
        				break;
        		}
        	}
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ShopperDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ShopperDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new item using the attributes provided. If the item is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param desc the item description
     * @return rowId or -1 if failed
     */
    public long createItem(String desc) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESC, desc);
        initialValues.put(KEY_ISDONE, false);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the item with the given rowId
     * 
     * @param rowId id of item to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all items in the database
     * 
     * @return Cursor over all items
     */
    public Cursor fetchAllItems() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DESC, KEY_ISDONE},
        		null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the item that matches the given rowId
     * 
     * @param rowId id of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_DESC, KEY_ISDONE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the item using the details provided. The item to be updated is
     * specified using the rowId, and it is altered to use the attributes
     * values passed in
     * 
     * @param rowId id of item to update
     * @param desc value to set item body to
     * @param isdone value to set
     * @return true if the item was successfully updated, false otherwise
     */
    public boolean updateItem(long rowId, String desc, boolean isdone) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESC, desc);
        args.put(KEY_ISDONE, false);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}

