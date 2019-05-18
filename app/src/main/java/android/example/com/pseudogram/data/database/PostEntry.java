package android.example.com.pseudogram.data.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;


//        foreignKeys = @ForeignKey(entity = UserEntry.class,
//            parentColumns = "id",
//            childColumns = "author",
//            onDelete = CASCADE))
@Entity(tableName = "post", indices = @Index(value = {"author"}))
public class PostEntry {
    @PrimaryKey
    @NonNull
    private String id;
    private String author;
    private String profilePic;
    private String title;
    private String photoUrl;
    // comments
    // geo
    private Date date;

    public PostEntry(@NonNull String id, String author, String profilePic, String title, String photoUrl, Date date) {
        this.id = id;
        this.author = author;
        this.profilePic = profilePic;
        this.title = title;
        this.photoUrl = photoUrl;
        this.date = date;
    }

    public String getId() { return id; }

    public String getAuthor() { return author; }

    public String getProfilePic() { return profilePic; }

    public String getTitle() { return title; }

    public String getPhotoUrl() { return photoUrl; }

    public Date getDate() { return date; }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
