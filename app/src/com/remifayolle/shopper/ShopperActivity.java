package com.remifayolle.shopper;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
//import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ShopperActivity extends ListActivity {

	private ShopperDbAdapter mDbHelper;
	private ImageButton mAddButton;
	private EditText mInput;
	//private ShareActionProvider mShareActionProvider;

    private static final int DELETE_ID = Menu.FIRST;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // init data
        mDbHelper = new ShopperDbAdapter(this);
        mDbHelper.open();
        fillData();
        mDbHelper.close();
        
        registerForContextMenu(getListView());
        
        mInput = (EditText) findViewById(R.id.editor);
        
        mAddButton = (ImageButton) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        		if(mInput.getText().length()>0)
        		{
        			// add new object to DB
        			mDbHelper.createItem(mInput.getText().toString());

        			// clean edit text
        			mInput.setText(null);
        		
        			// update list
        			fillData();
        		}
        	};
        });
        
    }

    

    @Override
	protected void onPause() {
		mDbHelper.close();
		super.onPause();
	}

	@Override
	protected void onStop() {
		//mDbHelper.close();
		super.onStop();
	}

	@Override
	protected void onResume() {
        mDbHelper.open();
		super.onResume();
	}

	@Override
	protected void onRestart() {
        
		// init data
        mDbHelper = new ShopperDbAdapter(this);
        mDbHelper.open();
        fillData();
        mDbHelper.close();
        
		super.onRestart();
	}
	

	private void fillData() {
		// Get all of the rows from the database and create the item list
        Cursor itemCursor = mDbHelper.fetchAllItems();
        //startManagingCursor(itemCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{ShopperDbAdapter.KEY_DESC};
        
        // Create an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.item_text};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.item, itemCursor, from, to);
        setListAdapter(items);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}

	
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.one_delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteItem(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                clearList();
                return true;
            case R.id.menu_share:
            	shareList();
            	return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }



	private void clearList() {
		// Get all of the rows from the database and create the item list
        Cursor itemCursor = mDbHelper.fetchAllItems();
        if (itemCursor.moveToFirst()) {
    		mDbHelper.deleteItem(itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)));
        	while (itemCursor.moveToNext()) {
        		mDbHelper.deleteItem(itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)));
        	}
        }
        
        // Refresh the list
		fillData();
	}
	
	

	private void shareList() {
		//Toast.makeText(this, "Share!!", Toast.LENGTH_SHORT).show();
		StringBuilder toShare = new StringBuilder(getString(R.string.share_begin));
        Cursor itemCursor = mDbHelper.fetchAllItems();
        if (itemCursor.moveToFirst()) {
    		toShare.append("\n-");
    		toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
        	while (itemCursor.moveToNext()) {
        		toShare.append("\n-");
        		toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
        	}
        }
		
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
    	shareIntent.setType("text/plain");
    	shareIntent.putExtra(Intent.EXTRA_TEXT,toShare.toString());
    	startActivity(Intent.createChooser(shareIntent,getString(R.string.share_menu_title)));
	}
}
