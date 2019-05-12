package android.example.com.practiceapp.ui.main.user;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.databinding.FragmentUserBinding;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;



public abstract class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private FragmentUserBinding binding;
    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        View view = binding.getRoot();
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        binding.headerUser.setViewmodel(model);
        binding.headerUser.setLifecycleOwner(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeToModel();
        setupTabLayout();
    }

    @Override
    public void onPause() {
        stopListener();
        super.onPause();
    }

    private void stopListener() {
        // Stop listening to changes
        Log.i(TAG, "stopListener()");
        binding.tabLayout.clearOnTabSelectedListeners();
    }

    private void subscribeToModel() {
        model.getActionButton().observe(this, s -> {
            if (s != null) {
                View.OnClickListener action = null;
                switch (s) {
                    case EDIT_PROFILE:
                        binding.headerUser.btProfile.setBackground(getResources().getDrawable(R.drawable.button));
                        binding.headerUser.btProfile.setText(getString(R.string.user_fragment_edit_profile));
                        action = this::editProfile;
                        break;
                    case FOLLOW:
                        binding.headerUser.btProfile.setBackground(getResources().getDrawable(R.drawable.button));
                        binding.headerUser.btProfile.setText(getString(R.string.user_fragment_follow_profile));
                        action = view -> followUser(model);
                        break;
                    case UNFOLLOW:
                        binding.headerUser.btProfile.setBackground(getResources().getDrawable(R.drawable.btn_gradient));
                        binding.headerUser.btProfile.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = view -> unfollowUser(model);
                        break;
                }
                binding.headerUser.btProfile.setOnClickListener(action);
            }
        });
    }

    protected void editProfile(View view) { showToast("EditProfile"); }
    private void followUser(MainViewModel model){
        showToast("Follow");
        model.followUser();
    }
    private void unfollowUser(MainViewModel model){
        showToast("Unfollow");
        model.unfollowUser();
    }

    private void setupTabLayout() {
        binding.container.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        binding.container.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.container));
    }

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

}
