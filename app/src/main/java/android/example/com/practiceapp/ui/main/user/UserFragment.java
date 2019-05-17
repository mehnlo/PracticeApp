package android.example.com.practiceapp.ui.main.user;

import androidx.annotation.StringRes;
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
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private FragmentUserBinding binding;
    private MainViewModel model;

    public UserFragment() { }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewmodel(model);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribeToModel();
        setupTabLayout();
    }

    @Override public void onPause() {
        stopListener();
        super.onPause();
    }

    private void stopListener() { binding.tabLayout.clearOnTabSelectedListeners(); }

    private void subscribeToModel() {
        model.getActionButton().observe(getViewLifecycleOwner(), s -> {
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
                        action = view -> followUser();
                        break;
                    case UNFOLLOW:
                        binding.headerUser.btProfile.setBackground(getResources().getDrawable(R.drawable.btn_gradient));
                        binding.headerUser.btProfile.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = view -> unFollowUser();
                        break;
                }
                binding.headerUser.btProfile.setOnClickListener(action);
            }
        });
    }

    private void editProfile(View view) {
        showToast(R.string.edit_profile_fragment_title);
        Navigation.findNavController(view).navigate(R.id.action_profile_to_editProfile);
    }

    private void followUser(){
        showToast(R.string.user_fragment_follow_profile);
        model.followUser();
    }

    private void unFollowUser(){
        showToast(R.string.user_fragment_unfollow_profile);
        model.unfollowUser();
    }

    private void setupTabLayout() {
        binding.container.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        binding.container.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.container));
    }

    private void showToast(@StringRes int message){ Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

}
