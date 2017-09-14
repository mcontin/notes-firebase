package continmattia.notefirebase.model;

public class User {
    private String userName;
    private String photoUrl;
    private String emailAddress;

    public User(String userName, String photoUrl, String emailAddress) {
        this.userName = userName;
        this.photoUrl = photoUrl;
        this.emailAddress = emailAddress;
    }

    public static User createAnonymous() {
        return new User("Anonymous", "https://www.wheretotonight.com/melbourne/images/empty_profile.png", "");
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
