package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.adapter.GridPostFragment;
import android.example.com.practiceapp.adapter.GridPostViewHolder;
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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    public static final String UNFOLLOW = "UNFOLLOW";
    public static final String FOLLOW = "FOLLOW";
    public static final String EDIT_PROFILE = "EDIT_PROFILE";

    private Context context;
    private boolean userVisibleHint = true;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference mCountersRef;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private final Post mPost = new Post();

    private ListenerRegistration counters;

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

    private void initListeners() {
        counters = mCountersRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen:error", e);
                    return;
                } if (snapshot.contains("posts")) {
                    mPostCount.setText(snapshot.get("posts").toString());
                } if (snapshot.contains("followers")) {
                    mFollowersCount.setText(snapshot.get("followers").toString());
                } if (snapshot.contains("follows")) {
                    mFollowsCount.setText(snapshot.get("follows").toString());
                }
            }
        });
    }

    private void stopListener() {
        // Stop listening to changes
        Log.i(TAG, "stopListener()");
        counters.remove();
        mTabLayout.clearOnTabSelectedListeners();
    }

    private void setUpHeader() {
        Uri uri = Uri.parse(mPost.getUser().getPhotoUrl());
        GlideApp.with(context)
                .load(uri)
                .circleCrop()
                .into(mProfilePic);
        mProfileName.setText(mPost.getUser().getUsername());
        mProfileEmail.setText(mPost.getUser().getEmail());
    }

    private void subscribeToModel() {
        final UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSelected().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    mPost.setUser(user);
                    String countersPath = "/counters/" + mPost.getUser().getEmail();
                    mCountersRef = db.document(countersPath);
                    setUpHeader();
                    initListeners();
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
                        mProfileButton.setText(getString(R.string.user_fragment_edit_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("EditProfile");
                                // TODO(1) Create method editProfile
                                // Inside method 2-2
                                // TODO(2) Go to EditProfile Fragment
                            }
                        };
                    } else if (s.equals(FOLLOW)) {
                        mProfileButton.setText(getString(R.string.user_fragment_follow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Follow");
                                // TODO(3) Create method followUser()
                                // Inside method 4-7
                                // TODO(4) Add the reference in Firestore Database
                                // TODO(5) Increment fieldValue of countFollows
                                // TODO(6) Increment fieldValue of countFollowers in the userSelected
                                // TODO(7) Send notification to the userSelected
                            }
                        };
                    } else if (s.equals(UNFOLLOW)) {
                        mProfileButton.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Unfollow");
                                // TODO(8) Create method unfollowUser()
                                // Inside method 9-11
                                // TODO(9) Remove the reference in Firestore Database
                                // TODO(10) Decrement fieldValue of countFollows
                                // TODO(11) Decrement fieldValue of countFollowers in the userSelected
                            }
                        };
                    }
                    mProfileButton.setOnClickListener(action);
                }
            }
        });
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
