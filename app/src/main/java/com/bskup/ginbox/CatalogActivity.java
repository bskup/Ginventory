package com.bskup.ginbox;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bskup.ginbox.data.ItemContract.ItemEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Tag for log messages
    public static String LOG_TAG = SettingsActivity.class.getName();

    // Global variable so it can be swapped from multiple loader methods
    private ItemCursorAdapter mItemCursorAdapter;
    // Loader ID
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Theme change based on preference
        String themeName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "default");
        Log.v(LOG_TAG, "value for theme String in CatalogActivity oncreate: " + themeName);
        if (themeName.equals("AppThemeLight")) {
            setTheme(R.style.AppThemeLight);
        } else if (themeName.equals("AppThemeDark")) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_catalog);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // Find the ListView which will be populated with the inventory item data
        ListView listViewItems = (ListView) findViewById(R.id.list_view_inventory_items);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        listViewItems.setEmptyView(findViewById(R.id.empty_view));
        // Create adapter, there is no data yet until load finishes so pass in null for the Cursor
        mItemCursorAdapter = new ItemCursorAdapter(this, null);
        // Attach adapter to listView
        listViewItems.setAdapter(mItemCursorAdapter);
        // Setup on item click listener
        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass selected item's Uri to the intent which opens editor activity
                // Create new intent
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Create Uri by using base uri and appending the id passed into onItemClick
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                // Add created Uri to the data field of the intent
                intent.setData(currentItemUri);
                // Open editor activity with intent containing clicked item's uri
                startActivity(intent);
            }
        });
        // Initialize loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define projection of columns we will actually use with this loader
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SIZE,
                ItemEntry.COLUMN_ITEM_SIZE_TYPE,
                ItemEntry.COLUMN_ITEM_ORIGIN,
                ItemEntry.COLUMN_ITEM_ABV,
                ItemEntry.COLUMN_ITEM_PURCHASE_PRICE,
                ItemEntry.COLUMN_ITEM_SALE_PRICE,
                ItemEntry.COLUMN_ITEM_SPIRIT_TYPE,
                ItemEntry.COLUMN_ITEM_SUPPLIER_NAME,
                ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER,
                ItemEntry.COLUMN_ITEM_NOTES,
                ItemEntry.COLUMN_ITEM_TARGET_QUANTITY,
                ItemEntry.COLUMN_ITEM_PHOTO_PATH };
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update ItemCursorAdapter with new cursor containing updated item data
        mItemCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Remove all cursor data by swapping in a null reference
        mItemCursorAdapter.swapCursor(null);
    }

    // Helper method to show Delete All confirmation dialog
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Yes, delete all" button, delete all the items
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Helper method to perform deletion of all items in database
    private void deleteAllItems() {
        // Delete all rows based on uri using ContentResolver, returns # of rows deleted
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        // Show a toast message depending on whether or not the delete was successful
        if (rowsDeleted == 0) {
            // If no rows deleted, there was an error with delete
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a success toast
            Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to insert hardcoded item data into the database for debugging purposes
    private void insertDummyItem() {
        // Create a ContentValues object where column names are the keys,
        // and inventory item's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Tanqueray London Dry");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, "32");
        values.put(ItemEntry.COLUMN_ITEM_ORIGIN, "United Kingdom");
        values.put(ItemEntry.COLUMN_ITEM_SIZE, "750");
        values.put(ItemEntry.COLUMN_ITEM_SIZE_TYPE, ItemEntry.SIZE_TYPE_ML);
        values.put(ItemEntry.COLUMN_ITEM_ABV, "40");
        values.put(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE, "799");
        values.put(ItemEntry.COLUMN_ITEM_SALE_PRICE, "1299");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, "Sysco");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, "1112223333");
        values.put(ItemEntry.COLUMN_ITEM_NOTES, "Test notes. This is a test note string. Lorem ipsum dolor sit amet.");
        values.put(ItemEntry.COLUMN_ITEM_TARGET_QUANTITY, "100");
        values.put(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE, ItemEntry.SPIRIT_TYPE_GIN);
        // Insert a new row for our test item using ContentResolver, returns uri of new row
        getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Show confirmation dialog making sure user wants to delete all pets
                showDeleteAllConfirmationDialog();
                return true;
            case R.id.action_settings:
                // Open Settings activity
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // When activity resumes, check if preferences changed and if so, recreate activity
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume called");
        Log.v(LOG_TAG, "GinventoryPreferenceFragment.mPreferencesChanged: " + SettingsActivity.GinventoryPreferenceFragment.mPreferencesChanged);
        if (SettingsActivity.GinventoryPreferenceFragment.mPreferencesChanged != null) {
            if (SettingsActivity.GinventoryPreferenceFragment.mPreferencesChanged) {
                // Restart the loader
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                // Recreate activity with new preferences applied
                recreate();
                Log.v(LOG_TAG, "recreate() called from onResume, setting mPreferencesChanged to false");
                SettingsActivity.GinventoryPreferenceFragment.mPreferencesChanged = false;
            }
        }
    }
}
