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
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.products.data.ProductContract.ProductEntry;

/**
 * Allows user to create a new Product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

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
            int price = 0;
            String mailString = mMailEditText.getText().toString().trim();
            String unitsSoldString = mUnitsSold.getText().toString().trim();
            int units = 0;
            String totalSalesString = mSales.getText().toString().trim();
            int sales = 0;


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

            // If no units are sold , don't try to parse
            // the string into an integer value. Use 0 by default.

            if (!TextUtils.isEmpty(unitsSoldString)) {
                units = Integer.parseInt(unitsSoldString);
            }
            values.put(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD, units);

            // If no units are sold thera are no Sales, don't try to parse
            // the string into an integer value. Use 0 by default.

            if (!TextUtils.isEmpty(totalSalesString)) {
                sales = Integer.parseInt(totalSalesString);
            }
            values.put(ProductEntry.COLUMN_PRODUCT_SALES, sales);

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

            // Otherwise this is an EXISTING product, so update the pet with content
            // URI: mCurrentProductUri and pass in the new ContentValues. Pass in null
            // for the selection and selection args because mCurrentPetUri will already
            // identify the correct row in the database that we want to modify.

            // Read from input fields
            // Use trim to eliminate leading or trailing white space

            String nameString = mNameEditText.getText().toString().trim();
            String quantityString = mQuantityText.getText().toString().trim();
            int quantity = Integer.parseInt(quantityString);
            String priceString = mPriceEditText.getText().toString().trim();
            int price = Integer.parseInt(priceString);
            String mailString = mMailEditText.getText().toString().trim();

            // Create a ContentValues object where column names are the keys,
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, mailString);

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
                //ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};
                //ProductEntry.COLUMN_PRODUCT_UNITS_SOLD,
                //ProductEntry.COLUMN_PRODUCT_SALES};

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
            //int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            //int unitsSoldColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD);
            //int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

            // Extract out the value from the Cursor for the given column index
            //String image = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            //int unitsSold = cursor.getInt(unitsSoldColumnIndex);
            //int sales = cursor.getInt(salesColumnIndex);

            // Update the views on the screen with the values from the database
            //mImage.setImageBitmap(image);
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mMailEditText.setText(email);
            mQuantityText.setText(Integer.toString(quantity));
            //mUnitsSold.setText(Integer.toString(unitsSold));
            //mSales.setText(Integer.toString(sales));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Update the views on the screen with the values from the database
        //mImage
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mPriceEditText.setText(Integer.toString(0));
        mMailEditText.setText("");
        mQuantityText.setText(Integer.toString(0));
        //mUnitsSold.setText(Integer.toString(0));
        //mSales.setText(Integer.toString(0));

    }

    /**
     * Checkpoint to verify if the user updates part of the pet form.
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
}