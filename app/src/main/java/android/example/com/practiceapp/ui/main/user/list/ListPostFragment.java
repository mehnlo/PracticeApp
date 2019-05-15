package android.example.com.practiceapp.ui.main.user.list;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.database.UserEntry;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.databinding.FragmentContentListUserBinding;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

public class ListPostFragment extends Fragment {
    private static final String TAG = ListPostFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 4;
    private static final int PREFETCH_DISTANCE = 2;
    private FragmentContentListUserBinding binding;
    private MainViewModel model;

    public ListPostFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content_list_user, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeToModel();
    }

    private void subscribeToModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        model.getUserSelected().observe(this, user -> {
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
                .setLifecycleOwner(this)
                .setQuery(model.getBaseQuery(), config, parser)
                .build();

        ListPostAdapter adapter = new ListPostAdapter(options, requireContext(), binding.pagingLoading);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

}
