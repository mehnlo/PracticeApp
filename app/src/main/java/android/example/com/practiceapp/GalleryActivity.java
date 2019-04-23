package android.example.com.practiceapp;

import android.arch.paging.PagedList;
import android.content.SharedPreferences;
import android.example.com.practiceapp.adapter.PostViewHolder;
import android.example.com.practiceapp.models.Photo;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.models.User;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 4;
    private static final int PREFETCH_DISTANCE = 2;
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItemsCollection;
    private final Post mPost = new Post();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        bindView();
        initUser();

        mFirestore = FirebaseFirestore.getInstance();
        String collectionPath = "/posts/" + mPost.getUser().getEmail() + "/userPosts";
        mItemsCollection = mFirestore.collection(collectionPath);

        setUpAdapter();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpAdapter() {
        Query baseQuery = mItemsCollection.orderBy(Photo.FIELD_DATE, Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .setPageSize(PAGE_SIZE)
                .build();

        SnapshotParser<Post> parser = new SnapshotParser<Post>() {
            @NonNull
            @Override
            public Post parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Post post = new Post();
                post.setUser(mPost.getUser());
                if (snapshot.exists()) {
                    Photo photo = snapshot.toObject(Photo.class);
                    post.setPhoto(photo);
                }
                return post;
            }
        };

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, parser)
                .build();

        FirestorePagingAdapter<Post, PostViewHolder> adapter =
                new FirestorePagingAdapter<Post, PostViewHolder>(options) {

            @NonNull
            @Override public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
                return new PostViewHolder(view);
            }
            @Override protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
                holder.bind(model);

            }
            @Override protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case FINISHED:
                        mProgressBar.setVisibility(View.GONE);
                        showToast("Reached end of data set.");
                        break;
                    case ERROR:
                        showToast("An error ocurred");
                        retry();
                        break;
                }
            }
        };

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(adapter);
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void bindView() {
        mRecycler = findViewById(R.id.paging_recycler);
        mProgressBar = findViewById(R.id.paging_loading);
    }
    private void initUser(){
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String email = prefs.getString(getString(R.string.account_email_key), "");
        String username = prefs.getString(getString(R.string.account_name_key), "");
        String photoUrl = prefs.getString(getString(R.string.account_photo_key), "");
        User mUser = new User();
        mUser.setEmail(email);
        mUser.setUsername(username);
        mUser.setPhotoUrl(photoUrl);
        mPost.setUser(mUser);
    }
}
