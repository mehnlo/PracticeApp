package android.example.com.practiceapp.data.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "user", indices = {@Index(value = {"email"}, unique = true)})
public class UserEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String displayName;
    private String email;
    private String photoUrl;
    private String tlfNo;
    private String sex;
    private Date lastLogin;


    public UserEntry (int id, String username, String displayName, String email, String photoUrl, String tlfNo, String sex, Date lastLogin) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.tlfNo = tlfNo;
        this.sex = sex;
        this.lastLogin = lastLogin;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getTlfNo() { return tlfNo; }

    public String getSex() { return sex; }

    public Date getLastLogin() { return lastLogin; }

}
