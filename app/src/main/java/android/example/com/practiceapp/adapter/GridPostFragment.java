package android.example.com.practiceapp.adapter;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.example.com.practiceapp.DetailPostFragment;
import android.example.com.practiceapp.utilities.OnPostSelectedListener;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Photo;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.viewmodel.PostViewModel;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class GridPostFragment extends Fragment
        implements OnPostSelectedListener
{

    public static final String TAG = GridPostFragment.class.getSimpleName();
    public static final int SPAN_COUNT = 3;
    public static final int PREFETCH_DISTANCE = 2;
    public static final int PAGE_SIZE = 6;
    public static final String DETAIL_POST_FRAGMENT = "DETAIL_POST_FRAGMENT";

    private Context context;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore db;
    private CollectionReference mItemsCollection;
    private final Post mPost = new Post();
    private PostViewModel postViewModel;


    public GridPostFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_content_grid_user, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        subscribeToModel();
        postViewModel = ViewModelProviders.of(getActivity()).get(PostViewModel.class);
        postViewModel.getPostSelected();
    }

    private void subscribeToModel() {
        UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSelected().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
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

        GridPostAdapter adapter = new GridPostAdapter(options, context, mProgressBar);
        adapter.setCallback(GridPostFragment.this);
        adapter.setViewModel(postViewModel);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, SPAN_COUNT));
        mRecyclerView.setAdapter(adapter);
    }

    private void bindView() {
        mProgressBar = getView().findViewById(R.id.paging_loading);
        mRecyclerView = getView().findViewById(R.id.recycler_view);
    }

    @Override
    public void onPostSelected() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, new DetailPostFragment(), DETAIL_POST_FRAGMENT)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
