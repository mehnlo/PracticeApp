package android.example.com.pseudogram.ui.main.user.list;

import android.example.com.pseudogram.R;
import android.example.com.pseudogram.data.database.PostEntry;
import android.example.com.pseudogram.data.database.UserEntry;
import android.example.com.pseudogram.databinding.FragmentContentListUserBinding;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.example.com.pseudogram.ui.main.MainViewModelFactory;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

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
        binding.setLifecycleOwner(getViewLifecycleOwner());
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

        SnapshotParser<PostEntry> parser = snapshot -> {
            PostEntry post = new PostEntry();
            if (snapshot.exists()) {
                post = new PostEntry(snapshot.getId(),
                        user.getEmail(),
                        user.getPhotoUrl(),
                        snapshot.getString(PostEntry.FIELD_TITLE),
                        snapshot.getString(PostEntry.FIELD_PHOTO_URL),
                        snapshot.getDate(PostEntry.FIELD_DATE));
            }
            return post;
        };

        FirestorePagingOptions<PostEntry> options = new FirestorePagingOptions.Builder<PostEntry>()
                .setLifecycleOwner(this)
                .setQuery(model.getBaseQuery(), config, parser)
                .build();

        ListPostAdapter adapter = new ListPostAdapter(options, requireContext(), binding.pagingLoading);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

}
