package android.example.com.practiceapp.ui.main.user;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.User;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;


public abstract class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private Context mContext;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private TextView mPostCount;
    private TextView mFollowersCount;
    private TextView mFollowsCount;
    private Button mProfileButton;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
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
        mTabLayout.clearOnTabSelectedListeners();
    }

    private void subscribeToModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(mContext);
        MainViewModel model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        model.getUserSelected().observe(requireActivity(), user -> {
            if (user != null) {
                setUpHeader(user);
            } else {
                Log.d(TAG, "onChanged: El usuario es nulo");
            }
        });
        model.getActionButton().observe(this, s -> {
            if (s != null) {
                View.OnClickListener action = null;
                switch (s) {
                    case EDIT_PROFILE:
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.button));
                        mProfileButton.setText(getString(R.string.user_fragment_edit_profile));
                        action = this::editProfile;
                        break;
                    case FOLLOW:
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.button));
                        mProfileButton.setText(getString(R.string.user_fragment_follow_profile));
                        action = view -> followUser(model);
                        break;
                    case UNFOLLOW:
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.btn_gradient));
                        mProfileButton.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = view -> unfollowUser(model);
                        break;
                }
                mProfileButton.setOnClickListener(action);
            }
        });
        model.getPostCount().observe(this, s -> {
            if (s == null) mPostCount.setText(String.valueOf(0));
            mPostCount.setText(s);
        });
        model.getFollowersCount().observe(this, s -> {
            if (s == null) mFollowersCount.setText(String.valueOf(0));
            mFollowersCount.setText(s);
        });
        model.getFollowsCount().observe(this, s -> {
            if (s == null) mFollowsCount.setText(String.valueOf(0));
            mFollowsCount.setText(s);
        });
    }

    private void setUpHeader(User user) {
        if (!TextUtils.isEmpty(user.getPhotoUrl())) {
            Uri uri = Uri.parse(user.getPhotoUrl());
            Glide.with(mContext)
                    .load(uri)
                    .circleCrop()
                    .into(mProfilePic);
        }
        mProfileName.setText(!TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : "");
        mProfileEmail.setText(!TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "");
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

    private void bindView() {
        // Get the widgets reference from XML layout
        assert getView() != null;
        mProfilePic = getView().findViewById(R.id.iv_profile_picture);
        mProfileName = getView().findViewById(R.id.tv_profile_name);
        mProfileEmail = getView().findViewById(R.id.tv_profile_email);
        mPostCount = getView().findViewById(R.id.tv_post_count);
        mFollowersCount = getView().findViewById(R.id.tv_followers_count);
        mFollowsCount = getView().findViewById(R.id.tv_follows_count);
        mProfileButton = getView().findViewById(R.id.bt_profile);
        mViewPager = getView().findViewById(R.id.container);
        mTabLayout = getView().findViewById(R.id.tabLayout);
    }

    private void setupTabLayout() {
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private void showToast(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
