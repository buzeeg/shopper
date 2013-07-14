package com.remifayolle.android.shopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.remifayolle.android.shopper.database.ItemsTable;
//import android.util.Log;

public class ShopperDbAdapter {

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

        /**
         * Constructor
         */
        public DatabaseHelper(Context context) {
            super(context, ItemsTable.DATABASE_NAME, null, ItemsTable.DATABASE_VERSION);
        }


        /**
         * Create table in SQLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(ItemsTable.DATABASE_CREATE);
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
        	            db.execSQL("DROP TABLE IF EXISTS " + ItemsTable.TABLE_NAME);
        	            onCreate(db);
        	            break;
        	            
        			case 2:
        				/*
        				 * Upgrade from version 2 to version 3
        				 * 
        				 * Simple deletion of current table (this is really BAD),
        				 * and creation from start of a new one.
        				 */
        				db.execSQL("ALTER TABLE " + ItemsTable.TABLE_NAME + " ADD " + ItemsTable.COLUMN_ISDONE + " boolean");
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
        initialValues.put(ItemsTable.COLUMN_DESC, desc);
        initialValues.put(ItemsTable.COLUMN_ISDONE, false);

        return mDb.insert(ItemsTable.TABLE_NAME, null, initialValues);
    }

    /**
     * Delete the item with the given rowId
     * 
     * @param rowId id of item to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {
        return mDb.delete(ItemsTable.TABLE_NAME, ItemsTable.COLUMN_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all items in the database
     * 
     * @return Cursor over all items
     */
    public Cursor fetchAllItems() {

        return mDb.query(ItemsTable.TABLE_NAME, new String[] {ItemsTable.COLUMN_ID, ItemsTable.COLUMN_DESC, ItemsTable.COLUMN_ISDONE},
        		null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the item that matches the given rowId
     * 
     * @param rowId id of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws android.database.SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, ItemsTable.TABLE_NAME, new String[] {ItemsTable.COLUMN_ID,
                    ItemsTable.COLUMN_DESC, ItemsTable.COLUMN_ISDONE}, ItemsTable.COLUMN_ID + "=" + rowId, null,
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
        args.put(ItemsTable.COLUMN_DESC, desc);
        args.put(ItemsTable.COLUMN_ISDONE, false);

        return mDb.update(ItemsTable.TABLE_NAME, args, ItemsTable.COLUMN_ID + "=" + rowId, null) > 0;
    }
}

