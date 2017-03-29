package com.bskup.ginventory;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bskup.ginventory.data.ItemContract.ItemEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Tag for log messages
    private static String LOG_TAG = EditorActivity.class.getSimpleName();

    // EditText fields for inventory item attributes
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mSizeEditText;
    private EditText mOriginEditText;
    private EditText mAbvEditText;
    private EditText mPurchasePriceEditText;
    private EditText mSalePriceEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    // Spinner for spirit type
    private Spinner mSpiritTypeSpinner;
    // Spinner for size type
    private Spinner mSizeTypeSpinner;

    // Initial value for spirit type spinner, values are in ItemContract
    private int mSpiritType = ItemEntry.SPIRIT_TYPE_UNKNOWN;
    // Initial value for size type spinner, values are in ItemContract
    private int mSizeType = ItemEntry.SIZE_TYPE_ML;

    // View that will hold the image we get back from camera
    private ImageView mPictureImageView;
    // "Add a photo text and icon" linear layout
    private LinearLayout mAddAPhotoLinearLayout;
    // View photo full size and delete photo container relative layout
    private RelativeLayout mViewPhotoFullSizeAndDeletePhotoRelativeLayout;
    // View photo full size image view
    private ImageView mViewPhotoFullSizeImageView;
    // Delete photo image view
    private ImageView mDeletePhotoImageView;
    // Editor down arrow container
    private LinearLayout mLinearLayoutEditorDownArrowContainer;
    // Editor up arrow container
    private LinearLayout mLinearLayoutEditorUpArrowContainer;

    // Loader ID for loader to use when loading an existing item from InventoryItems db
    private static final int EXISTING_ITEM_LOADER = 0;
    // Request code for passing into startActivityResult when launching Camera
    static final int REQUEST_IMAGE_CAPTURE = 1;
    // Permission request response constant
    public static final int PERMISSION_REQUEST_CAMERA_AND_EXTERNAL_STORAGE = 100;

    // Boolean for tracking whether any fields have been touched, so we can know whether
    // or not to show "Discard changes?" dialog before closing out of the editor activity
    // Initially false until something is touched
    private boolean mItemHasChanged = false;

    // Uri to hold current item Uri, will be null when entering a new item because
    // we didn't open the editor activity with a Uri
    private Uri mCurrentItemUri;

    // OnTouchListener that will detect fields being touched so we know whether or not
    // to display "Discard changes?" dialog when closing out of the activity
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Switch ItemHasChanged variable to true
            mItemHasChanged = true;
            return false;
        }
    };

    // OnClickListener that will open intent with current image uri
    private View.OnClickListener mOpenFullSizePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do this when "view full size" ImageView is clicked
            // If photo path string is not null, there is a photo, create intent and view it
            Log.v(LOG_TAG, "photoPath onClick in EditorActivity view full size: " + mCurrentPhotoPath);
            if (mCurrentPhotoPath != null) {
                File photoFile = new File(mCurrentPhotoPath);
                Uri photoUri = FileProvider.getUriForFile(v.getContext(), v.getContext().getString(R.string.file_provider_path), photoFile);
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
    };
    // OnClickListener that will delete current image
    private View.OnClickListener mDeletePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            // Do this when "delete photo" image view is clicked
            // If photo path string is not null, there is a photo, delete it
            Log.v(LOG_TAG, "photoPath onClick in EditorActivity delete photo: " + mCurrentPhotoPath);
            if (mCurrentPhotoPath != null) {
                // Confirmation before delete
                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the positive and negative buttons on the dialog
                AlertDialog.Builder deletePhotoBuilder = new AlertDialog.Builder(v.getContext());
                deletePhotoBuilder.setMessage(R.string.delete_photo_message);
                deletePhotoBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Delete" button, delete the photo
                        // TODO try passing in this stuff instead of creating it
                        File photoFile = new File(mCurrentPhotoPath);
                        Uri photoUri = FileProvider.getUriForFile(v.getContext(), v.getContext().getString(R.string.file_provider_path), photoFile);
                        Log.v(LOG_TAG, "photoUri : " + photoUri);
                        // Delete photo
                        long filesDeleted = getContentResolver().delete(photoUri, null, null);
                        if (filesDeleted == 1) {
                            mPictureImageView.setImageBitmap(null);
                            mAddAPhotoLinearLayout.setVisibility(View.VISIBLE);
                            mViewPhotoFullSizeAndDeletePhotoRelativeLayout.setVisibility(View.GONE);
                            mCurrentPhotoPath = null;
                            mCurrentPhotoUri = null;
                            mCurrentPhotoFile = null;
                            // Create a ContentValues object where column names are the keys,
                            // and item attributes from the editor are the values.
                            ContentValues values = new ContentValues();
                            values.put(ItemEntry.COLUMN_ITEM_PHOTO_PATH, mCurrentPhotoPath);
                            // Define selection criteria for rows to update
                            String selection = ItemEntry._ID + "=?";
                            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentItemUri))};
                            // Update item data using ContentResolver, returns # of rows updated
                            long rowsUpdated = getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);
                            // Show a toast message depending on whether or not the update was successful
                            if (rowsUpdated == 0) {
                                // If no rows updated, there was an error with update
                                Toast.makeText(v.getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the update was successful and we can display a success toast
                                Toast.makeText(v.getContext(), "Photo deleted, item updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                deletePhotoBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the item
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog deletePhotoAlertDialog = deletePhotoBuilder.create();
                deletePhotoAlertDialog.show();
            } else {
                // Photo path is null, no photo exists
                Toast.makeText(v.getContext(), R.string.no_photo_available, Toast.LENGTH_SHORT).show();
            }
        }
    };

    // File path to current photo for use with ACTION_VIEW intents
    private String mCurrentPhotoPath;
    // File object to hold full size photo data
    private File mCurrentPhotoFile;
    // Uri of current File object
    private Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Theme change based on preference
        String themeName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "default");
        Log.v(LOG_TAG, "value for theme String in EditorActivity oncreate: " + themeName);
        if (themeName.equals("AppThemeDark")) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        setContentView(R.layout.activity_editor);

        // Use getIntent and getData to get the associated Uri that opened this activity
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            // If opened using FAB, uri will be null, change title to "Add an item"
            // No need to create a loader, nothing to load
            setTitle(getString(R.string.editor_title_new_item));
            // Invalidate options menu so "Delete" can be hidden
            // (Can't delete a blank item that hasn't been saved yet)
            // After calling this, onPrepareOptionsMenu is called
            invalidateOptionsMenu();
        } else {
            // If opened using ListView item, uri will be present, change title to "Edit Item"
            // and load item data using loader
            setTitle(getString(R.string.editor_title_edit_item));
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find views and layouts that we will need to work with
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mSizeEditText = (EditText) findViewById(R.id.edit_item_size);
        mOriginEditText = (EditText) findViewById(R.id.edit_item_origin);
        mAbvEditText = (EditText) findViewById(R.id.edit_item_abv);
        mPurchasePriceEditText = (EditText) findViewById(R.id.edit_item_purchase_price);
        mSalePriceEditText = (EditText) findViewById(R.id.edit_item_sale_price);
        mSpiritTypeSpinner = (Spinner) findViewById(R.id.spinner_spirit_type);
        mSizeTypeSpinner = (Spinner) findViewById(R.id.spinner_size_type);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_item_supplier_name);
        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_item_supplier_phone_number);
        mLinearLayoutEditorDownArrowContainer = (LinearLayout) findViewById(R.id.linear_layout_editor_down_arrow_container);
        mLinearLayoutEditorUpArrowContainer = (LinearLayout) findViewById(R.id.linear_layout_editor_up_arrow_container);

        // Set touch listener on all fields to know whether or not to show "discard changes?" dialog
        mNameEditText.setOnTouchListener(mOnTouchListener);
        mQuantityEditText.setOnTouchListener(mOnTouchListener);
        mSizeEditText.setOnTouchListener(mOnTouchListener);
        mOriginEditText.setOnTouchListener(mOnTouchListener);
        mAbvEditText.setOnTouchListener(mOnTouchListener);
        mPurchasePriceEditText.setOnTouchListener(mOnTouchListener);
        mSalePriceEditText.setOnTouchListener(mOnTouchListener);
        mSpiritTypeSpinner.setOnTouchListener(mOnTouchListener);
        mSizeTypeSpinner.setOnTouchListener(mOnTouchListener);
        mSupplierNameEditText.setOnTouchListener(mOnTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mOnTouchListener);
        mLinearLayoutEditorDownArrowContainer.setOnTouchListener(mOnTouchListener);
        mLinearLayoutEditorUpArrowContainer.setOnTouchListener(mOnTouchListener);

        // Call helper method to set up our spinners
        setupSpinners();

        // Find the "add a photo" container layout so we can set a click listener on it
        RelativeLayout pictureContainerRelativeLayout = (RelativeLayout) findViewById(R.id.picture_container_relative_layout);
        // Find the Picture image view and set it to a global variable so we can change
        // the image it contains from other methods
        mPictureImageView = (ImageView) findViewById(R.id.picture_image_view);
        // Find the "add a photo icon and text" linear layout so we can show and hide it
        mAddAPhotoLinearLayout = (LinearLayout) findViewById(R.id.add_a_photo_linear_layout);
        // Create new click listener and set it on the "Add a photo" container layout
        pictureContainerRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do this when "Add a Photo" is clicked
                launchCameraIntentForResult();
            }
        });

        // Find the view full size and delete photo container layout so we can show/hide it
        mViewPhotoFullSizeAndDeletePhotoRelativeLayout = (RelativeLayout) findViewById(R.id.view_and_delete_photo_container_relative_layout);
        // Find the individual image views inside the above container
        mViewPhotoFullSizeImageView = (ImageView) findViewById(R.id.view_full_size_photo_image_view);
        mDeletePhotoImageView = (ImageView) findViewById(R.id.delete_photo_image_view);

        // If current picture is null, hide the view full size and delete photo layout
        if (mCurrentPhotoPath == null) {
            mViewPhotoFullSizeAndDeletePhotoRelativeLayout.setVisibility(View.GONE);
        } else {
            mViewPhotoFullSizeAndDeletePhotoRelativeLayout.setVisibility(View.VISIBLE);
            // If photo visible, set click listeners on view full size and delete image views
            mViewPhotoFullSizeImageView.setOnClickListener(mOpenFullSizePhotoClickListener);
            mDeletePhotoImageView.setOnClickListener(mDeletePhotoClickListener);
        }

        // Set click listener on down arrow
        mLinearLayoutEditorDownArrowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do this when down arrow clicked
                if (!mQuantityEditText.getText().toString().isEmpty()) {
                    int currentQuantity = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                    // If quantity is at least 1 we can reduce it by 1
                    if (currentQuantity >= 1) {
                        int newQuantity = currentQuantity - 1;
                        mQuantityEditText.setText(String.valueOf(newQuantity));
                    }
                }
            }
        });
        // Set click listener on up arrow
        mLinearLayoutEditorUpArrowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do this when up arrow clicked
                if (!mQuantityEditText.getText().toString().isEmpty()) {
                    int currentQuantity = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                    // If current quantity is at least 1 below max value, increase it by 1
                    if (currentQuantity <= (Integer.MAX_VALUE - 1)) {
                        int newQuantity = currentQuantity + 1;
                        mQuantityEditText.setText(String.valueOf(newQuantity));
                    }
                }
            }
        });
    }

    // Override back button press to show dialog if pressed after item has been touched
    @Override
    public void onBackPressed() {
        // If item hasn't changed, do normal back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise item has changed
        // Create discardButtonClickListener to pass into showUnsavedChangesDialog
        // that tells dialog what to do when positive button "discard" is pressed
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard", close the current activity
                finish();
            }
        };
        // Show dialog about unsaved changes
        showDiscardChangesDialog(discardButtonClickListener);
    }

    // Override loader creation method to pass in the row we want via mCurrentItemUri
    // and the projection of columns we want
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
                ItemEntry.COLUMN_ITEM_PHOTO_PATH};

        // Return new loader with Uri of single item clicked on to open EditorActivity
        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    // Override method called when loader finishes loading data in the form of a Cursor
    // Extract values from Cursor and populate our EditText fields with extracted values
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Do not proceed if the cursor is null or has 0 rows
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Update editor fields with data for current item
        // Move cursor to 0 before reading from it
        if (data.moveToFirst()) {
            // Find the column index of each item attribute we want
            int nameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int sizeColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SIZE);
            int sizeTypeColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SIZE_TYPE);
            int originColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_ORIGIN);
            int abvColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_ABV);
            int purchasePriceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE);
            int salePriceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SALE_PRICE);
            int spiritTypeColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE);
            int supplierNameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);
            int photoPathColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PHOTO_PATH);

            // Extract values from cursor using each column index, convert where necessary
            String name = data.getString(nameColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int size = data.getInt(sizeColumnIndex);
            int sizeType = data.getInt(sizeTypeColumnIndex);
            String origin = data.getString(originColumnIndex);
            int abv = data.getInt(abvColumnIndex);
            long purchasePriceAsCents = data.getLong(purchasePriceColumnIndex);
            double purchasePriceAsDouble = purchasePriceAsCents / 100.0;
            String purchasePriceString = String.format(Locale.US, "%.2f", purchasePriceAsDouble);
            long salePriceAsCents = data.getLong(salePriceColumnIndex);
            // ^should be 1299
            double salePriceAsDouble = salePriceAsCents / 100.0;
            Log.e(LOG_TAG, "salePriceAsDouble in onLoadFinished: " + salePriceAsDouble);
            // ^should be 12.99
            String salePriceString = String.format(Locale.US, "%.2f", salePriceAsDouble);
            Log.e(LOG_TAG, "salePriceString in onLoadFinished: " + salePriceString);
            int spiritType = data.getInt(spiritTypeColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            long supplierPhoneNumber = data.getLong(supplierPhoneNumberColumnIndex);
            String photoPath = data.getString(photoPathColumnIndex);

            // Update EditText fields with values extracted from cursor
            mNameEditText.setText(name);
            mQuantityEditText.setText(String.valueOf(quantity));
            mSizeEditText.setText(String.valueOf(size));
            mOriginEditText.setText(origin);
            mAbvEditText.setText(String.valueOf(abv));
            mPurchasePriceEditText.setText(purchasePriceString);
            mSalePriceEditText.setText(salePriceString);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(String.valueOf(supplierPhoneNumber));

            // If photo path is not null, create scaled bitmap from it
            if (photoPath != null) {
                mCurrentPhotoPath = photoPath;
                Bitmap testBm = EditorActivity.createCustomScaledBitmap(getApplicationContext(), 100, photoPath);
                // Hide the "add a photo" text
                mAddAPhotoLinearLayout.setVisibility(View.GONE);
                // Populate image field with new bitmap
                mPictureImageView.setImageBitmap(testBm);
                // Show view full size and delete photo layout
                mViewPhotoFullSizeAndDeletePhotoRelativeLayout.setVisibility(View.VISIBLE);
                // Set click listener on view full size image view
                mViewPhotoFullSizeImageView.setOnClickListener(mOpenFullSizePhotoClickListener);
                // Set click listener on delete photo image view
                mDeletePhotoImageView.setOnClickListener(mDeletePhotoClickListener);
            }

            // Update spirit type dropdown spinner
            // SetSelection passes in value corresponding to the position in the array
            // that populates the spinner, R.array.array_spirit_type_options in this case
            switch (spiritType) {
                case ItemEntry.SPIRIT_TYPE_BEER:
                    mSpiritTypeSpinner.setSelection(1);
                    break;
                case ItemEntry.SPIRIT_TYPE_BRANDY:
                    mSpiritTypeSpinner.setSelection(2);
                    break;
                case ItemEntry.SPIRIT_TYPE_CHAMPAGNE:
                    mSpiritTypeSpinner.setSelection(3);
                    break;
                case ItemEntry.SPIRIT_TYPE_COGNAC:
                    mSpiritTypeSpinner.setSelection(4);
                    break;
                case ItemEntry.SPIRIT_TYPE_GIN:
                    mSpiritTypeSpinner.setSelection(5);
                    break;
                case ItemEntry.SPIRIT_TYPE_LIQEUR:
                    mSpiritTypeSpinner.setSelection(6);
                    break;
                case ItemEntry.SPIRIT_TYPE_RUM:
                    mSpiritTypeSpinner.setSelection(7);
                    break;
                case ItemEntry.SPIRIT_TYPE_TEQUILA:
                    mSpiritTypeSpinner.setSelection(8);
                    break;
                case ItemEntry.SPIRIT_TYPE_VERMOUTH:
                    mSpiritTypeSpinner.setSelection(9);
                    break;
                case ItemEntry.SPIRIT_TYPE_WHISKEY:
                    mSpiritTypeSpinner.setSelection(10);
                    break;
                case ItemEntry.SPIRIT_TYPE_WINE:
                    mSpiritTypeSpinner.setSelection(11);
                    break;
                default:
                    mSpiritTypeSpinner.setSelection(0);
                    break;
            }
            // Update size type dropdown spinner
            // SetSelection passes in value corresponding to the position in the array
            // that populates the spinner, R.array.array_size_type_options in this case
            switch (sizeType) {
                case ItemEntry.SIZE_TYPE_OZ:
                    mSizeTypeSpinner.setSelection(1);
                    break;
                case ItemEntry.SIZE_TYPE_PACK:
                    mSizeTypeSpinner.setSelection(2);
                    break;
                default:
                    mSizeTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    // Override method that is called when loader is reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear all editor fields
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mSizeEditText.setText("");
        mOriginEditText.setText("");
        mAbvEditText.setText("");
        mPurchasePriceEditText.setText("");
        mSalePriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
        mSpiritTypeSpinner.setSelection(0);
        mSizeTypeSpinner.setSelection(0);
        mCurrentPhotoPath = null;
        mCurrentPhotoUri = null;
        mCurrentPhotoFile = null;
    }

    // Override method that takes action based on result of asking user for permissions
    // Added @NonNull annotations to get rid of error about non annotated parameter overriding
    // @NonNull parameter
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA_AND_EXTERNAL_STORAGE) {
            // If over sdk 18 only check for camera permission
            if (android.os.Build.VERSION.SDK_INT > 18 && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    || (android.os.Build.VERSION.SDK_INT <= 18 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))) {
                // Permissions all granted, launch camera intent
                launchCameraIntentForResult();
            }
        }
    }

    // Override method that handles receiving result from activity launched using camera intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the request code returned to this activity matches the one we defined for our camera
        // intent and the result is OK, create a scaled down bitmap and display it in ui
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Hide the "add a photo" text
            mAddAPhotoLinearLayout.setVisibility(View.GONE);
            // Log the current photo path
            Log.v(LOG_TAG, "Current photo path: " + mCurrentPhotoPath);
            // Show a toast that photo was saved
            Toast.makeText(EditorActivity.this, R.string.photo_saved, Toast.LENGTH_SHORT).show();

            // Get Uri from current photo path and store in global variable so we can pass it
            // to the saveItem() method and store it in the database
            mCurrentPhotoUri = Uri.fromFile(mCurrentPhotoFile);
            mCurrentPhotoPath = mCurrentPhotoUri.getPath();
            Log.v(LOG_TAG, "Current photo Uri: " + mCurrentPhotoUri);

            // Display scaled down bitmap in 100dp square in ui
            mPictureImageView.setImageBitmap(createCustomScaledBitmap(getApplicationContext(), 100, mCurrentPhotoPath));

            // We got a picture back, show View full size and delete photo layout
            mViewPhotoFullSizeAndDeletePhotoRelativeLayout.setVisibility(View.VISIBLE);
            mViewPhotoFullSizeImageView.setOnClickListener(mOpenFullSizePhotoClickListener);
            mDeletePhotoImageView.setOnClickListener(mDeletePhotoClickListener);
        }
    }

    // Helper method to create scaled Bitmap to display in the UI
    public static Bitmap createCustomScaledBitmap(Context context, int destinationImageWidthInDp, String photoPath) {
        // Create Bitmap to scale based on full size photo at mCurrentPhotoPath
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        // Get screen density to help scale to 100dp on any users screen
        float screenDensity = context.getResources().getDisplayMetrics().density;
        float imageViewWidthInDpAsFloat = (float) destinationImageWidthInDp;
        // If landscape image, calculate ratio based on its height so the new height matches
        // our ImageView and the width extends off the sides of our square ImageView
        float scaleRatio;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            scaleRatio = imageViewWidthInDpAsFloat / bitmap.getHeight();
        } else {
            scaleRatio = imageViewWidthInDpAsFloat / bitmap.getWidth();
        }
        Log.e(LOG_TAG, "screenDensity: " + screenDensity);
        Log.e(LOG_TAG, "bitmap.getWidth: " + bitmap.getWidth());
        Log.e(LOG_TAG, "scaleRatio: " + scaleRatio);
        int newWidth = Math.round(screenDensity * (bitmap.getWidth() * scaleRatio));
        int newHeight = Math.round(screenDensity * (bitmap.getHeight() * scaleRatio));
        // Log new width and height
        Log.e(LOG_TAG, "newWidth: " + newWidth);
        Log.e(LOG_TAG, "newHeight: " + newHeight);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // Helper method to launch camera intent using startActivityForResult after checking
    // to see if there is an app to open the intent
    private void launchCameraIntentForResult() {
        // Check for camera permission before attempting to open camera
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (android.os.Build.VERSION.SDK_INT <= 18 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))) {
            // Camera or write external storage permission not granted, ask for permission and
            // receive result in onRequestPermissionResult
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CAMERA_AND_EXTERNAL_STORAGE);
            return;
        }

        // Create new intent to open an app that can handle image capture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // If there is an app that can handle it, start activity for result, passing
        // in the constant we created that corresponds to an integer code
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            mCurrentPhotoFile = null;
            try {
                mCurrentPhotoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(LOG_TAG, "Error creating File");
            }
            // Continue only if the File was successfully created
            if (mCurrentPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.file_provider_path),
                        mCurrentPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Helper method to create a File with collision free name to store full size image in
    // Saves in /Android/data/com.bskup.ginventory/files/Pictures
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Helper method to set up spinners
    private void setupSpinners() {
        // Create adapter for each spinner
        ArrayAdapter spiritTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_spirit_type_options, android.R.layout.simple_spinner_item);
        ArrayAdapter sizeTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_size_type_options, android.R.layout.simple_spinner_item);

        // Specify spinner dropdown layout style
        spiritTypeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sizeTypeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Apply the adapters to the spinners
        mSpiritTypeSpinner.setAdapter(spiritTypeSpinnerAdapter);
        mSizeTypeSpinner.setAdapter(sizeTypeSpinnerAdapter);

        // Read value user selects from Spirit Type spinner and change global variable mSpiritType
        // to corresponding integer defined in ItemContract (used when saving an item)
        mSpiritTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String spiritTypeSelection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(spiritTypeSelection)) {
                    if (spiritTypeSelection.equals(getString(R.string.spirit_type_beer))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_BEER;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_wine))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_WINE;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_champagne))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_CHAMPAGNE;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_gin))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_GIN;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_rum))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_RUM;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_tequila))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_TEQUILA;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_whiskey))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_WHISKEY;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_vermouth))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_VERMOUTH;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_brandy))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_BRANDY;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_cognac))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_COGNAC;
                    } else if (spiritTypeSelection.equals(getString(R.string.spirit_type_liqeur))) {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_LIQEUR;
                    } else {
                        mSpiritType = ItemEntry.SPIRIT_TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSpiritType = ItemEntry.SPIRIT_TYPE_UNKNOWN;
            }
        });
        // Read value user selects from Size Type spinner and change global variable mSizeType
        // to corresponding integer defined in ItemContract (used when saving an item)
        mSizeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sizeTypeSelection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(sizeTypeSelection)) {
                    if (sizeTypeSelection.equals(getString(R.string.size_type_pack))) {
                        mSizeType = ItemEntry.SIZE_TYPE_PACK;
                    } else if (sizeTypeSelection.equals(getString(R.string.size_type_oz))) {
                        mSizeType = ItemEntry.SIZE_TYPE_OZ;
                    } else {
                        mSizeType = ItemEntry.SIZE_TYPE_ML;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSizeType = ItemEntry.SIZE_TYPE_ML;
            }
        });
    }

    // Helper method to create Discard Changes dialog
    private void showDiscardChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create AlertDialog.Builder and set message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes_message);
        // Set positive and negative buttons and corresponding listeners
        builder.setPositiveButton(R.string.discard_changes_positive_button, discardButtonClickListener);
        builder.setNegativeButton(R.string.discard_changes_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "keep editing", dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Helper method to display Delete confirmation dialog
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, delete the item and exit activity
                deleteItem();
                finish();
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

    // Helper method to get user input from editor and save new item into database
    // Returns true if save successful or false if unsuccessful
    private boolean saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String originString = mOriginEditText.getText().toString().trim();
        String sizeString = mSizeEditText.getText().toString().trim();
        String abvString = mAbvEditText.getText().toString().trim();
        String purchasePriceString = mPurchasePriceEditText.getText().toString().trim();
        String salePriceString = mSalePriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        // Before creating ContentValues, check if all fields are empty and spirit type unknown
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(sizeString)
                && TextUtils.isEmpty(originString) && TextUtils.isEmpty(abvString)
                && TextUtils.isEmpty(purchasePriceString) && TextUtils.isEmpty(salePriceString)
                && TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneNumberString)
                && mSpiritType == ItemEntry.SPIRIT_TYPE_UNKNOWN) {
            // If all fields are empty, return true as if we saved successfully so it
            // triggers finish() from onOptionsItemSelected and closes activity
            return true;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();

        // Set error msg if name left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        String name;
        if (TextUtils.isEmpty(nameString)) {
            mNameEditText.setError(getString(R.string.error_required_name));
            // Request focus so error message is automatically displayed
            mNameEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            name = nameString;
        }
        // Set error msg if quantity left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        int quantity;
        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setError(getString(R.string.error_required_quantity));
            // Request focus so error message is automatically displayed
            mQuantityEditText.requestFocus();
            // Return without saving
            return false;
        } else if (Integer.parseInt(quantityString) < 0) {
            // If quantity is somehow negative
            mQuantityEditText.setError(getString(R.string.error_invalid_quantity));
            // Request focus so error message is automatically displayed
            mQuantityEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            quantity = Integer.parseInt(quantityString);
        }
        // Set error msg if origin left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        String origin;
        if (TextUtils.isEmpty(originString)) {
            mOriginEditText.setError(getString(R.string.error_required_origin));
            // Request focus so error message is automatically displayed
            mQuantityEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            origin = originString;
        }
        // Set error msg if size left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        int size;
        if (TextUtils.isEmpty(sizeString)) {
            mSizeEditText.setError(getString(R.string.error_required_size));
            // Request focus so error message is automatically displayed
            mSizeEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            size = Integer.parseInt(sizeString);
        }
        // Set error msg if abv left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        int abv;
        if (TextUtils.isEmpty(abvString)) {
            mAbvEditText.setError(getString(R.string.error_required_abv));
            // Request focus so error message is automatically displayed
            mAbvEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            abv = Integer.parseInt(abvString);
        }
        // Set error msg if purchase price left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        Double purchasePriceAsDouble;
        long purchasePriceAsCents;
        if (TextUtils.isEmpty(purchasePriceString)) {
            mPurchasePriceEditText.setError(getString(R.string.error_required_purchase_price));
            // Request focus so error message is automatically displayed
            mPurchasePriceEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            // Parse purchase price and multiply by 100 to convert to cents to store as whole numbers
            // Parse sale price and multiply by 100 to convert to cents to store as whole numbers
            purchasePriceAsDouble = Double.valueOf(purchasePriceString);
            Log.e(LOG_TAG, "purchasePriceAsDouble in saveItem: " + purchasePriceAsDouble);
            purchasePriceAsCents =  Double.valueOf(purchasePriceAsDouble * 100.0).longValue();
            Log.e(LOG_TAG, "purchasePriceAsCents in saveItem: " + purchasePriceAsCents);
        }
        // Set error msg if sale price left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        Double salePriceAsDouble;
        long salePriceAsCents;
        if (TextUtils.isEmpty(salePriceString)) {
            mSalePriceEditText.setError(getString(R.string.error_required_sale_price));
            // Request focus so error message is automatically displayed
            mSalePriceEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            // Parse sale price and multiply by 100 to convert to cents to store as whole numbers
            salePriceAsDouble = Double.valueOf(salePriceString);
            Log.e(LOG_TAG, "salePriceAsDouble in saveItem: " + salePriceAsDouble);
            salePriceAsCents =  Double.valueOf(salePriceAsDouble * 100.0).longValue();
            Log.e(LOG_TAG, "salePriceAsCents in saveItem: " + salePriceAsCents);
        }
        // Set error msg if supplier name left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        String supplierName;
        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierNameEditText.setError(getString(R.string.error_required_supplier_name));
            // Request focus so error message is automatically displayed
            mSupplierNameEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            supplierName = supplierNameString;
        }
        // Set error msg if supplier phone number left blank by user, otherwise parse it from
        // the String we pull from the EditText field
        long supplierPhoneNumber;
        if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            mSupplierPhoneNumberEditText.setError(getString(R.string.error_required_supplier_phone_number));
            // Request focus so error message is automatically displayed
            mSupplierPhoneNumberEditText.requestFocus();
            // Return without saving
            return false;
        } else {
            // Remove everything except numbers before saving
            // Regex [^0-9] = "not 0-9" so everything but those we replace with "" or nothing
            String supplierPhoneNumberStringOnlyNumbers = supplierPhoneNumberString.replaceAll("[^0-9]", "");
            supplierPhoneNumber = Long.parseLong(supplierPhoneNumberStringOnlyNumbers);
        }

        // Add values to contentValues object
        values.put(ItemEntry.COLUMN_ITEM_NAME, name);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_ORIGIN, origin);
        values.put(ItemEntry.COLUMN_ITEM_SIZE, size);
        values.put(ItemEntry.COLUMN_ITEM_SIZE_TYPE, mSizeType);
        values.put(ItemEntry.COLUMN_ITEM_ABV, abv);
        values.put(ItemEntry.COLUMN_ITEM_PURCHASE_PRICE, purchasePriceAsCents);
        values.put(ItemEntry.COLUMN_ITEM_SALE_PRICE, salePriceAsCents);
        values.put(ItemEntry.COLUMN_ITEM_SPIRIT_TYPE, mSpiritType);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, supplierName);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);
        values.put(ItemEntry.COLUMN_ITEM_PHOTO_PATH, mCurrentPhotoPath);

        // If opened using ListView item, uri will be present, proceed to "Edit Item" (update mode)
        if (mCurrentItemUri != null) {
            // Define selection criteria for rows to update
            String selection = ItemEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentItemUri))};
            // Update row from editor fields data using ContentResolver, returns # of rows updated
            long rowsUpdated = getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);

            // Show a toast message depending on whether or not the update was successful
            if (rowsUpdated == 0) {
                // If no rows updated, there was an error with update
                Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the update was successful and we can display a success toast
                Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            // If opened using FAB, uri will be null, proceed to "Add an item" (insert mode)
            // Insert a new row from editor fields data using ContentResolver, returns uri of new row
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the uri is null, there was an error with insertion
                Toast.makeText(this, R.string.editor_insert_item_error, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a success toast
                Toast.makeText(this, R.string.editor_insert_item_success, Toast.LENGTH_SHORT).show();
            }
        }

        // Reset photo member variables after saving
        mCurrentPhotoPath = null;
        mCurrentPhotoFile = null;
        mCurrentPhotoUri = null;

        // If we made it this far without returning false, must have saved successfully
        return true;
    }

    // Helper method to perform deletion of an item from the database
    private void deleteItem() {
        // If current item Uri is not null, we are in "Edit mode", ok to try deleting item
        if (mCurrentItemUri != null) {
            // Define selection criteria for rows to delete
            String selection = ItemEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentItemUri))};
            // Delete row based on uri using ContentResolver, returns # of rows deleted
            int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, selection, selectionArgs);

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                // If no rows deleted, there was an error with delete
                Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a success toast
                Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide "Delete" menu item
        if (mCurrentItemUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                boolean didItSave = saveItem();
                if (didItSave) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Open delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow aka home button in the app bar
            case android.R.id.home:
                // If item hasn't changed, continue navigating up to parent activity
                if (!mItemHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise item has changed, create discardClickListener and show dialog
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard", navigate to parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show dialog about unsaved changes
                showDiscardChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
