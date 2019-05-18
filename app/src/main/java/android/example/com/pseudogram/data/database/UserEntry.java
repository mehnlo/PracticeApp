package android.example.com.pseudogram.data.database;

import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
@Entity(tableName = "user")
public class UserEntry {

    @Ignore public static final String FIELD_EMAIL = "email";
    @Ignore public static final String FIELD_USERNAME = "username";
    @Ignore public static final String FIELD_DISPLAY_NAME = "displayName";
    @Ignore public static final String FIELD_PHOTO_URL = "photoUrl";
    @Ignore public static final String FIELD_TLF_NO = "tlfNo";
    @Ignore public static final String FIELD_GENDER = "gender";
    @Ignore public static final String FIELD_LAST_LOGIN = "lastLogin";

    @PrimaryKey @NonNull private String email;
    private String username;
    @ColumnInfo(name="display_name") private String displayName;
    @ColumnInfo(name="photo_url") private String photoUrl;
    @ColumnInfo(name="tlf_no") private String tlfNo;
    private String gender;
    @ColumnInfo(name="last_login") private Date lastLogin;

    @Exclude @Ignore private int genderIdItemPosition;
    @Exclude @Ignore private final ArrayList<String> genders = new ArrayList<>();

     // Default constructor required for calls to DataSnapshot.getValue(UserEntry.class)
     @Ignore public UserEntry() {}

    public UserEntry (@NonNull String email, String username, String displayName, String photoUrl, String tlfNo, String gender, Date lastLogin) {
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.tlfNo = tlfNo;
        this.gender = gender;
        this.lastLogin = lastLogin;
        if (genders.isEmpty()){
            String[] values = {"unspecified", "male", "female"};
            Collections.addAll(genders, values);
        }
        setGenderIdItemPosition(genders.indexOf(gender));
    }

    @NonNull public String getEmail() { return email; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getTlfNo() { return tlfNo; }

    public String getGender() { return gender; }

    public void setGender(String gender) {
        this.gender = gender;
        if (genders.isEmpty()){
            String[] values = {"unspecified", "male", "female"};
            Collections.addAll(genders, values);
        }
        setGenderIdItemPosition(genders.indexOf(gender));
    }

    public Date getLastLogin() { return lastLogin; }

    public int getGenderIdItemPosition() {
        return genderIdItemPosition;
    }

    public void setGenderIdItemPosition(int genderIdItemPosition) {
        this.genderIdItemPosition = genderIdItemPosition;
        this.gender = genders.get(genderIdItemPosition);
    }

    @NonNull @Override public String toString() {
        return "UserEntry{" +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", display_name='" + displayName + '\'' +
                ", photo_url='" + photoUrl + '\'' +
                ", tlf_no='" + tlfNo + '\'' +
                ", gender='" + gender + '\'' +
                ", last_login=" + lastLogin.toString() +
                '}';
    }

    @Exclude public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_EMAIL, email);
        if (username != null) result.put(FIELD_USERNAME, username);
        if (displayName != null) result.put(FIELD_DISPLAY_NAME, displayName);
        if (photoUrl != null) result.put(FIELD_PHOTO_URL, photoUrl);
        if (tlfNo != null) result.put(FIELD_TLF_NO, tlfNo);
        if (gender != null) result.put(FIELD_GENDER, gender);
        result.put(FIELD_LAST_LOGIN, FieldValue.serverTimestamp());
        return result;
    }

    @BindingAdapter({"android:profileImage"}) public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .circleCrop()
                .into(view);
    }
}
