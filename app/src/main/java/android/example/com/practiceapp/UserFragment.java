package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
                                editProfile();
                            }
                        };
                    } else if (s.equals(FOLLOW)) {
                        mProfileButton.setText(getString(R.string.user_fragment_follow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Follow");
                                followUser(model.getUserSigned().getValue().getEmail());
                            }
                        };
                    } else if (s.equals(UNFOLLOW)) {
                        mProfileButton.setText(getString(R.string.user_fragment_unfollow_profile));
                        action = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showToast("Unfollow");
                                unfollowUser(model.getUserSigned().getValue().getEmail());
                            }
                        };
                    }
                    mProfileButton.setOnClickListener(action);
                }
            }
        });
    }

    private void followUser(String emailSigned) {
        Map<String, Object> data = new HashMap<>();
        DocumentReference followingRef = db.collection("following/" + emailSigned + "/userFollowing").document(mPost.getUser().getEmail());
        DocumentReference followersRef = db.collection("followers/" + mPost.getUser().getEmail() + "/userFollowing").document(emailSigned);
        DocumentReference countFollowsRef = db.collection("counters").document(emailSigned);
        DocumentReference countFollowersRef = db.collection("counters").document(mPost.getUser().getEmail());
        // Get a new write batch
        WriteBatch batch = db.batch();
        batch.set(followingRef, data);
        batch.set(followersRef, data);
        batch.update(countFollowsRef, "follows", FieldValue.increment(1));
        batch.update(countFollowersRef, "followers", FieldValue.increment(1));
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "followUser success");
                } else {
                    Log.w(TAG, "followUser failed with: ", task.getException());
                }
            }
        });

        // TODO(1) Send notification to the userSelected

    }

    private void unfollowUser(String emailSigned) {
        Map<String, Object> data = new HashMap<>();
        DocumentReference followingRef = db.collection("following/" + emailSigned + "/userFollowing").document(mPost.getUser().getEmail());
        DocumentReference followersRef = db.collection("followers/" + mPost.getUser().getEmail() + "/userFollowers").document(emailSigned);
        DocumentReference countFollowsRef = db.collection("counters").document(emailSigned);
        DocumentReference countFollowersRef = db.collection("counters").document(mPost.getUser().getEmail());
        // Get a new write batch
        WriteBatch batch = db.batch();
        batch.delete(followingRef);
        batch.delete(followersRef);
        batch.update(countFollowsRef, "follows", FieldValue.increment(-1));
        batch.update(countFollowersRef, "followers", FieldValue.increment(-1));
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "unfollowUser success");
                } else {
                    Log.w(TAG, "unfollowUser failed with: ", task.getException());
                }
            }
        });

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
