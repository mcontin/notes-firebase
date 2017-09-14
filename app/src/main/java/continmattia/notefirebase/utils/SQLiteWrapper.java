package continmattia.notefirebase.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import continmattia.notefirebase.db.CategoryHelper;
import continmattia.notefirebase.db.NoteHelper;
import continmattia.notefirebase.db.NoteProvider;
import continmattia.notefirebase.model.Category;
import continmattia.notefirebase.model.Note;

public class SQLiteWrapper {

    public static void saveNote(Context context, Note note) {
        Uri uri = Uri.parse(NoteProvider.NOTES_URI + "/" + note.getNoteId());
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);

        if (c != null) {
            if (!c.moveToFirst()) {
                ContentValues values = new ContentValues();

                values.put(NoteHelper.COL_CID, note.getNoteId());
                values.put(NoteHelper.COL_TITLE, note.getTitle());
                values.put(NoteHelper.COL_CONTENT, note.getContent());
                values.put(NoteHelper.COL_CREATED_AT, note.getCreatedAt());
                values.put(NoteHelper.COL_CIMAGE_EXISTS, note.isCloudImageExists());
                values.put(NoteHelper.COL_CAUDIO_EXISTS, note.isCloudAudioExists());
                values.put(NoteHelper.EXT_COL_CATEGORY_ID, note.getCategoryId());

                context.getContentResolver().insert(NoteProvider.NOTES_URI, values);
            } else {
                editNote(context, note);
            }
            c.close();
        }
    }

    public static void editNote(Context context, Note note) {
        ContentValues values = new ContentValues();

        values.put(NoteHelper.COL_TITLE, note.getTitle());
        values.put(NoteHelper.COL_CONTENT, note.getContent());
        values.put(NoteHelper.COL_CIMAGE_EXISTS, note.isCloudImageExists());
        values.put(NoteHelper.COL_CAUDIO_EXISTS, note.isCloudAudioExists());
        values.put(NoteHelper.EXT_COL_CATEGORY_ID, note.getCategoryId());

        Uri uri = Uri.parse(NoteProvider.NOTES_URI + "/" + note.getNoteId());

        context.getContentResolver().update(uri, values, null, null);
    }

    public static void deleteNote(Context context, Note note) {
        Uri uriToDelete = Uri.parse(NoteProvider.NOTES_URI + "/" + note.getNoteId());
        context.getContentResolver().delete(uriToDelete, null, null);
    }

    public static void saveCategory(Context context, Category category) {
        Uri uri = Uri.parse(NoteProvider.CATEGORIES_URI + "/" + category.getCategoryId());
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);

        if (c != null) {
            if (!c.moveToFirst()) {
                ContentValues values = new ContentValues();

                values.put(CategoryHelper.COL_CATEGORY_NAME, category.getCategoryName());
                values.put(CategoryHelper.COL_CID, category.getCategoryId());

                context.getContentResolver().insert(NoteProvider.CATEGORIES_URI, values);
            } else {
                editCategory(context, category);
            }
            c.close();
        }
    }

    public static void editCategory(Context context, Category category) {
        ContentValues values = new ContentValues();

        values.put(CategoryHelper.COL_CATEGORY_NAME, category.getCategoryName());

        Uri uri = Uri.parse(NoteProvider.CATEGORIES_URI + "/" + category.getCategoryId());

        context.getContentResolver().update(uri, values, null, null);
    }

    public static void deleteCategory(Context context, Category category) {
        Uri uriToDelete = Uri.parse(NoteProvider.CATEGORIES_URI + "/" + category.getCategoryId());
        context.getContentResolver().delete(uriToDelete, null, null);
    }

}
