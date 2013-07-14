package com.remifayolle.android.shopper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for Items database creation and management.
 * @author rem
 */
public class ItemsDatabaseHelper extends SQLiteOpenHelper {

    /**
     * TAG to display traces
     */
    private static final String TAG = ItemsDatabaseHelper.class.getSimpleName();


    /**
     * Create a helper object to create/access/manage the ItemsDatabase
     * @param context to use to open or create the database
     */
    public ItemsDatabaseHelper(Context context) {
        super(context, ItemsTable.DATABASE_NAME, null, ItemsTable.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ItemsTable.DATABASE_CREATE);
    }

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
