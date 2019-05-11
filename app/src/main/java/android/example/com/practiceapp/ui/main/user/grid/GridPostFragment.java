package android.example.com.practiceapp.ui.main.user.grid;


import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.content.Context;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.data.models.User;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

public class GridPostFragment extends Fragment {

    public static final String TAG = GridPostFragment.class.getSimpleName();
    private static final int SPAN_COUNT = 3;
    private static final int PREFETCH_DISTANCE = 2;
    private static final int PAGE_SIZE = 6;

    private Context mContext;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MainViewModel model;


    public GridPostFragment() { }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
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
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(mContext);
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
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

        GridPostAdapter adapter = new GridPostAdapter(options, mContext, mProgressBar);
        adapter.setViewModel(model);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, SPAN_COUNT));
        mRecyclerView.setAdapter(adapter);
    }

    private void bindView() {
        assert getView() != null;
        mProgressBar = getView().findViewById(R.id.paging_loading);
        mRecyclerView = getView().findViewById(R.id.recycler_view);
    }

}