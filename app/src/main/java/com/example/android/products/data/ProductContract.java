package com.example.android.products.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.Button;

/**
 * API Contract for the Inventory app.
 */

public class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.products";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.products/products/ is a valid path for
     * looking at product data. content://com.example.android.products/customer/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "costumer".
     */
    public static final String PATH_PRODUCTS = "products";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {
    }

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * Name of database table for the Inventory app
         */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the Activity (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Product Image.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Product Name.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Product Price.
         * Type: FLOAT
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Product Supplier email.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_EMAIL = "email";

        /**
         * Product Quantity.
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Product Units sold.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_UNITS_SOLD = "unitsSold";

        /**
         * Product Total Sales.
         * Type: FLOAT
         */
        public final static String COLUMN_PRODUCT_SALES = "totalSales";

    }
}
