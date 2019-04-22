package android.example.com.practiceapp.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {
    private static final String FIELD_USER = "user";
    private static final String FIELD_PHOTOS = "photos";
    private User user;
    private Photo photo;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(User user, Photo photo) {
        this.user = user;
        this.photo = photo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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
}
