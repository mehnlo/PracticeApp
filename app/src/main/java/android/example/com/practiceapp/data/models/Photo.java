package android.example.com.practiceapp.data.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Photo {

    private static final String FIELD_TITLE = "title";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String FIELD_GEO = "geo";
    public static final String FIELD_DATE = "date";
    private static final String FIELD_COMMENTS = "comments";

    private String title;
    private String photoUrl;
    private String geo;
    private Date date;
    private Comment[] comments;

    public Photo() { // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Photo(String title, String photoUrl, String geo, Date date, Comment[] comments) {
        this.title = title;
        this.photoUrl = photoUrl;
        this.geo = geo;
        this.date = date;
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "title='" + title + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", geo='" + geo + '\'' +
                ", date=" + date +
                ", comments=" + Arrays.toString(comments) +
                '}';
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_TITLE, title);
        result.put(FIELD_PHOTO_URL, photoUrl);
        result.put(FIELD_GEO, geo);
        result.put(FIELD_DATE, new Timestamp(new Date()));
        result.put(FIELD_COMMENTS, comments);
        return result;
    }


}
