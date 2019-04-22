package android.example.com.practiceapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Photo {

    private static final String FIELD_TITLE = "title";
    private static final String FIELD_PHOTO_URI = "photoUri";
    private static final String FIELD_GEO = "geo";
    public static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_COMMENTS = "comments";

    private String title;
    private String photoUri;
    private String geo;
    private Date timestamp;
    private Comment[] comments;

    public Photo() { // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Photo(String title, String photoUri, String geo, Date timestamp, Comment[] comments) {
        this.title = title;
        this.photoUri = photoUri;
        this.geo = geo;
        this.timestamp = timestamp;
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_TITLE, title);
        result.put(FIELD_PHOTO_URI, photoUri);
        result.put(FIELD_GEO, geo);
        result.put(FIELD_TIMESTAMP, new Timestamp(new Date()));
        result.put(FIELD_COMMENTS, comments);
        return result;
    }


}
