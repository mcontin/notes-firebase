package continmattia.notefirebase.db;

import android.provider.BaseColumns;

public class CategoryHelper implements BaseColumns {

    public static final String TABLE_NAME = "category";

    public static final String COL_CATEGORY_NAME = "name";
    public static final String COL_CID = "cid";

    public static final String CREATE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CATEGORY_NAME + " TEXT NOT NULL, " +
                    COL_CID + " TEXT " +
                    ");";

    public static final String DROP_QUERY =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
