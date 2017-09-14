package continmattia.notefirebase.model;

import com.google.firebase.database.Exclude;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

public class Note {

    private String noteId;
    private String title;
    private String content;

    @Exclude
    private String audioPath;
    @Exclude
    private String imagePath;
    private String sketchPath;
    private String noteType;
    private String categoryId;
    private long createdAt;
    private long nextReminder;
    private boolean cloudAudioExists;
    private boolean cloudImageExists;
    private boolean cloudSketchExists;

    public Note() {}

    public static Note makeRandomNote() {
        Lorem mLorem = LoremIpsum.getInstance();

        Note random = new Note();
        random.setTitle(mLorem.getWords(1, 3));
        random.setContent(mLorem.getWords(10, 40));

        return random;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSketchPath() {
        return sketchPath;
    }

    public void setSketchPath(String sketchPath) {
        this.sketchPath = sketchPath;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getNextReminder() {
        return nextReminder;
    }

    public void setNextReminder(long nextReminder) {
        this.nextReminder = nextReminder;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isCloudAudioExists() {
        return cloudAudioExists;
    }

    public void setCloudAudioExists(boolean cloudAudioExists) {
        this.cloudAudioExists = cloudAudioExists;
    }

    public boolean isCloudImageExists() {
        return cloudImageExists;
    }

    public void setCloudImageExists(boolean cloudImageExists) {
        this.cloudImageExists = cloudImageExists;
    }

    public boolean isCloudSketchExists() {
        return cloudSketchExists;
    }

    public void setCloudSketchExists(boolean cloudSketchExists) {
        this.cloudSketchExists = cloudSketchExists;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Note && ((Note) obj).getNoteId().equals(this.getNoteId());
    }
}
