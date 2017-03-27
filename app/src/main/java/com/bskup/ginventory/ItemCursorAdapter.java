package com.bskup.ginventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bskup.ginventory.data.ItemContract.ItemEntry;

import java.io.File;
import java.util.Locale;


public class ItemCursorAdapter extends CursorAdapter {

    // Tag for log messages
    private static final String LOG_TAG = ItemCursorAdapter.class.getName();

    // Constructor passing in context and cursor to get data from
    public ItemCursorAdapter(Context context, Cursor c) {
        // Call the recommended constructor passing in 0 for flags
        super(context, c, 0);
    }

    // Creates new blank list view item. No data is bound to the views yet
    // Passes in context, cursor to get data from (already moved to correct position),
    // and parent to which the new view is attached
    // Returns a View, the newly created list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.e(LOG_TAG, "newView called");
        // Return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // Binds the item data in current row pointed to by cursor to the given list item layout
    // View - the View returned by newView, context - app context, cursor - cursor to get data from
    // (Cursor is already moved to the correct row)
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        Log.e(LOG_TAG, "bindView called, position:" + cursor.getPosition());

        // Find the views we need to work with in our inflated layout template
        TextView textViewName = (TextView) view.findViewById(R.id.text_view_name);
        TextView textViewSummary = (TextView) view.findViewById(R.id.text_view_summary);
        TextView textViewBigFontQuantity = (TextView) view.findViewById(R.id.text_view_big_font_quantity);
        LinearLayout linearLayoutViewImageContainer = (LinearLayout) view.findViewById(R.id.linear_layout_view_image_container);
        RelativeLayout relativeLayoutWholeBottomRow = (RelativeLayout) view.findViewById(R.id.relative_layout_whole_bottom_row);
        TextView textViewPriceBubble = (TextView) view.findViewById(R.id.text_view_price_bubble);
        LinearLayout linearLayoutOrderMoreContainer = (LinearLayout) view.findViewById(R.id.linear_layout_order_more_container);


