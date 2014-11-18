package com.remifayolle.android.shopper;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.remifayolle.android.shopper.database.ItemsContentProvider;
import com.remifayolle.android.shopper.database.ItemsTable;

/**
 * @author rem
 */
public class ShopperMainFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter = null;
    private ImageButton mAddButton = null;
    private EditText mInput = null;
    private static final int DELETE_ID = Menu.FIRST;

    /**
     * Fragment creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Fill data
        fillData();
    }



    /**
     * View creation
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main, container, false);
    }

    /**
	 * View has just been created
	 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerForContextMenu(getListView());

        mInput = (EditText) view.findViewById(R.id.editor);
        if(mInput != null) {
            // set padding by code for pre-lollipop devices (http://code.google.com/p/android/issues/detail?id=77982)
            final float scale = getResources().getDisplayMetrics().density; // Get the screen's density scale
            int leftInDp = (int) (16 * scale + 0.5f);
            int topInDp = (int) (8 * scale + 0.5f);
            int othersInDp = (int) (4 * scale + 0.5f);
            mInput.setPadding(leftInDp,topInDp,othersInDp,othersInDp);

            mInput.setOnEditorActionListener(new TextView.OnEditorActionListener(){
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        mAddButton.performClick();
                        return true;
                    }
                    return false;
                }
            });
        }

        mAddButton = (ImageButton) view.findViewById(R.id.add_button);
        if(mAddButton != null) {
            mAddButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    if(mInput != null && mInput.getText() != null) {
                        if(mInput.getText().length()>0) {
                            // add new object to DB
                            ContentValues values = new ContentValues();
                            values.put(ItemsTable.COLUMN_DESC, mInput.getText().toString());
                            getActivity().getContentResolver().insert(ItemsContentProvider.CONTENT_URI, values);

                            // clean edit text
                            mInput.setText(null);
                        }
                    }
                }
            });
        }
    }

    /**
     * Init the loader to get data in the list
     */
    private void fillData() {
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{ItemsTable.COLUMN_DESC};

        // Create an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.item_text};

        // Init loader
        getLoaderManager().initLoader(0, null, this);

        // Create adapter
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item, null, from, to, 0);

        // Set adapter to list
        setListAdapter(mAdapter);
    }


    /**
     * Action bar menu creation
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    /**
     * Action bar menu action selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                clearList();
                return true;
            case R.id.menu_share:
                shareList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Delete all items
     */
    private void clearList() {
        getActivity().getContentResolver().delete(ItemsContentProvider.CONTENT_URI, null, null);
    }

    /**
     * Share current items list
     */
    private void shareList() {
        StringBuilder toShare = new StringBuilder(getString(R.string.share_begin));
        Cursor itemCursor = mAdapter.getCursor();
        if (itemCursor != null) {
            if (itemCursor.moveToFirst()) {
                int isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COLUMN_ISDONE));
                if(isdone==0)
                {
                    toShare.append("\n-");
                    toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ItemsTable.COLUMN_DESC)));
                }
                while (itemCursor.moveToNext()) {
                    isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COLUMN_ISDONE));
                    if(isdone==0)
                    {
                        toShare.append("\n-");
                        toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ItemsTable.COLUMN_DESC)));
                    }
                }
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,toShare.toString());
        startActivity(Intent.createChooser(shareIntent,getString(R.string.share_menu_title)));
    }



    /**
     * Click on item
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO : to implement

        super.onListItemClick(l, v, position, id);
    }



    /**
     * Create contextual menu on list items
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.one_delete);
    }

    /**
     * Contextual menu on item
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Uri deleteUri = Uri.parse(ItemsContentProvider.CONTENT_URI + "/" + info.id);
                getActivity().getContentResolver().delete(deleteUri, null, null);
                return true;
        }
        return super.onContextItemSelected(item);
    }



    /**
	 * Creates a new loader after the initLoader() call
	 */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = { ItemsTable.COLUMN_ID, ItemsTable.COLUMN_DESC, ItemsTable.COLUMN_ISDONE };
        return new CursorLoader(getActivity(), ItemsContentProvider.CONTENT_URI, projection, null, null, null);
    }

    /**
     * Async load is finished
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    /**
     * Loader has been reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
