/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.products;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.products.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Allows user to create a new Product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int FILE_SELECT_CODE = 1;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * Product Image field
     */
    private ImageView mImage;

    /**
     * EditText field to enter the products name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the products quantity
     */
    private EditText mQuantityText;

    /**
     * EditText field to enter the products Price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the suppliers email
     */
    private EditText mMailEditText;

    /**
     * Text View that shows the Units Sold
     */

    private TextView mUnitsSold;


    /**
     * Text View thar shows the total sales
     */

    private TextView mSales;

    /**
     * Add image Button
     */

    private Button addPhotoButton;

    /**
     * Add Sell Product Button
     */

    private Button sellButton;

    /**
     * Add Buy Product Button
     */

    private Button buyButton;

    /**
     * Supplier Buy Text
     */

    private TextView supplierText;

    /**
     * Buy supplier Button
     */

    private Button supplierButton;

    /**
     * Supplier Cancel Order Button
     */

    private Button supplierCancel;

    /**
     * Edit Text Supplier Quantity
     */

    private EditText mSupplierQuantity;

    /**
     * Validation to confirm whether the user really wants to exit without changes if
     * the user updates part of the product form
     */

    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity
        // in order to figure out if we're creating a new pet or editing an existing one.
        // Use getIntent() and getData() to get the associated URI

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change the app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // initialize the loader
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input form
        mImage = (ImageView) findViewById(R.id.product_image);
        mNameEditText = (EditText) findViewById(R.id.product_name);
        mQuantityText = (EditText) findViewById(R.id.product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.product_price);
        mMailEditText = (EditText) findViewById(R.id.supplier_email);
        mUnitsSold = (TextView) findViewById(R.id.total_units_sold);
        mSales = (TextView) findViewById(R.id.total_sales);
        addPhotoButton = (Button) findViewById(R.id.add_photo);
        supplierText = (TextView) findViewById(R.id.buy_supplier_text);
        supplierButton = (Button) findViewById(R.id.buy_supplier_button);
        supplierCancel = (Button) findViewById(R.id.cancel_buy_button);
        mSupplierQuantity = (EditText) findViewById(R.id.product_buy_quantity);


        // Check if the fields have been changed
        mImage.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mMailEditText.setOnTouchListener(mTouchListener);
        addPhotoButton.setOnTouchListener(mTouchListener);
        mSupplierQuantity.setOnTouchListener(mTouchListener);

        // Set the image view to be a click listener to insert an Image to
        // the product
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto();
            }
        });


        // Initiate the Sell Button

        sellButton = (Button) findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // initiate the sell button method
                sellButton();
            }

        });

        // Initiate the Buy Button

        buyButton = (Button) findViewById(R.id.buy_button);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supplierText.setVisibility(View.VISIBLE);
                supplierButton.setVisibility(View.VISIBLE);
                supplierCancel.setVisibility(View.VISIBLE);
                mSupplierQuantity.setVisibility(View.VISIBLE);
            }
        });

        // Initiate the Cancel Supplier Button

        supplierCancel = (Button) findViewById(R.id.cancel_buy_button);
        supplierCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If the product hasn't changed, continue with handling back button press
                if (!mProductHasChanged) {
                    supplierText.setVisibility(View.GONE);
                    supplierButton.setVisibility(View.GONE);
                    supplierCancel.setVisibility(View.GONE);
                    mSupplierQuantity.setVisibility(View.GONE);
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should
                // be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, close the current activity.
                                finish();
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
            }
        });


        // Initiate the Buy Supplier Button

        supplierButton = (Button) findViewById(R.id.buy_supplier_button);
        supplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                supplierBuyButton();
                mail();
                finish();
            }
        });

    }

    /**
     * Add Photo method
     */
    private void addPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)),
                FILE_SELECT_CODE);
    }

    /**
     * Get user input from editor and save new product into the database
     */
    private void saveProduct() {

        if (mCurrentProductUri == null) {

            // Read from input fields
            // Use trim to eliminate leading or trailing white space

            String nameString = mNameEditText.getText().toString().trim();
            String quantityString = mQuantityText.getText().toString().trim();
            int quantity = 0;
            String priceString = mPriceEditText.getText().toString().trim();
            float price = 0;
            String mailString = mMailEditText.getText().toString().trim();
            int unitsSoldString = 0;
            float totalSalesString = 0;
            float sales = 0;
            Bitmap imageBitMap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] imageByteArray = outputStream.toByteArray();


            // Checkpoint to guarantee that there are no empty fields.

            if (mCurrentProductUri == null &&
                    TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                    TextUtils.isEmpty(priceString) && TextUtils.isEmpty(mailString)) {
                return;
            }

            // Create a ContentValues object where column names are the keys,
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, mailString);
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);

            // If the quantity is not provided by the user, don't try to parse
            // the string into an integer value. Use 0 by default.

            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

            // If the price is not provided by the user, don't try to parse
            // the string into an integer value. Use 0 by default.

            if (!TextUtils.isEmpty(priceString)) {
                price = Integer.parseInt(priceString);
            }
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);


            // If there is no image loaded send message
            if (mImage.getDrawable() == null) {
                Toast.makeText(this, "Please add a valid image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert a new product into the provider, returning the content UR
            // for the new product.

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion
            // was successful

            if (newUri == null) {
                // If the new content URI is null, then there was an error with
                // insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            // Otherwise this is an EXISTING product, so update the product with content
            // URI: mCurrentProductUri and pass in the new ContentValues. Pass in null
            // for the selection and selection args because mCurrentPetUri will already
            // identify the correct row in the database that we want to modify.

            // Read from input fields
            // Use trim to eliminate leading or trailing white space

            String nameString = mNameEditText.getText().toString().trim();
            String quantityString = mQuantityText.getText().toString().trim();
            String priceString = mPriceEditText.getText().toString().trim();
            String mailString = mMailEditText.getText().toString().trim();
            String unitsSoldString = mUnitsSold.getText().toString().trim();
            String totalSalesString = mSales.getText().toString().trim();
            Bitmap imageBitMap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] imageByteArray = outputStream.toByteArray();

            // Create a ContentValues object where column names are the keys,
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, mailString);
            values.put(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD, unitsSoldString);
            values.put(ProductEntry.COLUMN_PRODUCT_SALES, totalSalesString);
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    mImage.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save Pet to database
                saveProduct();

                // Exit activity
                finish();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all products attributes, define a projection that
        // contains all columns from the products  table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_UNITS_SOLD,
                ProductEntry.COLUMN_PRODUCT_SALES};

        // This loader will execute the ContentProvider's query method on
        // a background thread.
        return new CursorLoader(this, // Parent activity context
                mCurrentProductUri,   // Query the content URI for the current product
                projection,           // Columns to include in the resulting Cursor
                null,                 // No selection clause
                null,                 // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of pet attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int unitsSoldColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD);
            int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

            // Extract out the value from the Cursor for the given column index

            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int unitsSold = cursor.getInt(unitsSoldColumnIndex);
            float sales = cursor.getFloat(salesColumnIndex);
            byte[] imageByteArray = cursor.getBlob(imageColumnIndex);

            // Update the views on the screen with the values from the database

            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mMailEditText.setText(email);
            mQuantityText.setText(Integer.toString(quantity));
            mUnitsSold.setText(Integer.toString(unitsSold));
            mSales.setText(Float.toString(sales));

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            mImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Update the views on the screen with the values from the database
        mImage.setImageBitmap(null);
        mNameEditText.setText("");
        mPriceEditText.setText(Integer.toString(0));
        mMailEditText.setText("");
        mQuantityText.setText(Integer.toString(0));
        mUnitsSold.setText(Integer.toString(0));
        mSales.setText(Integer.toString(0));
    }

    /**
     * Checkpoint to verify if the user updates part of the products form.
     */

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    /**
     * Creating the “Discard changes” dialog
     */

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Hook up the back button
     */

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should
        // be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Shows a Dialog Box to confirm deleting a Product
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the
            // mCurrentProductUri content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Sell Button method
     */

    private void sellButton() {

        // Read from input fields
        float mSellPrice = Float.parseFloat(mPriceEditText.getText().toString());
        float mSellSales = Float.parseFloat(mSales.getText().toString());
        int mSellUnitsSold = Integer.parseInt(mUnitsSold.getText().toString());
        int decreaseQuantity = Integer.parseInt(mQuantityText.getText().toString());
        if (decreaseQuantity == 0) {

            // If the product quantity is 0 then there are no products to sell.
            Toast.makeText(this, getString(R.string.no_more_products),
                    Toast.LENGTH_SHORT).show();

        } else {

            decreaseQuantity--;
            mSellUnitsSold++;
            mSellSales = Math.round(((mSellUnitsSold * mSellPrice) * 100.00) / 100.00);

            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.sell_confirmation),
                    Toast.LENGTH_SHORT).show();
        }

        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, decreaseQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD, mSellUnitsSold);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, mSellSales);

        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
    }

    /**
     * Sell Button method
     */

    private void supplierBuyButton() {

        // Read from input fields

        int currentQuantity = Integer.parseInt(mQuantityText.getText().toString());
        int supplierBuyQuantity = Integer.parseInt(mSupplierQuantity.getText().toString());
        if (supplierBuyQuantity < 0) {

            // If the product quantity negative then the product quantity to buy isn't valid.
            Toast.makeText(this, getString(R.string.invalid_quantity),
                    Toast.LENGTH_SHORT).show();

        } else {

            currentQuantity = currentQuantity + supplierBuyQuantity;

        }

        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);

        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
    }

    // Create an email to the supplier
    private void mail() {

        String mailString = mMailEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        int currentQuantity = Integer.parseInt(mSupplierQuantity.getText().toString());

        //Creates an email message with the order summary
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mail to:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mailString);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Product Order");
        intent.putExtra(Intent.EXTRA_TEXT, "Product name: " + nameString
                + "\n" + "Requested Product quantity: " + currentQuantity);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

        // Otherwise, the update was successful and we can display a toast.
        Toast.makeText(this, getString(R.string.buy_supplier_confirmation),
                Toast.LENGTH_SHORT).show();
    }
}