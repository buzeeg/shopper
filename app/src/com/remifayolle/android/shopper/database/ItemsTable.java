package com.remifayolle.android.shopper.database;

/**
 * <b>ItemsTable</b>
 * <p>
 * Items database informations :
 * <ul>
 * <li>Database file name</li>
 * <li>Database version</li>
 * <li>Table name</li>
 * <li>Available columns</li>
 * </ul>
 * </p>
 * @author rem
 */
public class ItemsTable {

    /**
	 * General informations about hits database
	 */
    public static final String DATABASE_NAME = "shopperdb";
    public static final int DATABASE_VERSION = 3;
    public static final String TABLE_NAME = "shoppertable";


    /**
     * Available columns
     */
    /* v1 attributes */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESC = "desc";

    /* v3 attributes */
    public static final String COLUMN_ISDONE = "isdone";


    /**
     * SQL statement for database creation
     * v3 database creation statement
     */
    public static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME +
            "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_DESC + " text not null, " +
            COLUMN_ISDONE + " boolean " +
            ");";

    /* V1 and V2 database creation statement */
    @SuppressWarnings("unused")
    private static final String DATABASE_CREATEv2 = "CREATE TABLE " + TABLE_NAME +
            "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_DESC + " text not null " +
            ");";


    /**
     * Available columns in the database for query (current version)
     */
    public static final String[] AVAILABLE_COLUMNS = {
        COLUMN_ID,
        COLUMN_DESC,
        COLUMN_ISDONE
    };

}
