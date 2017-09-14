package continmattia.notefirebase.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import continmattia.notefirebase.R;
import continmattia.notefirebase.db.CategoryHelper;
import continmattia.notefirebase.db.NoteHelper;
import continmattia.notefirebase.db.NoteProvider;
import continmattia.notefirebase.dialog.AudioDialogFragment;
import continmattia.notefirebase.model.Category;
import continmattia.notefirebase.model.Note;
import continmattia.notefirebase.utils.BitmapUtils;
import continmattia.notefirebase.utils.FirebaseRefs;

public class NoteDetailActivity extends AppCompatActivity implements AudioDialogFragment.OnAudioCreatedListener {

    public static final String KEY_NOTE_ID = "id";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private String mNoteId = null;
    private Note mNote = new Note();
    private File mPhoto;

    @BindView(R.id.detail_title_tv)
    EditText mTitleTv;
    @BindView(R.id.detail_content_tv)
    EditText mContentTv;
    @BindView(R.id.detail_date_tv)
    TextView mDateTv;
    @BindView(R.id.note_photo_iv)
    ImageView mPhotoIv;
    @BindView(R.id.play_note_audio_btn)
    Button mPlayButton;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
    private FirebaseStorage mStorageRef = FirebaseStorage.getInstance();

    private List<Category> categories = new ArrayList<>();
    private File mAudioFile;
    private MediaPlayer mPlayer = new MediaPlayer();
    private boolean shouldPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        ButterKnife.bind(this);

        mNoteId = getIntent().getStringExtra(KEY_NOTE_ID);

        if (mNoteId != null) {
            selectNote(mNoteId);
        } else {
            setupEmptyView();
        }

