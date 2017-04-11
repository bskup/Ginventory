package com.bskup.ginventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bskup.ginventory.data.ItemContract.ItemEntry;

import static android.R.attr.id;

public class ItemProvider extends ContentProvider {
    // Tag for log messages
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();
    // Constants for use with UriMatcher
    // Whole inventory table
    private static final int INVENTORY = 100;
    // Singe inventory item from inventory table
    private static final int INVENTORY_ID = 101;
    // UriMatcher object to match a content URI to a corresponding code constant
    // The input passed into the constructor is the code to return for the root URI
    // (NO_MATCH is commonly used here)
    // sVariable for "static"
    // (initialized when class is loaded, belongs to class not instance of class)
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. Runs first time anything is called from this class
    // Adds patterns this provider should recognize
    static {
        // Calls to AddUri(String authority, String path, int code)
        // One for each pattern the provider should recognize
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }
    // Create ItemDbHelper global variable, so it can be referenced from other
    // ContentProvider methods
    private ItemDbHelper mDbHelper;

    // Initialize the provider and dbHelper object
    @Override
    public boolean onCreate() {
        // Initialize a PetDbHelper object to gain access to the pets database
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    // Perform query for the given URI. Use projection, selection, selectionArgs, and sortOrder
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Cursor to hold result of query
        Cursor cursor;
        // int variable for code UriMatcher will return from .match
        int match = sUriMatcher.match(uri);
        // Switch statement based on result of .match
        switch (match) {
            case INVENTORY:
                // Query the whole table, could return multiple rows
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // Extract ID from uri and query db using extracted id in selection/selection args
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                // Perform the query, returning a cursor
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                // URI doesn't match one of our predefined patterns
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // This query method is called from the loader in CatalogActivity when it is first created,
        // but the data won't update until the loader is recreated, or it is told to manually.
        // This sets a listener on the Uri passed into this query method that detects whenever the
        // data at that Uri changes, and tells the loader to reload the data in the background
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Insert data into db via provider - Uses insertItem() helper method
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        // Check the uri pattern
        final int match = sUriMatcher.match(uri);
        // If uri matches full table pattern, perform insertion using insertItem method
        switch (match) {
            case INVENTORY:
                // Notify listener that data has changed before returning uri
                getContext().getContentResolver().notifyChange(uri, null);
                // Inserts new row and returns uri with appended id
                return insertItem(uri, contentValues);
            // Can only insert a new row into a table, not into another row,
            // so all other URI patterns are not allowed
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert item into database with given content values. Returns new content URI for that row
    private Uri insertItem(Uri uri, ContentValues values) {
        // Sanity check the attributes in ContentValues before proceeding
        // Check that the name is not null
        String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }
        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Item requires a quantity");
        }
        // Check that the size is not null
        Integer size = values.getAsInteger(ItemEntry.COLUMN_ITEM_SIZE);
        if (size == null) {
            throw new IllegalArgumentException("Item requires a size");
        }
        // Check that the size type is not null
        Integer sizeType = values.getAsInteger(ItemEntry.COLUMN_ITEM_SIZE_TYPE);
        if (sizeType == null) {
            throw new IllegalArgumentException("Item requires a size type");
        }
        // Check that the origin is not null
        String origin = values.getAsString(ItemEntry.COLUMN_ITEM_ORIGIN);
        if (origin == null) {
            throw new IllegalArgumentException("Item requires an origin");
        }
        // Check that the abv is not null
        Integer abv = values.getAsInteger(ItemEntry.COLUMN_ITEM_ABV);
        if (abv == null) {
            throw new IllegalArgumentException("Item requires an ABV");
        }
        // Check that the purchase price is not null
        Integer purchasePrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE);
        if (purchasePrice == null) {
            throw new IllegalArgumentException("Item requires a purchase price");
        }
        // Check that the sale price is not null
        Integer salePrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_SALE_PRICE);
        if (salePrice == null) {
            throw new IllegalArgumentException("Item requires a sale price");
        }
        // Check that the supplier name is not null
        String supplierName = values.getAsString(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Item requires a supplier name");
        }
        // Check that the supplier phone number is not null
        Long supplierPhoneNumber = values.getAsLong(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumber == null) {
            throw new IllegalArgumentException("Item requires a supplier phone number");
        }
        // Check that the target quantity is not null
        Integer targetQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_TARGET_QUANTITY);
        if (targetQuantity == null) {
            throw new IllegalArgumentException("Item requires a target quantity");
        }

        // Check that quantity >= 0, at this point we already know if it is null
        if (quantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity");
        }
        // Check that size >= 0, at this point we already know if it is null
        if (size < 0) {
            throw new IllegalArgumentException("Item requires a valid size");
        }
        // Check that abv >= 0, at this point we already know if it is null
        if (abv < 0) {
            throw new IllegalArgumentException("Item requires a valid ABV");
        }
        // Check that purchase price >= 0, at this point we already know if it is null
        if (purchasePrice < 0) {
            throw new IllegalArgumentException("Item requires a valid purchase price");
        }
        // Check that sale price >= 0, at this point we already know if it is null
        if (salePrice < 0) {
            throw new IllegalArgumentException("Item requires a valid sale price");
        }
        // Check that target quantity >= 0, at this point we already know if it is null
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("Item requires a valid target quantity");
        }
        // Check that spirit type is valid
        Integer spiritTypeToCheck = values.getAsInteger(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE);
        if (spiritTypeToCheck == null || !ItemEntry.isValidSpiritType(spiritTypeToCheck)) {
            throw new IllegalArgumentException("Item requires a valid spirit type");
        }
        // Check that size type is valid
        Integer sizeTypeToCheck = values.getAsInteger(ItemEntry.COLUMN_ITEM_SIZE_TYPE);
        if (sizeTypeToCheck == null || !ItemEntry.isValidSizeType(sizeTypeToCheck)) {
            throw new IllegalArgumentException("Item requires a valid size type");
        }
        // Insert a new item into the inventory database table with the given ContentValues
        // Get writable database object
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Insert a new row for item in database, returning the ID of that new row
        long newRowId = db.insert(ItemEntry.TABLE_NAME, null, values);
        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            Toast.makeText(getContext(), "Error with saving item", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID
            Toast.makeText(getContext(), "Item saved", Toast.LENGTH_SHORT).show();
        }
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    // Updates the data at given uri filtered by selection and selection args
    // New data comes from ContentValues object
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Helper method to update item or items in database based on selection and selectionArgs
    // Uri is passed in so we can notify the listener set on that uri that data has changed
    // Returns the number of rows that were successfully updated
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check if ContentValues object contains name key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            // Check that the name is not null
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        // Check if ContentValues object contains quantity key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that quantity is valid
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity");
            }
        }
        // Check if ContentValues object contains size key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SIZE)) {
            // Check that size is valid
            Integer size = values.getAsInteger(ItemEntry.COLUMN_ITEM_SIZE);
            if (size != null && size < 0) {
                throw new IllegalArgumentException("Item requires a valid size");
            }
        }
        // Check if ContentValues object contains size type key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SIZE_TYPE)) {
            // Check that the size type is valid
            Integer sizeTypeToCheck = values.getAsInteger(ItemEntry.COLUMN_ITEM_SIZE_TYPE);
            if (sizeTypeToCheck == null || !ItemEntry.isValidSizeType(sizeTypeToCheck)) {
                throw new IllegalArgumentException("Item requires a valid size type");
            }
        }
        // Check if ContentValues object contains origin key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_ORIGIN)) {
            // Check that the origin is not null
            String origin = values.getAsString(ItemEntry.COLUMN_ITEM_ORIGIN);
            if (origin == null) {
                throw new IllegalArgumentException("Item requires an origin");
            }
        }
        // Check if ContentValues object contains abv key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_ABV)) {
            // Check that abv is valid
            Integer abv = values.getAsInteger(ItemEntry.COLUMN_ITEM_ABV);
            if (abv != null && abv < 0) {
                throw new IllegalArgumentException("Item requires a valid ABV");
            }
        }
        // Check if ContentValues object contains purchase price key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE)) {
            // Check that purchase price is valid
            Integer purchasePrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE);
            if (purchasePrice != null && purchasePrice < 0) {
                throw new IllegalArgumentException("Item requires a valid purchase price");
            }
        }
        // Check if ContentValues object contains sale price key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SALE_PRICE)) {
            // Check that sale price is valid
            Integer salePrice = values.getAsInteger(ItemEntry.COLUMN_ITEM_SALE_PRICE);
            if (salePrice != null && salePrice < 0) {
                throw new IllegalArgumentException("Item requires a valid sale price");
            }
        }
        // Check if ContentValues object contains supplier name key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME)) {
            // Check that the supplier name is not null
            String supplierName = values.getAsString(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }
        // Check if ContentValues object contains supplier phone number key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER)) {
            // Check that supplier phone number is valid
            // Long variable instead of long so we can check for null
            // Primitive types (long) must be initialized to some value and will never be null
            Long supplierPhoneNumber = values.getAsLong(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);
            if (supplierPhoneNumber != null && supplierPhoneNumber < 0) {
                throw new IllegalArgumentException("Item requires a valid supplier phone number");
            }
        }
        // Check if ContentValues object contains spirit type key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE)) {
            // Check that the spirit type is valid
            Integer spiritTypeToCheck = values.getAsInteger(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE);
            if (spiritTypeToCheck == null || !ItemEntry.isValidSpiritType(spiritTypeToCheck)) {
                throw new IllegalArgumentException("Item requires a valid spirit type");
            }
        }
        // Check if ContentValues object contains target quantity key
        // If it does, check it for validity
        if (values.containsKey(ItemEntry.COLUMN_ITEM_TARGET_QUANTITY)) {
            // Check that target quantity is valid
            Integer targetQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_TARGET_QUANTITY);
            if (targetQuantity != null && targetQuantity < 0) {
                throw new IllegalArgumentException("Item requires a valid target quantity");
            }
        }
        // Check if the ContentValues objects size is 0
        // If it is 0, return 0 instead of wasting resources doing database call
        if (values.size() == 0) {
            return 0;
        }
        // If we are still going at this point, size is larger than 0
        // So we can get a writable database object and perform the update operation
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Perform update operation, return the number of rows that were affected
        int rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were successfully updated, notify listener that data changed
        if (rowsUpdated != 0) {
            // Notify listener that data has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // Deletes the data at given selection and selectionArgs
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Number of rows successfully deleted
        int rowsDeleted;
        // Use uriMatcher to determine if the uri we pass in refers to a whole table or single row
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Perform delete operation, return the number of rows that were affected
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were successfully deleted, notify listener that data changed
                if (rowsDeleted != 0) {
                    // Notify listener that data has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // Perform delete operation, return the number of rows that were affected
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were successfully deleted, notify listener that data changed
                if (rowsDeleted != 0) {
                    // Notify listener that data has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    // Returns the MIME type (data type) for the given content Uri
    // MIME type "dir" or directory is for a whole list or table
    // MIME type "item" is a single row or item
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Return MIME type for a list of items (dir)
                return ItemEntry.CONTENT_TYPE_LIST;
            case INVENTORY_ID:
                // Return MIME type for a single inventory item (item)
                return ItemEntry.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match );
        }
    }
}
