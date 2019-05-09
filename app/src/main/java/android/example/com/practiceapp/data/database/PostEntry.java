package android.example.com.practiceapp.data.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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
    private String title;
    private String photoUrl;
    // comments
    // geo
    private Date date;

    public PostEntry(String id, String author, String title, String photoUrl, Date date) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.photoUrl = photoUrl;
        this.date = date;
    }

    public String getId() { return id; }

    public String getAuthor() { return author; }

    public String getTitle() { return title; }

    public String getPhotoUrl() { return photoUrl; }

    public Date getDate() { return date; }
}
