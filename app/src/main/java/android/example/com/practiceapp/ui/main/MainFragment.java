package android.example.com.practiceapp.ui.main;

import androidx.lifecycle.ViewModelProviders;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment {

    public MainFragment() {}

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        MainViewModel viewModel = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        FeedAdapter adapter = new FeedAdapter();
        viewModel.getFeed().observe(requireActivity(), adapter::submitList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }
}
