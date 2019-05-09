package android.example.com.practiceapp.ui.main.profile.grid;


import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.ui.main.detail.DetailPostFragment;
import android.example.com.practiceapp.ui.post.PostActivity;
import android.example.com.practiceapp.ui.post.PostViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.example.com.practiceapp.utilities.OnPostSelectedListener;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.data.models.User;
import android.example.com.practiceapp.ui.post.PostViewModel;
import android.example.com.practiceapp.ui.main.MainViewModel;
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
    private PostViewModel postViewModel;
    private MainViewModel model;


    public GridPostFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_grid_user, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        subscribeToModel();

    }

    private void subscribeToModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(context);
        model = ViewModelProviders.of(getActivity(), factory).get(MainViewModel.class);
        model.getUserSelected().observe(this, user -> {
            if (user != null) {
                setUpAdapter(user);
            } else {
                Log.d(TAG, "onChanged: The user is null");
            }
        });
    }

    private void setUpAdapter(User user) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .setPageSize(PAGE_SIZE)
                .build();

        SnapshotParser<Post> parser = snapshot -> {
            Post post = new Post();
            post.setUser(user);
            if (snapshot.exists()) {
                Photo photo = snapshot.toObject(Photo.class);
                post.setPhoto(photo);
            }
            return post;
        };

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(model.getBaseQuery(), config, parser)
                .build();

        GridPostAdapter adapter = new GridPostAdapter(options, context, mProgressBar);
        adapter.setCallback(GridPostFragment.this);
        adapter.setViewModel(model);
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
