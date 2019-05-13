package android.example.com.practiceapp.data.firebase;

import android.example.com.practiceapp.data.database.PostEntry;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

/**
 * Parser for Posts JSON data.
 */
final class PostsJsonParser {

    private static final String FEED = "feed";
    private static final String COMMENTS = "comments";
    private static final String DATE = "date";
    private static final String SECONDS = "_seconds";
    private static final String NANOSECONDS = "_nanoseconds";
    private static final String GEO = "geo";
    private static final String PHOTO_URL = "photoUrl";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String ID = "id";

    private static PostEntry[] fromJson(final JSONArray jsonPostsArray) throws JSONException {

        PostEntry[] postEntries = new PostEntry[jsonPostsArray.length()];

        for (int i = 0; i < jsonPostsArray.length(); i++) {
            // Get the JSON object representing the post
            JSONObject jsonPost = jsonPostsArray.getJSONObject(i);

            // Create the Post entry object
            PostEntry post = fromJson(jsonPost);

            postEntries[i] = post;
        }
        return postEntries;
    }

    private static PostEntry fromJson(final JSONObject jsonPost) throws JSONException {
        String id = jsonPost.getString(ID);
        JSONObject jsonDate = jsonPost.getJSONObject(DATE);
        Long seconds = jsonDate.getLong(SECONDS);
        int nanoseconds = jsonDate.getInt(NANOSECONDS);
        Timestamp timestamp = new Timestamp(seconds, nanoseconds);
        Date date = timestamp.toDate();
        String title = jsonPost.getString(TITLE);
        String photoUrl = jsonPost.getString(PHOTO_URL);
        String author = jsonPost.getString(AUTHOR);

        // Create the post entry object
        return new PostEntry(id, author, title, photoUrl, date);
    }

    /**
     * This method parses JSON from a Firebase Function response and returns an array of Strings
     * describing the feed
     * @param postsJsonObject JSON response from server
     * @return Array of Strings describing the feed data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    @Nullable
    PostsResponse parse(final Object postsJsonObject) throws JSONException {
        JSONObject feedJson = new JSONObject(postsJsonObject.toString());
        JSONArray postsArray = feedJson.getJSONArray(FEED);
        PostEntry[] postFeed = fromJson(postsArray);
        return new PostsResponse(postFeed);
    }
}
