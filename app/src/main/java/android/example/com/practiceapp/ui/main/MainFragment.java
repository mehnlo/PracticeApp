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
    private Boolean userVisibleHint = true;
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
    public void onResume() {
        super.onResume();
        //Highlight the selected item has been done by NavigationView
        ((MainActivity)getActivity()).setNavItemChecked(0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userVisibleHint = true;
        subscribeToModel();
    }

    private void subscribeToModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(context);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(MainViewModel.class);
        viewModel.getFeed().observe(this, postEntries -> {
            if (!postEntries.isEmpty()) {
                Log.d(TAG, "Fetch feed successfully");
            }
        });
    }

    @Override
    public void onPause() {
        userVisibleHint = false;
        super.onPause();
    }

    @Override
    public boolean getUserVisibleHint() {
        return userVisibleHint;
    }
}
