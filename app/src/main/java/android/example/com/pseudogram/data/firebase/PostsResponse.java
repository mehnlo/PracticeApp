package android.example.com.pseudogram.data.firebase;


import android.example.com.pseudogram.data.database.PostEntry;

import androidx.annotation.NonNull;

/**
 * Posts response from the backend. Contains the posts feed.
 */
public class PostsResponse {
    @NonNull
    private final PostEntry[] mPostFeed;

    public PostsResponse(@NonNull final PostEntry[] postFeed) {
        mPostFeed = postFeed;
    }

    public PostEntry[] getPostsFeed() { return mPostFeed; }

}
