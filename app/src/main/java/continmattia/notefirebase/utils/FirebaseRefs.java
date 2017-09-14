package continmattia.notefirebase.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseRefs {

    private static final String REF_USERS = "users";
    private static final String REF_NOTES = "notes";
    private static final String REF_CATEGORIES = "categories";
    private static final String REF_IMAGES = "images";
    private static final String REF_AUDIOS = "audios";

    public static DatabaseReference getNotesRef(FirebaseDatabase db, String userId) {
        return db.getReference().child(REF_USERS).child(userId).child(REF_NOTES);
    }

    public static DatabaseReference getCategoriesRef(FirebaseDatabase db, String userId) {
        return db.getReference().child(REF_USERS).child(userId).child(REF_CATEGORIES);
    }

    public static StorageReference getImagesRef(FirebaseStorage storage, String userId) {
        return storage.getReference().child(REF_USERS).child(userId).child(REF_IMAGES);
    }

    public static StorageReference getAudiosRef(FirebaseStorage storage, String userId) {
        return storage.getReference().child(REF_USERS).child(userId).child(REF_AUDIOS);
    }

}
