package android.example.com.pseudogram.data.database;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.text.DateFormat.getDateInstance;

@IgnoreExtraProperties
@Entity(tableName = "post", indices = @Index(value = {"author"}))
public class PostEntry {

    @Ignore private static final String FIELD_ID = "id";
    @Ignore private static final String FIELD_AUTHOR = "author";
    @Ignore private static final String FIELD_PROFILE_PIC = "profilePic";
    @Ignore public static final String FIELD_TITLE = "title";
    @Ignore public static final String FIELD_PHOTO_URL = "photoUrl";
    @Ignore public static final String FIELD_DATE = "date";

    @PrimaryKey @NonNull private String id;
    private String author;
    @ColumnInfo(name = "profile_pic") private String profilePic;
    private String title;
    @ColumnInfo(name = "photo_url") private String photoUrl;
    private Date date;

    // Default constructor required for calls to DataSnapshot.getValue(PostEntry.class)
    @Ignore public PostEntry() {}

    public PostEntry(@NonNull String id, String author, String profilePic, String title, String photoUrl, Date date) {
        this.id = id;
        this.author = author;
        this.profilePic = profilePic;
        this.title = title;
        this.photoUrl = photoUrl;
        this.date = date;
    }

    @NonNull public String getId() { return id; }

    public void setId(@NonNull String id) { this.id = id; }

    public String getAuthor() { return author; }

    public String getProfilePic() { return profilePic; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getPhotoUrl() { return photoUrl; }

    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Date getDate() { return date; }

    @Override public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull @Override public String toString() {
        return "PostEntry{" +
                FIELD_ID + "='" + id + '\'' +
                ", " + FIELD_AUTHOR + "='" + author + '\'' +
                ", " + FIELD_PROFILE_PIC + "='" + author + '\'' +
                ", " + FIELD_TITLE + "='" + title + '\'' +
                ", " + FIELD_PHOTO_URL + "='" + photoUrl + '\'' +
                ", " + FIELD_DATE + "=" + date +
                '}';
    }

    @Exclude public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        if (title != null) result.put(FIELD_TITLE, title);
        if (photoUrl != null) result.put(FIELD_PHOTO_URL, photoUrl);
        result.put(FIELD_DATE, new Timestamp(new Date()));
        return result;
    }

    @BindingAdapter("android:postImage") public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext()).load(imageUrl).into(view);
    }

    @BindingAdapter("android:text") public static void setText(TextView view, Date date) {
        String formatted = getDateInstance().format(date);
        view.setText(formatted);
    }
}
