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
import android.support.v7.widget.LinearLayoutManager;
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

public class ListPostFragment extends Fragment {
    private static final String TAG = ListPostFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 4;
    private static final int PREFETCH_DISTANCE = 2;
    private Context context;
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private FirebaseFirestore db;
    private CollectionReference mItemsCollection;
    private final Post mPost = new Post();

    public ListPostFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list_user, container, false);
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

        FirestorePagingAdapter<Post, ListPostViewHolder> adapter =
                new FirestorePagingAdapter<Post, ListPostViewHolder>(options) {

                    @NonNull
                    @Override public ListPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_post, parent, false);
                        return new ListPostViewHolder(view);
                    }
                    @Override protected void onBindViewHolder(@NonNull ListPostViewHolder holder, int position, @NonNull Post model) {
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

        mRecycler.setLayoutManager(new LinearLayoutManager(context));
        mRecycler.setAdapter(adapter);
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void bindView() {
        mProgressBar = getView().findViewById(R.id.paging_loading);
        mRecycler = getView().findViewById(R.id.recycler_view);
    }

}
