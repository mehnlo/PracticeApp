package android.example.com.practiceapp.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "post",
        indices = @Index(value = {"author"}),
        foreignKeys = @ForeignKey(entity = UserEntry.class,
            parentColumns = "id",
            childColumns = "author",
            onDelete = CASCADE))
public class PostEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String author;
    private String title;
    private String photoUrl;
    // comments
    // geo
    private Date date;

    public PostEntry(int id, String author, String title, String photoUrl, Date date) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.photoUrl = photoUrl;
        this.date = date;
    }

    public int getId() { return id; }

    public String getAuthor() { return author; }

    public String getTitle() { return title; }

    public String getPhotoUrl() { return photoUrl; }

    public Date getDate() { return date; }
}
