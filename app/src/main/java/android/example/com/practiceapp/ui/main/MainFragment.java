package android.example.com.practiceapp.ui.main;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();
    private Context context;
    private MainViewModel viewModel;

    public MainFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            subscribeToModel();
        }
    }

    private void subscribeToModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(context);
        viewModel = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        viewModel.getFeed().observe(requireActivity(), postEntries -> {
            if (!postEntries.isEmpty()) {
                Log.d(TAG, "Fetch feed successfully");
            }
        });
    }


}
