package com.bskup.ginventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static android.text.style.TtsSpan.GENDER_FEMALE;
import static android.text.style.TtsSpan.GENDER_MALE;

public class ItemContract extends AppCompatActivity {

    // Content authority
    public static final String CONTENT_AUTHORITY = "com.bskup.ginventory";
    // Base content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Path to inventory items table
    public static final String PATH_INVENTORY = "inventory";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor
    private ItemContract() {}

    // Inner class that defines constants for the inventory database table
    // Each entry represents a single item
    public static final class ItemEntry implements BaseColumns {
        // Name of database table for inventory items
        public final static String TABLE_NAME = "inventory";
        // Complete content Uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        // MIME type of the Content URI for a list of inventory items (MIME type=dir)
        // Uses a built in constant from contentResolver class
        public static final String CONTENT_TYPE_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        // MIME type of the Content URI for a single inventory item (MIME type=item)
        // Uses a built in constant from contentResolver class
        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        // Database column name Strings
        // Unique ID for each inventory item for use in the database table, SQL data type: INTEGER
        public final static String _ID = BaseColumns._ID;
        // Name of the inventory item, SQL data type: TEXT
        public final static String COLUMN_ITEM_NAME ="name";
        // Quantity of items in stock, SQL data type: INTEGER
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        // Size of item in fl oz or ml, SQL data type: INTEGER
        public final static String COLUMN_ITEM_SIZE = "size";
        // Size type of the item, SQL data type: INTEGER
        public final static String COLUMN_ITEM_SIZE_TYPE = "sizetype";
        // Origin country of the inventory item, SQL data type: TEXT
        public final static String COLUMN_ITEM_ORIGIN ="origin";
        // Alcohol by volume, SQL data type: INTEGER
        public final static String COLUMN_ITEM_ABV = "abv";
        // Purchase price we paid, SQL data type: INTEGER
        public final static String COLUMN_ITEM_PURCHASE_PRICE = "purchaseprice";
        // Sale price item is being sold for, SQL data type: INTEGER
        public final static String COLUMN_ITEM_SALE_PRICE = "saleprice";
        // Spirit type of the item, SQL data type: INTEGER
        public final static String COLUMN_ITEM_SPIRIT_TYPE = "spirittype";
        // Supplier name saved as String, SQL data type: TEXT
        public final static String COLUMN_ITEM_SUPPLIER_NAME = "suppliername";
        // Supplier phone number saved as long, SQL data type: INTEGER
        public final static String COLUMN_ITEM_SUPPLIER_PHONE_NUMBER = "supplierphonenumber";
        // Uri of item photo saved as String, SQL data type: TEXT
        public final static String COLUMN_ITEM_PHOTO_PATH = "photopath";
        // Notes saved as String, SQL data type: TEXT
        public final static String COLUMN_ITEM_NOTES = "notes";
        // Target quantity of items to achieve full stock level, SQL data type: INTEGER
        public final static String COLUMN_ITEM_TARGET_QUANTITY = "targetquantity";
        // Possible values for spirit type
        public static final int SPIRIT_TYPE_UNKNOWN = 0;
        public static final int SPIRIT_TYPE_BEER = 1;
        public static final int SPIRIT_TYPE_WINE = 2;
        public static final int SPIRIT_TYPE_CHAMPAGNE = 3;
        public static final int SPIRIT_TYPE_GIN = 4;
        public static final int SPIRIT_TYPE_RUM = 5;
        public static final int SPIRIT_TYPE_TEQUILA = 6;
        public static final int SPIRIT_TYPE_WHISKEY = 7;
        public static final int SPIRIT_TYPE_VERMOUTH = 8;
        public static final int SPIRIT_TYPE_BRANDY = 9;
        public static final int SPIRIT_TYPE_COGNAC = 10;
        public static final int SPIRIT_TYPE_LIQEUR = 11;
        // Possible values for size type
        public static final int SIZE_TYPE_ML = 0;
        public static final int SIZE_TYPE_OZ = 1;
        public static final int SIZE_TYPE_PACK = 2;

        // Helper method to determine if spirit type integer value is valid or not using constants declared here
        public static Boolean isValidSpiritType(Integer spiritTypeToCheck) {
            if (spiritTypeToCheck == SPIRIT_TYPE_UNKNOWN || spiritTypeToCheck == SPIRIT_TYPE_BEER
                    || spiritTypeToCheck == SPIRIT_TYPE_WINE || spiritTypeToCheck == SPIRIT_TYPE_CHAMPAGNE
                    || spiritTypeToCheck == SPIRIT_TYPE_GIN || spiritTypeToCheck == SPIRIT_TYPE_RUM
                    || spiritTypeToCheck == SPIRIT_TYPE_TEQUILA || spiritTypeToCheck == SPIRIT_TYPE_WHISKEY
                    || spiritTypeToCheck == SPIRIT_TYPE_VERMOUTH || spiritTypeToCheck == SPIRIT_TYPE_BRANDY
                    || spiritTypeToCheck == SPIRIT_TYPE_COGNAC || spiritTypeToCheck == SPIRIT_TYPE_LIQEUR) {
                return true;
            } else {
                return false;
            }
        }

        // Helper method to determine if size type integer value is valid or not using constants declared here
        public static Boolean isValidSizeType(Integer sizeTypeToCheck) {
            if (sizeTypeToCheck == SIZE_TYPE_PACK || sizeTypeToCheck == SIZE_TYPE_ML
                    || sizeTypeToCheck == SIZE_TYPE_OZ) {
                return true;
            } else {
                return false;
            }
        }
    }
}
