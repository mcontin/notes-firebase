package continmattia.notefirebase.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import continmattia.notefirebase.model.Category;

public class NoteProvider extends ContentProvider {

    private static final String AUTHORITY = "continmattia.notefirebase.db";
    private static final String BASE_PATH_NOTES = "notes";
    private static final String BASE_PATH_CATEGORIES = "categories";

    public static final Uri NOTES_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_NOTES);
    public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_CATEGORIES);

    // query selectors for switch
    private static final int ALL_NOTES = 1;
    private static final int ONE_NOTE = 2;
    private static final int CATEGORY_NOTES = 3;
    private static final int ALL_CATEGORIES = 4;
    private static final int ONE_CATEGORY = 5;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(AUTHORITY, BASE_PATH_NOTES, ALL_NOTES);
        mUriMatcher.addURI(AUTHORITY, BASE_PATH_NOTES + "/*", ONE_NOTE);
        mUriMatcher.addURI(AUTHORITY, BASE_PATH_NOTES + "/cat/*", CATEGORY_NOTES);

        mUriMatcher.addURI(AUTHORITY, BASE_PATH_CATEGORIES, ALL_CATEGORIES);
        mUriMatcher.addURI(AUTHORITY, BASE_PATH_CATEGORIES + "/*", ONE_CATEGORY);
    }

    private DbHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        int uriType = mUriMatcher.match(uri);

        switch (uriType) {
            case ALL_NOTES:
                builder.setTables(NoteHelper.TABLE_NAME);
                break;
            case ONE_NOTE:
                builder.setTables(NoteHelper.TABLE_NAME);
                builder.appendWhere(NoteHelper.COL_CID + "='" + uri.getLastPathSegment() + "'");
                break;
            case CATEGORY_NOTES:
                builder.setTables(NoteHelper.TABLE_NAME);
                builder.appendWhere(NoteHelper.EXT_COL_CATEGORY_ID + "='" + uri.getLastPathSegment() + "'");
                break;
            case ALL_CATEGORIES:
                builder.setTables(CategoryHelper.TABLE_NAME);
                break;
            case ONE_CATEGORY:
                builder.setTables(CategoryHelper.TABLE_NAME);
                builder.appendWhere(CategoryHelper.COL_CID + "='" + uri.getLastPathSegment() + "'");
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = mUriMatcher.match(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        long id = 0;
        String path = "";

        switch (uriType) {
            case ALL_NOTES:
                path = BASE_PATH_NOTES;
                id = db.insert(NoteHelper.TABLE_NAME, null, values);
                break;
            case ALL_CATEGORIES:
                path = BASE_PATH_CATEGORIES;
                id = db.insert(CategoryHelper.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        db.close();

        return Uri.parse("content://" + AUTHORITY + "/" + path + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = mUriMatcher.match(uri);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String id = "";

        switch (uriType) {
            case ONE_NOTE:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(NoteHelper.TABLE_NAME,
                            NoteHelper.COL_CID + "='" + id + "'", null);
                } else {
                    rowsDeleted = db.delete(NoteHelper.TABLE_NAME,
                            NoteHelper.COL_CID + "='" + id + "' and " + selection, selectionArgs);
                }
                break;
            case ONE_CATEGORY:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CategoryHelper.TABLE_NAME,
                            CategoryHelper.COL_CID + "='" + id + "'", null);
                } else {
                    rowsDeleted = db.delete(CategoryHelper.TABLE_NAME,
                            CategoryHelper.COL_CID + "='" + id + "' and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = mUriMatcher.match(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rowsUpdated = 0;

        String id;
        switch (uriType) {
            case ONE_NOTE:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(NoteHelper.TABLE_NAME, values,
                            NoteHelper.COL_CID + "='" + id + "'", null);
                } else {
                    rowsUpdated = db.update(NoteHelper.TABLE_NAME, values,
                            NoteHelper.COL_CID + "='" + id + "' and " + selection, selectionArgs);
                }
                break;
            case ONE_CATEGORY:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(CategoryHelper.TABLE_NAME, values,
                            CategoryHelper.COL_CID + "='" + id + "'", null);
                } else {
                    rowsUpdated = db.update(CategoryHelper.TABLE_NAME, values,
                            CategoryHelper.COL_CID + "='" + id + "' and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        if (rowsUpdated > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}
