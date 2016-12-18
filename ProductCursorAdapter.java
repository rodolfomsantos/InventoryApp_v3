package com.example.android.products;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.products.data.ProductContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {


    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Track Sell Button
     */

    private Button trackSellButton;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView unitsSoldTextView = (TextView) view.findViewById(R.id.catalog_units_sold);
        final TextView totalSalesTextView = (TextView) view.findViewById(R.id.catalog_total_sales);
        trackSellButton = (Button) view.findViewById(R.id.track_sell_button);

        // Find the columns of product attributes that we're interested
        final int rowId = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int unitsSoldColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD);
        int totalSalesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

        // Read the products attributes from the cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);
        final String productPrice = cursor.getString(priceColumnIndex);
        final String productUnitsSold = cursor.getString(unitsSoldColumnIndex);
        String productTotalSales = cursor.getString(totalSalesColumnIndex);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        quantityTextView.setText(productQuantity);
        priceTextView.setText(productPrice);
        unitsSoldTextView.setText(productUnitsSold);
        totalSalesTextView.setText(productTotalSales);

        // Set the track Sell Button
        trackSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int mProductQuantity = Integer.parseInt(quantityTextView.getText().toString());
                int mProductUnitsSold = Integer.parseInt(unitsSoldTextView.getText().toString());
                float mPrice = Float.parseFloat(priceTextView.getText().toString());
                float mTotalSales = Float.parseFloat(totalSalesTextView.getText().toString());

                if (mProductQuantity == 0) {

                    // If the product quantity is 0 then there are no products to sell.
                    Toast.makeText(context.getApplicationContext(), "No more Products to sell", Toast.LENGTH_SHORT).show();

                } else {

                    mProductQuantity--;
                    mProductUnitsSold++;
                    mTotalSales = mProductUnitsSold * mPrice;

                    Toast.makeText(context.getApplicationContext(), "Sales tracker updated", Toast.LENGTH_SHORT).show();
                }
                // Create a ContentValues object where column names are the keys,
                ContentValues values = new ContentValues();

                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mProductQuantity);
                values.put(ProductEntry.COLUMN_PRODUCT_UNITS_SOLD, mProductUnitsSold);
                values.put(ProductEntry.COLUMN_PRODUCT_SALES, mTotalSales);

                Uri mCurrentInventoryUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, rowId);
                int rowsAffected = context.getContentResolver().update(mCurrentInventoryUri, values, null, null);

            }

        });


    }
}
