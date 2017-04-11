package com.bskup.ginventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bskup.ginventory.data.ItemContract.ItemEntry;

// Manages database creation and version management for ginventory app
public class ItemDbHelper extends SQLiteOpenHelper {
    // Name of inventory items database file
    private static final String INVENTORY_ITEMS_DATABASE_FILE_NAME = "inventory.db";
    // Database version, if schema changes this must be incremented
    private static final int INVENTORY_ITEMS_DATABASE_VERSION = 1;

    // Constructor passing in the file to open and version
    public ItemDbHelper(Context context) {
        super(context, INVENTORY_ITEMS_DATABASE_FILE_NAME, null, INVENTORY_ITEMS_DATABASE_VERSION);
    }

    // Called when database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventory items table
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SIZE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SIZE_TYPE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_ORIGIN + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_ABV + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PURCHASE_PRICE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SALE_PRICE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SPIRIT_TYPE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_NOTES + " TEXT, "
                + ItemEntry.COLUMN_ITEM_TARGET_QUANTITY + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PHOTO_PATH + " TEXT);";
        // Execute the SQL statement that creates the inventory items table
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    // Called when database version is upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}