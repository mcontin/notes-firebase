package continmattia.notefirebase.activity;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import continmattia.notefirebase.model.Note;

abstract class SQLiteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
