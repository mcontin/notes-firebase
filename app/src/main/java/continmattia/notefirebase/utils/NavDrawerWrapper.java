package continmattia.notefirebase.utils;

import android.app.Activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import continmattia.notefirebase.R;
import continmattia.notefirebase.model.User;

public class NavDrawerWrapper {

    private static final int ID_PROFILE = 1000;
    public static final int ID_NOTES = 1001;
    public static final int ID_CATEGORIES = 1002;
    public static final int ID_LOGOUT = 1003;
    public static final int ID_DELETE_ACCOUNT = 1004;
    public static final int ID_SETTINGS = 1005;

    private static final String ITEM_NOTES = "Notes";
    private static final String ITEM_CATEGORIES = "Categories";
    private static final String ITEM_LOGOUT = "Logout";
    private static final String ITEM_DELETE_ACCOUNT = "Delete account";
    private static final String ITEM_SETTINGS = "Settings";

    private static IProfile getProfileFrom(User user) {
        return new ProfileDrawerItem()
                .withName(user.getUserName())
                .withIcon(user.getPhotoUrl())
                .withEmail(user.getEmailAddress())
                .withIdentifier(ID_PROFILE);
    }

    public static AccountHeader buildHeaderFromUser(Activity activity, User user) {
        return new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.universe)
                .addProfiles(getProfileFrom(user))
                .build();
    }

    public static PrimaryDrawerItem makeNotesItem() {
        return new PrimaryDrawerItem()
                .withName(NavDrawerWrapper.ITEM_NOTES)
                .withIcon(GoogleMaterial.Icon.gmd_view_list)
                .withIdentifier(NavDrawerWrapper.ID_NOTES);
    }

    public static PrimaryDrawerItem makeCategoriesItem() {
        return new PrimaryDrawerItem()
                        .withName(NavDrawerWrapper.ITEM_CATEGORIES)
                        .withIcon(GoogleMaterial.Icon.gmd_folder)
                        .withIdentifier(NavDrawerWrapper.ID_CATEGORIES);
    }

    public static PrimaryDrawerItem makeSettingsItem() {
        return new PrimaryDrawerItem()
                .withName(NavDrawerWrapper.ITEM_SETTINGS)
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(NavDrawerWrapper.ID_SETTINGS);
    }

    public static PrimaryDrawerItem makeLogoutItem() {
        return new PrimaryDrawerItem()
                .withName(NavDrawerWrapper.ITEM_LOGOUT)
                .withIcon(GoogleMaterial.Icon.gmd_lock)
                .withIdentifier(NavDrawerWrapper.ID_LOGOUT);
    }

    public static PrimaryDrawerItem makeDeleteAccountItem() {
        return new PrimaryDrawerItem()
                .withName(NavDrawerWrapper.ITEM_DELETE_ACCOUNT)
                .withIcon(GoogleMaterial.Icon.gmd_delete)
                .withIdentifier(NavDrawerWrapper.ID_DELETE_ACCOUNT);
    }

}
