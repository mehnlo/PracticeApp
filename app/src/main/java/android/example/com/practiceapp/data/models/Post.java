package android.example.com.practiceapp.data.models;

import android.example.com.practiceapp.data.database.UserEntry;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {
    private static final String FIELD_USER = "user";
    private static final String FIELD_PHOTOS = "photos";
    private UserEntry user;
    private Photo photo;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(UserEntry user, Photo photo) {
        this.user = user;
        this.photo = photo;
    }

    public UserEntry getUser() {
        return user;
    }

    public void setUser(UserEntry user) {
        this.user = user;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_USER, user.toMap());
        result.put(FIELD_PHOTOS, photo.toMap());
        return result;
    }

    @Override
    public String toString() {
        return "Post{" +
                "user=" + user.toString() +
                ", photo=" + photo.toString() +
                '}';
    }
}
