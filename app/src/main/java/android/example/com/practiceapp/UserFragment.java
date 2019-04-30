package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.example.com.practiceapp.adapter.SectionsPagerAdapter;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.utilities.GlideApp;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;


public class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    public static final String UNFOLLOW = "UNFOLLOW";
    public static final String FOLLOW = "FOLLOW";
    public static final String EDIT_PROFILE = "EDIT_PROFILE";
    public static final String EDIT_PROFILE_FRAGMENT = "EDIT_PROFILE_FRAGMENT";

    private Context context;
    private boolean userVisibleHint = true;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private TextView mPostCount;
    private TextView mFollowersCount;
    private TextView mFollowsCount;
    private Button mProfileButton;

    public UserFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        userVisibleHint = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Highlight the selected item has been done by NavigationView
        ((MainActivity)getActivity()).setNavItemChecked(1);
    }

    @Override
    public void onPause() {
        userVisibleHint = false;
        stopListener();
        super.onPause();
    }

    @Override
    public boolean getUserVisibleHint() { return userVisibleHint; }

    private void stopListener() {
        // Stop listening to changes
        Log.i(TAG, "stopListener()");
        mTabLayout.clearOnTabSelectedListeners();
    }

    private void subscribeToModel() {
        final UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSelected().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    setUpHeader(user);
                } else {
                    Log.d(TAG, "onChanged: El usuario es nulo");
                }
            }
        });
        model.getActionButton().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null) {
                    View.OnClickListener action = null;
                    if (s.equals(EDIT_PROFILE)) {
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.button));
                        mProfileButton.setText(getString(R.string.user_fragment_edit_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("EditProfile");
                                editProfile();
                            }
                        };
                    } else if (s.equals(FOLLOW)) {
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.button));
                        mProfileButton.setText(getString(R.string.user_fragment_follow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Follow");
                                model.followUser();
                            }
                        };
                    } else if (s.equals(UNFOLLOW)) {
                        mProfileButton.setBackground(getResources().getDrawable(R.drawable.btn_gradient));

                        mProfileButton.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Unfollow");
                                model.unollowUser();
                            }
                        };
                    }
                    mProfileButton.setOnClickListener(action);
                }
            }
        });
        model.getPostCount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) mPostCount.setText(String.valueOf(0));
                mPostCount.setText(s);
            }
        });
        model.getFollowersCount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) mFollowersCount.setText(String.valueOf(0));
                mFollowersCount.setText(s);
            }
        });
        model.getFollowsCount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) mFollowsCount.setText(String.valueOf(0));
                mFollowsCount.setText(s);
            }
        });
    }

    private void setUpHeader(User user) {
        Uri uri = Uri.parse(user.getPhotoUrl());
        GlideApp.with(context)
                .load(uri)
                .circleCrop()
                .into(mProfilePic);
        mProfileName.setText(user.getUsername());
        mProfileEmail.setText(user.getEmail());
    }

    private void editProfile() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new EditProfileFragment(), EDIT_PROFILE_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(EDIT_PROFILE_FRAGMENT)
                .commit();
    }

    private void bindView() {
        // Get the widgets reference from XML layout
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

    private void showToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
