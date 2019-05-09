package android.example.com.practiceapp.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctionsException;

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
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(context);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(MainViewModel.class);

        loadFeed();
//        .addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//               Exception e = task.getException();
//               if (e instanceof FirebaseFunctionsException) {
//                   FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                   FirebaseFunctionsException.Code code = ffe.getCode();
//                   Object details = ffe.getDetails();
//               }
//            } else {
//                Log.d(TAG, "onComplete: loadFeed completed successfully");
//            }
//        });
    }
    private void loadFeed() {
        Log.d(TAG, "loadFeed()");
        // Create the arguments to the callable function.
        Toast.makeText(context, "loadFeed()", Toast.LENGTH_SHORT).show();
        viewModel.loadFeed();
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
