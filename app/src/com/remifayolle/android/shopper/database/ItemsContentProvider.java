package com.remifayolle.android.shopper.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.remifayolle.android.shopper.BuildConfig;

import java.util.Arrays;
import java.util.HashSet;

/**
 * ItemsContentProvider
 * <br><br>
 * Provides access to the items database.
 * <br>
 * Use it with a Loader to perform asynchronous tasks.
 * @author rem
 */
public class ItemsContentProvider extends ContentProvider {

    /**
     * TAG to display traces
     * @return
     */
    private static final String TAG = ItemsContentProvider.class.getSimpleName();


    // ItemsContentProvider Uri
    private static final String AUTHORITY = BuildConfig.AUTHORITY; //"com.remifayolle.android.shopper.database.itemscontentprovider";
    private static final String BASE_PATH = "items";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);


    /**
     * Database helper - provider access to database.
     */
    private ItemsDatabaseHelper mDBHelper;


    /**
     * UriMatcher
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ITEMS = 10;
    private static final int ITEM_ID = 20;
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH+"/#", ITEM_ID);
    }



    @Override
    public boolean onCreate() {
        mDBHelper = new ItemsDatabaseHelper(getContext());
        return true;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if caller has requested a column which does exists
        checkColumnsForQuery(projection);

        // Set the table
        queryBuilder.setTables(ItemsTable.TABLE_NAME);

        // Set where clause
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ITEMS:
                break;
            case ITEM_ID:
                queryBuilder.appendWhere(ItemsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Execute query
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

            // Make sure potential listeners are getting notified
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.e(TAG, "getReadableDatabase or query failed - e:" + e.getMessage());
            e.printStackTrace();
        }

        return cursor;
    }



    @Override
    public String getType(Uri uri) {
        Log.w(TAG, "getType not implemented in ContentProvider");
        return null;
    }



    /**
     * Insert input values in Items database.
     * <br><br>
     * How to use it :
     * <code>
     * <p>
     * <br>ContentValues values = new ContentValues();
     * <br>values.put(TodoTable.COLUMN_CATEGORY, category);
     * <br>values.put(TodoTable.COLUMN_SUMMARY, summary);
     * <br>values.put(TodoTable.COLUMN_DESCRIPTION, description);
     * <br>
     * <br>if (todoUri == null) {
     * <br>  // New todo
     * <br>  todoUri = getContentResolver().insert(MyTodoContentProvider.CONTENT_URI, values);
     * <br>} else {
     * <br>  // Update todo
     * <br>  getContentResolver().update(todoUri, values, null, null);
     * <br>}
     * </p>
     * </code>
     * see http://www.vogella.com/articles/AndroidSQLite/article.html#todo_activities
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        // Check if caller has requested a column which does exists
        checkValuesForInsert(values);

        // Insert input value in database
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case ITEMS:
                id = db.insert(ItemsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        // Notify ContentResolver that a chanage have been done
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the id of the newly created row
        return Uri.parse(CONTENT_URI + "/" + id);
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int rowsdeleted = 0;
        switch (uriType) {
            case ITEMS:
                rowsdeleted = db.delete(ItemsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsdeleted = db.delete(ItemsTable.TABLE_NAME,
                            ItemsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsdeleted = db.delete(ItemsTable.TABLE_NAME,
                            ItemsTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                Log.w(TAG, "Unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsdeleted;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.w(TAG, "update not implemented in ContentProvider");
        return 0;
    }



    private void checkColumnsForQuery(String[] projection) {
        if(projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ItemsTable.AVAILABLE_COLUMNS));
            if (!availableColumns.containsAll(requestedColumns)) {
                Log.e(TAG, "Unknown columns in projection");
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }



    private void checkValuesForInsert(ContentValues values) {
        Log.w(TAG, "checkValuesForInsert not implemented in ContentProvider");
    }
}
