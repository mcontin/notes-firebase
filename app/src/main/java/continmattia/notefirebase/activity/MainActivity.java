package continmattia.notefirebase.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import continmattia.notefirebase.R;
import continmattia.notefirebase.adapter.CategoriesCursorAdapter;
import continmattia.notefirebase.adapter.NotesCursorAdapter;
import continmattia.notefirebase.db.NoteProvider;
import continmattia.notefirebase.dialog.AddCategoryDialogFragment;
import continmattia.notefirebase.model.Category;
import continmattia.notefirebase.model.Note;
import continmattia.notefirebase.model.User;
import continmattia.notefirebase.utils.FirebaseRefs;
import continmattia.notefirebase.utils.NavDrawerWrapper;
import continmattia.notefirebase.utils.SQLiteWrapper;

import static continmattia.notefirebase.utils.NavDrawerWrapper.ID_CATEGORIES;
import static continmattia.notefirebase.utils.NavDrawerWrapper.ID_NOTES;

public class MainActivity extends SQLiteActivity
        implements NotesCursorAdapter.OnNoteInteractionListener,
        AddCategoryDialogFragment.OnCategoryCreateListener,
        CategoriesCursorAdapter.OnCategoryInteractionListener {

    private static final int KEY_LOGIN_ID = 1;
    private static final String TAG = "MainActivity";
    private static final String KEY_CATEGORY_ID = "CATEGORY_ID";

    private User mCurrentUser;

    private NotesCursorAdapter mNotesCursorAdapter = new NotesCursorAdapter(this, null);
    private CategoriesCursorAdapter mCategoriesCursorAdapter = new CategoriesCursorAdapter(this, null);

    private int mCurrentSelection = NavDrawerWrapper.ID_NOTES;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.notes_lv)
    ListView mListView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClicked();
            }
        });

        setSupportActionBar(mToolbar);

        setupUserSession();

        setupListView();
    }

    private void onPostLogin() {
        mCurrentUser = new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString(), mFirebaseUser.getEmail());
        subscribeToFirebaseEvents();
        initNavDrawer();
    }

    private void subscribeToFirebaseEvents() {
        DatabaseReference notesReference = FirebaseRefs.getNotesRef(database, mFirebaseUser.getUid());
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Note note = dataSnapshot.getValue(Note.class);
                if (note != null) {
                    note.setNoteId(dataSnapshot.getKey());
                    SQLiteWrapper.saveNote(MainActivity.this, note);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Note note = dataSnapshot.getValue(Note.class);
                SQLiteWrapper.editNote(MainActivity.this, note);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Note note = dataSnapshot.getValue(Note.class);
                SQLiteWrapper.deleteNote(MainActivity.this, note);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load notes.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        notesReference.addChildEventListener(childEventListener);

        notesReference = FirebaseRefs.getCategoriesRef(database, mFirebaseUser.getUid());
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null) {
                    category.setCategoryId(dataSnapshot.getKey());
                    SQLiteWrapper.saveCategory(MainActivity.this, category);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Category category = dataSnapshot.getValue(Category.class);
                SQLiteWrapper.editCategory(MainActivity.this, category);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                SQLiteWrapper.deleteCategory(MainActivity.this, category);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load notes.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        notesReference.addChildEventListener(childEventListener);
    }

    private void setupListView() {
        getSupportLoaderManager().initLoader(NavDrawerWrapper.ID_NOTES, null, this);
        mListView.setAdapter(mNotesCursorAdapter);
    }

    private void setupUserSession() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            login();
        } else {
            onPostLogin();
        }
    }

    private void initNavDrawer() {
        if (mCurrentUser == null) {
            mCurrentUser = User.createAnonymous();
        }

        AccountHeader mHeader = NavDrawerWrapper.buildHeaderFromUser(this, mCurrentUser);

        Drawer mDrawer = new DrawerBuilder()
                .withAccountHeader(mHeader)
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        NavDrawerWrapper.makeNotesItem(),
                        NavDrawerWrapper.makeCategoriesItem(),
                        NavDrawerWrapper.makeSettingsItem(),
                        NavDrawerWrapper.makeLogoutItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            if (drawerItem instanceof Nameable) {
                                String name = ((Nameable) drawerItem).getName().getText(MainActivity.this);
                                mToolbar.setTitle(name);
                            }
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                    }
                })
                .withFireOnInitialOnClick(true)
                .build();

        mDrawer.addStickyFooterItem(NavDrawerWrapper.makeDeleteAccountItem());
    }

    private void onTouchDrawer(int identifier) {
        mCurrentSelection = identifier;

        switch (identifier) {
            case NavDrawerWrapper.ID_NOTES:
                getSupportLoaderManager().restartLoader(NavDrawerWrapper.ID_NOTES, null, this);
                break;
            case NavDrawerWrapper.ID_CATEGORIES:
                getSupportLoaderManager().restartLoader(NavDrawerWrapper.ID_CATEGORIES, null, this);
                break;
            case NavDrawerWrapper.ID_SETTINGS:
                Toast.makeText(this, "Settings not implemented", Toast.LENGTH_SHORT).show();
                break;
            case NavDrawerWrapper.ID_LOGOUT:
                logout();
                break;
            case NavDrawerWrapper.ID_DELETE_ACCOUNT:
                deleteAccount();
                break;
        }
    }

    private void logout() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    login();
                } else {
                    Toast.makeText(MainActivity.this, "Signout failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteAccount() {
        AuthUI.getInstance().delete(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    login();
                } else {
                    Toast.makeText(MainActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login() {
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())).build(),
                KEY_LOGIN_ID);
    }

    private void showSnackbar(@StringRes int stringId) {
        Snackbar.make(mToolbar, stringId, Snackbar.LENGTH_SHORT).show();
    }

    private void onFabClicked() {
        switch (mCurrentSelection) {
            case ID_NOTES:
                Intent intent = new Intent(this, NoteDetailActivity.class);
                startActivity(intent);
                break;
            case ID_CATEGORIES:
                AddCategoryDialogFragment.getInstance().show(getSupportFragmentManager(), "category");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KEY_LOGIN_ID) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                onPostLogin();
                return;
            } else {
                if (response == null) {
                    showSnackbar(R.string.error_login_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.error_no_network);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.error_unknown);
                    return;
                }
            }

            showSnackbar(R.string.error_unknown_response);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case NavDrawerWrapper.ID_NOTES:
                if (args == null) {
                    return new CursorLoader(this, NoteProvider.NOTES_URI, null, null, null, null);
                } else {
                    String categoryId = args.getString(KEY_CATEGORY_ID);
                    Uri uri = Uri.withAppendedPath(NoteProvider.NOTES_URI, "cat/" + categoryId);
                    return new CursorLoader(this, uri, null, null, null, null);
                }
            case NavDrawerWrapper.ID_CATEGORIES:
                return new CursorLoader(this, NoteProvider.CATEGORIES_URI, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case NavDrawerWrapper.ID_NOTES:
                mNotesCursorAdapter.swapCursor(cursor);
                mListView.setAdapter(mNotesCursorAdapter);
                mToolbar.setTitle("Notes");
                break;
            case NavDrawerWrapper.ID_CATEGORIES:
                mCategoriesCursorAdapter.swapCursor(cursor);
                mListView.setAdapter(mCategoriesCursorAdapter);
                mToolbar.setTitle("Categories");
                break;
            default:
        }
    }

    @Override
    public void onNoteClicked(String id) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.KEY_NOTE_ID, id);
        startActivity(intent);
    }

    @Override
    public void onNoteDelete(final String id) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.root_cl),
                "Delete the selected note?", Snackbar.LENGTH_LONG);
        mySnackbar.setAction("DELETE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseRefs.getNotesRef(database, mFirebaseUser.getUid()).child(id).removeValue();
            }
        });
        mySnackbar.show();
    }

    @Override
    public void onCategoryClicked(String id) {
        // Commentato perchè l'URI non viene matchato in NoteProvider e viene lanciata un'eccezione
//        Bundle cursorExtras = new Bundle();
//        cursorExtras.putString(KEY_CATEGORY_ID, id);
//        getSupportLoaderManager().restartLoader(ID_NOTES, cursorExtras, this);
    }

    @Override
    public void onCategoryDelete(final String id) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.root_cl),
                "Delete the selected category?", Snackbar.LENGTH_LONG);
        mySnackbar.setAction("DELETE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseRefs.getCategoriesRef(database, mFirebaseUser.getUid()).child(id).removeValue();
            }
        });
        mySnackbar.show();
    }

    @Override
    public void onCategoryCreated(String name) {
        DatabaseReference categoriesRef = FirebaseRefs.getCategoriesRef(database, mFirebaseUser.getUid());

        categoriesRef = categoriesRef.push();
        Category cat = new Category(name);
        cat.setCategoryId(categoriesRef.getKey());

        categoriesRef.setValue(cat);
    }
}