        setupCategories();
    }

    private void selectNote(String noteId) {
        Uri uri = Uri.parse(NoteProvider.NOTES_URI + "/" + noteId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(NoteHelper.COL_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(NoteHelper.COL_CONTENT));
            long date = cursor.getLong(cursor.getColumnIndex(NoteHelper.COL_CREATED_AT));
            boolean hasPhoto = cursor.getInt(cursor.getColumnIndex(NoteHelper.COL_CIMAGE_EXISTS)) > 0;
            boolean hasAudio = cursor.getInt(cursor.getColumnIndex(NoteHelper.COL_CAUDIO_EXISTS)) > 0;
            String categoryId = cursor.getString(cursor.getColumnIndex(NoteHelper.EXT_COL_CATEGORY_ID));

            mNote.setTitle(title);
            mNote.setContent(content);
            mNote.setCreatedAt(date);
            mNote.setCloudImageExists(hasPhoto);
            mNote.setCloudAudioExists(hasAudio);
            mNote.setCategoryId(categoryId);

            setupView(title, content, date);

            cursor.close();
        } else {
            Toast.makeText(this, "There was an error loading the note", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupView(String title, String content, long lDate) {
        mTitleTv.setText(title);
        mContentTv.setText(content);

        Date date = new Date(lDate);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        String dateText = df.format(date);

        mDateTv.setText(dateText);

        if (mNote.isCloudImageExists()) {
            File photo = new File(getFilesDir(), mNoteId + ".jpg");
            if (photo.exists()) {
                Picasso.with(this).load(photo).into(mPhotoIv);
            } else {
                StorageReference ref = FirebaseRefs.getImagesRef(mStorageRef, mFirebaseUser.getUid()).child(mNoteId + ".jpg");
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(NoteDetailActivity.this).load(uri).into(mPhotoIv);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(NoteDetailActivity.this, "There was an error while loading the photo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        if (mNote.isCloudAudioExists()) {
            mAudioFile = new File(getFilesDir(), mNoteId + ".3gp");
            if (mAudioFile.exists()) {
                initAudioButton();
            } else {
                // todo download audio file
                Toast.makeText(this, "No local audio found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupEmptyView() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        String dateText = df.format(date);

        mDateTv.setText(dateText);
    }

    private void setupCategories() {
        Spinner spinner = (Spinner) findViewById(R.id.categories_spinner);
        final List<String> categoriesNames = new ArrayList<>();

        Cursor cursor = getContentResolver().query(NoteProvider.CATEGORIES_URI, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                categoriesNames.add("No category");
                categories.add(new Category());
                do {
                    Category cat = new Category();
                    cat.setCategoryId(cursor.getString(cursor.getColumnIndex(CategoryHelper.COL_CID)));
                    cat.setCategoryName(cursor.getString(cursor.getColumnIndex(CategoryHelper.COL_CATEGORY_NAME)));

                    categories.add(cat);
                    categoriesNames.add(cursor.getString(cursor.getColumnIndex(CategoryHelper.COL_CATEGORY_NAME)));
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoriesNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mNote.setCategoryId(null);
                } else {
                    mNote.setCategoryId(categories.get(position).getCategoryId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (mNote.getCategoryId() != null) {
            for (Category cat : categories) {
                if (categories.indexOf(cat) != 0) {
                    if (cat.getCategoryId().equals(mNote.getCategoryId())) {
                        spinner.setSelection(categories.indexOf(cat));
                    }
                }
            }
        }
    }

    @OnClick(R.id.action_add_photo)
    void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @OnClick(R.id.action_add_audio)
    void recordAudio() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private File bitmapToFile(Bitmap bitmap) {
        File filesDir = getFilesDir();
        File imageFile = new File(filesDir, "temp.jpg");

        if (imageFile.exists()) {
            imageFile.delete();
        }

        try {
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        return imageFile;
    }

    void saveNote() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notesReference = FirebaseRefs.getNotesRef(database, mFirebaseUser.getUid());

        mNote.setTitle(mTitleTv.getText().toString());
        mNote.setContent(mContentTv.getText().toString());

        if (mNoteId != null) {
            notesReference = notesReference.child(mNoteId);
        } else {
            notesReference = notesReference.push();
            mNoteId = notesReference.getKey();
            mNote.setCreatedAt(new Date().getTime());
        }
        mNote.setNoteId(mNoteId);

        if (mAudioFile != null) {
            File newFile = new File(getFilesDir(), mNoteId + ".3gp");
            mAudioFile.renameTo(newFile);
            mNote.setCloudAudioExists(true);
            mNote.setAudioPath(newFile.getAbsolutePath());
            uploadAudio(newFile);
        }

        if (mPhoto != null) {
            File newFile = new File(getFilesDir(), mNoteId + ".jpg");
            mPhoto.renameTo(newFile);
            mNote.setCloudImageExists(true);
            mNote.setImagePath(newFile.getAbsolutePath());
            upload(newFile);
            notesReference.setValue(mNote);
        } else {
            notesReference.setValue(mNote);
            finish();
        }
    }

    private void upload(File file) {
        Uri uri = Uri.fromFile(file);
        StorageReference riversRef = FirebaseRefs.getImagesRef(mStorageRef, mFirebaseUser.getUid()).child(file.getName());

        final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Uploading your note, please wait...");

        riversRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progress.dismiss();
                        Toast.makeText(NoteDetailActivity.this, "Upload succeeded", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progress.dismiss();
                        Toast.makeText(NoteDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAudio(File file) {
        Uri uri = Uri.fromFile(file);
        StorageReference riversRef = FirebaseRefs.getAudiosRef(mStorageRef, mFirebaseUser.getUid()).child(file.getName());

        riversRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(NoteDetailActivity.this, "Upload succeeded", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(NoteDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onAudioCreate(String audioPath) {
        mAudioFile = new File(audioPath);
        initAudioButton();
    }

    private void initAudioButton() {
        mPlayButton.setVisibility(View.VISIBLE);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldPlay) {
                    mPlayButton.setText("Stop playing");
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(mAudioFile.getAbsolutePath());
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e("Error audio", "onClick: error playing audio");
                    }
                } else {
                    mPlayButton.setText("Start playing");
                    mPlayer.release();
                    mPlayer = null;
                }
                shouldPlay = !shouldPlay;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AudioDialogFragment.newInstance().show(getSupportFragmentManager(), "audio");
                } else {
                    Toast.makeText(this, "Audio permission required to record", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPhoto = bitmapToFile(BitmapUtils.cropCenter(imageBitmap));
            Picasso.with(this).load(mPhoto).into(mPhotoIv);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_note_menu:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
