package android.example.com.practiceapp.adapter;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Photo;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

public class GridPostFragment extends Fragment {

    public static final String TAG = GridPostFragment.class.getSimpleName();
    public static final int SPAN_COUNT = 3;
    public static final int PREFETCH_DISTANCE = 2;
    public static final int PAGE_SIZE = 6;

    private Context context;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore db;
    private CollectionReference mItemsCollection;
    private final Post mPost = new Post();


    public GridPostFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.content_grid_user_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSelected().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    Log.d(TAG, "onChanged: the user isn't null");
                    db = FirebaseFirestore.getInstance();
                    mPost.setUser(user);
                    String postsPath = "/posts/" + mPost.getUser().getEmail() + "/userPosts";
                    mItemsCollection = db.collection(postsPath);
                    setUpAdapter();
                } else {
                    Log.d(TAG, "onChanged: The user is null");
                }
            }
        });
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

        FirestorePagingAdapter<Post, ProfileImagesViewHolder> adapter =
                new FirestorePagingAdapter<Post, ProfileImagesViewHolder>(options) {
                    @NonNull
                    @Override
                    public ProfileImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        // Create a new View
                        View v = LayoutInflater.from(context).inflate(R.layout.profile_item, viewGroup, false);
                        return new ProfileImagesViewHolder(v);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull ProfileImagesViewHolder holder, int position, @NonNull Post model) {
                        holder.bind(model);
                    }

                    @Override
                    public int getItemViewType(int position) {
                        return super.getItemViewType(position);
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
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
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, SPAN_COUNT));
        mRecyclerView.setAdapter(adapter);
    }

    private void bindView() {
        mProgressBar = getView().findViewById(R.id.paging_loading);
        mRecyclerView = getView().findViewById(R.id.recycler_view);
    }

    private void showToast(String message){ Toast.makeText(context, message, Toast.LENGTH_SHORT).show(); }
}
