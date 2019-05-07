package android.example.com.practiceapp.data.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Comment {
    private String userId;
    private String body;
    private String timestamp;

    public Comment() { // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Comment(String userId, String body, String timestamp) {
        this.userId = userId;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("body", body);
        result.put("timestamp", timestamp);
        return result;
    }
}
