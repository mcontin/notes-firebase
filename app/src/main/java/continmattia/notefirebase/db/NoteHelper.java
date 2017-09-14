package continmattia.notefirebase.db;

import android.provider.BaseColumns;


public class NoteHelper implements BaseColumns {

    public static final String TABLE_NAME = "note";

    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_AUDIO_PATH = "audio_path";
    public static final String COL_IMAGE_PATH = "image_path";
    public static final String COL_SKETCH_PATH = "sketch_path";
    public static final String COL_TYPE = "type";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_NEXT_REMINDER = "next_reminder";
    public static final String COL_CAUDIO_EXISTS = "caudio_exists";
    public static final String COL_CID = "cid";
    public static final String COL_CIMAGE_EXISTS = "cimage_exists";
    public static final String COL_CSKETCH_EXISTS = "csketch_exists";
    public static final String EXT_COL_CATEGORY_ID = "category_id";

    public static final String CREATE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT NOT NULL, " +
                    COL_CONTENT + " TEXT, " +
                    COL_AUDIO_PATH + " TEXT, " +
                    COL_IMAGE_PATH + " TEXT, " +
                    COL_SKETCH_PATH + " TEXT, " +
                    COL_TYPE + " TEXT, " +
                    COL_CREATED_AT + " INTEGER, " +
                    COL_NEXT_REMINDER + " INTEGER, " +
                    COL_CID + " TEXT, " +
                    COL_CAUDIO_EXISTS + " BOOLEAN, " +
                    COL_CIMAGE_EXISTS + " BOOLEAN, " +
                    COL_CSKETCH_EXISTS + " BOOLEAN, " +
                    EXT_COL_CATEGORY_ID + " INTEGER, " +
                    "FOREIGN KEY(" + EXT_COL_CATEGORY_ID + ") REFERENCES " + CategoryHelper.TABLE_NAME + "(" + CategoryHelper._ID + ")" +
                    ");";

    public static final String DROP_QUERY =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
