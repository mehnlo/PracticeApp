package android.example.com.practiceapp.models;


import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    public static final String FIELD_UID = "uid";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_DISPLAYNAME = "displayName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PHOTO_URL = "photoUrl";
    public static final String FIELD_TLFNO = "tlfNo";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_LAST_LOGIN = "lastLogin";

    private String uid;
    private String username;
    private String displayName;
    private String email;
    private String photoUrl;
    private String tlfNo;
    private String sex;
    private Long lastLogin;

    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User (String uid, String username, String displayName, String email, String photoUrl, String tlfNo, String sex, Long lastLogin ) {
        this.uid = uid;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.tlfNo = tlfNo;
        this.sex = sex;
        this.lastLogin = lastLogin;
    }
    public void setUser(User user) {
        uid = user.uid;
        username = user.username;
        displayName = user.displayName;
        email = user.email;
        photoUrl = user.photoUrl;
        tlfNo = user.tlfNo;
        sex = user.sex;
        lastLogin = user.lastLogin;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", tlfNo='" + tlfNo + '\'' +
                ", sex='" + sex + '\'' +
                ", lastLogin=" + lastLogin +
                '}';
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        if (uid != null) result.put(FIELD_UID, uid);
        if (username != null) result.put(FIELD_USERNAME, username);
        if (displayName != null) result.put(FIELD_DISPLAYNAME, displayName);
        if (email != null) result.put(FIELD_EMAIL, email);
        if (photoUrl != null) result.put(FIELD_PHOTO_URL, photoUrl);
        if (tlfNo != null) result.put(FIELD_TLFNO, tlfNo);
        if (sex != null) result.put(FIELD_SEX, sex);
        result.put(FIELD_LAST_LOGIN, FieldValue.serverTimestamp());
        return result;
    }
}