        // Extract properties from Cursor that we need to work with
        //Extract ID
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(ItemEntry._ID));
        // Extract name
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_NAME));
        // Extract quantity
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY
        ));
        // Extract size
        String size = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SIZE
        ));
        // Extract size type
        int sizeType = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SIZE_TYPE
        ));
        String sizeTypeString;
        // Translate size type from int as its saved in db, to String to display in the ui
        switch (sizeType) {
            case ItemEntry.SIZE_TYPE_PACK:
                sizeTypeString = context.getString(R.string.size_type_pack);
                break;
            case ItemEntry.SIZE_TYPE_OZ:
                sizeTypeString = context.getString(R.string.size_type_oz);
                break;
            default:
                sizeTypeString = context.getString(R.string.size_type_ml);
        }
        // Extract origin
        String origin = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_ORIGIN
        ));
        // Extract ABV%
        String abv = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_ABV
        ));
        // Extract purchase price
        String purchasePrice = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE
        ));
        // Extract sale price
        long salePriceAsCents = cursor.getLong(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SALE_PRICE
        ));
        Log.e(LOG_TAG, "salePriceAsCents in itemCursorAdapter: " + salePriceAsCents);
        // Divide by 100 to display as decimal
        double salePriceAsDouble = salePriceAsCents / 100.0;
        Log.e(LOG_TAG, "salePriceAsDouble in itemCursorAdapter: " + salePriceAsDouble);
        String salePriceString = "$" + String.format(Locale.US, "%.2f", salePriceAsDouble);
        // Extract spirit type
        int spiritType = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE
        ));
        String spiritTypeString;
        // Translate size type from int as its saved in db, to String to display in the ui
        switch (spiritType) {
            case ItemEntry.SPIRIT_TYPE_BEER:
                spiritTypeString = context.getString(R.string.spirit_type_beer);
                break;
            case ItemEntry.SPIRIT_TYPE_BRANDY:
                spiritTypeString = context.getString(R.string.spirit_type_brandy);
                break;
            case ItemEntry.SPIRIT_TYPE_CHAMPAGNE:
                spiritTypeString = context.getString(R.string.spirit_type_champagne);
                break;
            case ItemEntry.SPIRIT_TYPE_COGNAC:
                spiritTypeString = context.getString(R.string.spirit_type_cognac);
                break;
            case ItemEntry.SPIRIT_TYPE_GIN:
                spiritTypeString = context.getString(R.string.spirit_type_gin);
                break;
            case ItemEntry.SPIRIT_TYPE_LIQEUR:
                spiritTypeString = context.getString(R.string.spirit_type_liqeur);
                break;
            case ItemEntry.SPIRIT_TYPE_RUM:
                spiritTypeString = context.getString(R.string.spirit_type_rum);
                break;
            case ItemEntry.SPIRIT_TYPE_TEQUILA:
                spiritTypeString = context.getString(R.string.spirit_type_tequila);
                break;
            case ItemEntry.SPIRIT_TYPE_VERMOUTH:
                spiritTypeString = context.getString(R.string.spirit_type_vermouth);
                break;
            case ItemEntry.SPIRIT_TYPE_WHISKEY:
                spiritTypeString = context.getString(R.string.spirit_type_whiskey);
                break;
            case ItemEntry.SPIRIT_TYPE_WINE:
                spiritTypeString = context.getString(R.string.spirit_type_wine);
                break;
            default:
                spiritTypeString = context.getString(R.string.spirit_type_unknown);
        }
        // Extract photo path
        final String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PHOTO_PATH));
        // Set click listener on view image container layout
        linearLayoutViewImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do this when "View image" container view is clicked
                // If photo path string is not null, there is a photo, create intent and view it
                Log.v(LOG_TAG, "photoPath onClick: " + photoPath);
                if (photoPath != null) {
                    File photoFile = new File(photoPath);
                    Uri photoUri = FileProvider.getUriForFile(v.getContext(), "com.bskup.ginventory.fileprovider", photoFile);
                    Log.v(LOG_TAG, "photoUri : " + photoUri);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(photoUri, "image/*");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (intent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(intent);
                    }
                } else {
                    // Photo path is null, no photo exists
                    Toast.makeText(v.getContext(), R.string.no_photo_available, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Extract supplier name and phone number
        final String supplierName = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME));
        final long supplierPhoneNumber = cursor.getLong(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER));
        // Set click listener on "Order more" container layout that opens phone app to call supplier name
        linearLayoutOrderMoreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Do this when "Order more" is clicked
                // Create and show confirmation using supplier name before opening phone app
                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the positive and negative buttons on the dialog
                AlertDialog.Builder dialConfirmationBuilder = new AlertDialog.Builder(v.getContext());
                dialConfirmationBuilder.setMessage("Are you sure you want to dial " + supplierName + " for more " + name + "?");
                dialConfirmationBuilder.setPositiveButton("Yes, dial", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked "Yes, dial" button, dial supplier phone number
                        // Create intent to open phone app using supplier phone number
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhoneNumber));
                        v.getContext().startActivity(dialIntent);
                    }
                });
                dialConfirmationBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog dialConfirmationDialog = dialConfirmationBuilder.create();
                dialConfirmationDialog.show();
            }
        });

        // Set blank click listener on whole bottom row so it does nothing when clicked
        // unless clicked on one of the buttons, helps avoid misclicks
        relativeLayoutWholeBottomRow.setOnClickListener(null);

        // Summary String containing info to be displayed in list under name of item
        String summary = size + sizeTypeString + " " + spiritTypeString + " - " + origin;

        // Populate text fields with extracted properties
        textViewName.setText(name);
        textViewSummary.setText(summary);
        textViewBigFontQuantity.setText(quantity);
        textViewPriceBubble.setText(salePriceString);

        // Change quantity text color based on amount in stock
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String warningThreshold = sharedPrefs.getString(context.getString(R.string.settings_warning_threshold_key), context.getString(R.string.settings_warning_threshold_default));
        if ((Integer.parseInt(quantity) > 0) && (Integer.parseInt(quantity) < Integer.parseInt(warningThreshold))) {
            textViewBigFontQuantity.setTextColor(ContextCompat.getColor(context, R.color.quantityLevelWarning));
        } else if (Integer.parseInt(quantity) == 0) {
            textViewBigFontQuantity.setTextColor(ContextCompat.getColor(context, R.color.quantityLevelOutOfStock));
        } else {
            textViewBigFontQuantity.setTextColor(ContextCompat.getColor(context, R.color.quantityLevelGood));
        }
    }

    // Override getView so we have access to the position in list that was clicked on
    // TODO put this stuff back in bindView using _id instead of position in list+1....wtf am i doing
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.e(LOG_TAG, "getView called, position " + position);
        final View view = super.getView(position, convertView, parent);
        ImageView downArrow = (ImageView) view.findViewById(R.id.image_view_theme_dependent_arrow_down_icon);
        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do this when down arrow clicked
                Log.e(LOG_TAG, "Down arrow clicked at position " + position);
                // Query db for quantity at position and if it is at least 1, decrease it
                String[] projection = { ItemEntry.COLUMN_ITEM_QUANTITY };
                String selection = ItemEntry._ID + "=?";
                String[] selectionArgs = new String[]{ String.valueOf(position + 1) };
                Cursor cursor = view.getContext().getContentResolver().query(ItemEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                // Cursor should only contain 1 row, move to first row
                cursor.moveToFirst();
                int quantity = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY)));
                if (quantity >= 1) {
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity - 1);
                    // Call update method through contentResolver passing in new values
                    int rowsUpdated = v.getContext().getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);
                    // If update successful, show a toast or something
                    if (rowsUpdated == 1) {
                        // Notify listener that data has changed
                        Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, position + 1);
                        v.getContext().getContentResolver().notifyChange(uri, null);
                        Toast.makeText(v.getContext(), "Quantity decreased", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update unsuccessful
                        Toast.makeText(v.getContext(), "Update unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                }
                cursor.close();
            }
        });
        ImageView upArrow = (ImageView) view.findViewById(R.id.image_view_theme_dependent_arrow_up_icon);
        upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do this when up arrow clicked
                Log.e(LOG_TAG, "Up arrow clicked at position " + position);
                // Query db for quantity at position and if it is under max value, increase it
                String[] projection = { ItemEntry.COLUMN_ITEM_QUANTITY };
                String selection = ItemEntry._ID + "=?";
                String[] selectionArgs = new String[]{ String.valueOf(position + 1) };
                Cursor cursor = view.getContext().getContentResolver().query(ItemEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                // Cursor should only contain 1 row, move to first row
                cursor.moveToFirst();
                int quantity = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY)));
                if (quantity <= (Integer.MAX_VALUE - 1)) {
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity + 1);
                    // Call update method through contentResolver passing in new values
                    int rowsUpdated = v.getContext().getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);
                    // If update successful, show a toast or something
                    if (rowsUpdated == 1) {
                        // Notify listener that data has changed
                        Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, position + 1);
                        v.getContext().getContentResolver().notifyChange(uri, null);
                        Toast.makeText(v.getContext(), "Quantity increased", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update unsuccessful
                        Toast.makeText(v.getContext(), "Update unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                }
                cursor.close();
            }
        });
        return view;
    }

}
