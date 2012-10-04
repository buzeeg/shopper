package com.remifayolle.android.shopper;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.SimpleCursorAdapter;
//import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ShopperActivity extends ListActivity {

	private ShopperDbAdapter mDbHelper;
	private ImageButton mAddButton;
	private EditText mInput;

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
        mInput.setOnEditorActionListener(new OnEditorActionListener(){
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        		if (actionId == EditorInfo.IME_ACTION_DONE) {
        			mAddButton.performClick();
                    return true;
                }
				return false;
			}
		});
        
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
        
        // save information about if item are done or not
        int[] isDoneArray = new int[itemCursor.getCount()];
        if (itemCursor.moveToFirst()) {
        	int i=0;
        	isDoneArray[i] = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
        	while (itemCursor.moveToNext()) {
        		i++;
        		isDoneArray[i] = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
        	}
        }
        itemCursor.moveToFirst();
        

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{ShopperDbAdapter.KEY_DESC};
        
        // Create an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.item_text};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.item, itemCursor, from, to);
        setListAdapter(items);
        
        // update listview items with adding striketrough if item are done
        // UGLY but working
        ListView lv = getListView();
        if (lv != null)
        {
        	if(isDoneArray.length == lv.getChildCount())
        	{	
        		for (int j=0; j<lv.getChildCount(); j++)
		        {
        			if(isDoneArray[j]>0)
        			{
        				View childView = lv.getChildAt(j);
        				TextView text = (TextView) childView.findViewById(R.id.item_text);
        				text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        			}
        		}
        	}
        }
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}

	
	/**
	 * Create contextual menu on list items
	 */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.one_delete);
    }


    /**
     * Contextual menu on item
     */
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


    /**
     * Click on item
     */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// get TextView to update
		TextView text = (TextView) v.findViewById(R.id.item_text);
		
		// get info on item to update
		Cursor c = mDbHelper.fetchAllItems();
		c.moveToPosition(position);
		
		// check if item in UI is selected (in fact if text is in strike through mode)
		if ((text.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) != 0)
		{
			// item is selected -> reset it + update DB
			text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			mDbHelper.updateItem(c.getInt(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)), c.getString(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)), false);
		}
		else
		{
			// item is not selected -> select it + update DB
			text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			mDbHelper.updateItem(c.getInt(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)), c.getString(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)), true);
		}
		
		super.onListItemClick(l, v, position, id);
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
        	int isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
        	if(isdone==0)
    		{
        		toShare.append("\n-");
    			toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
    		}
        	while (itemCursor.moveToNext()) {
        		isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
            	if(isdone==0)
        		{
            		toShare.append("\n-");
            		toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
        		}
        	}
        }
		
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
    	shareIntent.setType("text/plain");
    	shareIntent.putExtra(Intent.EXTRA_TEXT,toShare.toString());
    	startActivity(Intent.createChooser(shareIntent,getString(R.string.share_menu_title)));
	}
}
