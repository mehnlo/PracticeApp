package android.example.com.practiceapp.ui.main.user.grid;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.example.com.practiceapp.data.database.UserEntry;
import android.example.com.practiceapp.databinding.FragmentContentGridUserBinding;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

public class GridPostFragment extends Fragment {
    public static final String TAG = GridPostFragment.class.getSimpleName();
    private static final int SPAN_COUNT = 3;
    private static final int PREFETCH_DISTANCE = 2;
    private static final int PAGE_SIZE = 6;
    private MainViewModel model;
    private FragmentContentGridUserBinding binding;

    public GridPostFragment() { }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content_grid_user, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribeToModel();
    }

    private void subscribeToModel() {
        model.getUserSelected().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                setUpAdapter(user);
            } else {
                Log.d(TAG, "onChanged: The user is null");
            }
        });
    }

    private void setUpAdapter(UserEntry user) {
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
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(model.getBaseQuery(), config, parser)
                .build();

        GridPostAdapter adapter = new GridPostAdapter(options, requireContext(), binding.pagingLoading);
        adapter.setViewModel(model);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), SPAN_COUNT));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);
    }


}
