package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.SharedPreferences;
import android.example.com.practiceapp.adapter.ProfileImagesViewHolder;
import android.example.com.practiceapp.models.Photo;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class UserFragment extends Fragment {
    public static final String TAG = UserFragment.class.getSimpleName();
    public static final int SPAN_COUNT = 3;
    public static final int PREFETCH_DISTANCE = 2;
    public static final int PAGE_SIZE = 6;

    private Context mContext;
    private boolean userVisibleHint = true;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItemsCollection;
    private DocumentReference mCountersRef;
    private ProgressBar mProgressBar;
    private final Post mPost = new Post();

    private ListenerRegistration counters;

    ImageView mProfilePic;
    TextView mProfileName;
    TextView mProfileEmail;
    TextView mPostCount;
    TextView mFollowersCount;
    TextView mFollowsCount;

    Button mEditProfileButton;

    public UserFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Get the application context
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        mFirestore = FirebaseFirestore.getInstance();
        UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSelected().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    Log.d(TAG, "onChanged: the user isn't null");
                    mPost.setUser(user);
                    String countersPath = "/counters/" + mPost.getUser().getEmail();
                    mCountersRef = mFirestore.document(countersPath);
                    String postsPath = "/posts/" + mPost.getUser().getEmail() + "/userPosts";
                    mItemsCollection = mFirestore.collection(postsPath);
                    setUpHeader();
                    setUpAdapter();
                    initListeners();
                } else {
                    Log.d(TAG, "onChanged: El usuario es nulo");
                }
            }
        });
        userVisibleHint = true;
        // TODO cambiar boton segun sea tu perfil u otro
        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO go to settings
                showToast("TODO");
            }
        });
        /*
         * GridLayoutManager
         *  A RecyclerView.LayoutManager implementations that lays out items in a grid.
         *  By default, each item occupies 1 span. You can change it by providing a custom
         *  GridLayoutManager.SpanSizeLookup instance via setSpanSizeLookup(SpanSizeLookup).
         *
         * public GridLayoutManager (Context context, int spanCount)
         *  Creates a vertical GridLayoutManager
         *
         *  Parameters
         *      context: Current context, will be used to access resources.
         *      spanCount: The number of columns in the grid
         *
         */
        // Define a layout for RecyclerView
        mLayoutManager = new GridLayoutManager(mContext, SPAN_COUNT);


    }

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
    }

    private void setUpHeader() {
        Uri uri = Uri.parse(mPost.getUser().getPhotoUrl());
        GlideApp.with(mContext)
                .load(uri)
                .circleCrop()
                .into(mProfilePic);
        mProfileName.setText(mPost.getUser().getUsername());
        mProfileEmail.setText(mPost.getUser().getEmail());
    }
    private void setUpAdapter() {
        Query baseQuery = mItemsCollection.orderBy(Photo.FIELD_DATE, Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .setPageSize(PAGE_SIZE)
                .build();
        SnapshotParser<Post> parser = new SnapshotParser<Post>() {
            @NonNull
            @Override
            public Post parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Post post = new Post();
                post.setUser(mPost.getUser());
                if (snapshot.exists()) {
                    Photo photo = snapshot.toObject(Photo.class);
                    post.setPhoto(photo);
                }
                return post;
            }
        };

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, parser)
                .build();

        FirestorePagingAdapter<Post, ProfileImagesViewHolder> adapter =
                new FirestorePagingAdapter<Post, ProfileImagesViewHolder>(options) {
                    @NonNull
                    @Override
                    public ProfileImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        // Create a new View
                        View v = LayoutInflater.from(mContext).inflate(R.layout.profile_item, viewGroup, false);
                        return new ProfileImagesViewHolder(v);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull ProfileImagesViewHolder holder, int position, @NonNull Post model) {
                        holder.bind(model);
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                        switch (state) {
                            case LOADING_INITIAL:
                            case LOADING_MORE:
                                mProgressBar.setVisibility(View.VISIBLE);
                                break;
                            case LOADED:
                                mProgressBar.setVisibility(View.GONE);
                                break;
                            case FINISHED:
                                mProgressBar.setVisibility(View.GONE);
                                showToast("Reached end of data set.");
                                break;
                            case ERROR:
                                showToast("An error ocurred");
                                retry();
                                break;
                        }
                    }
                };

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
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

    private void bindView() {
        // Get the widgets reference from XML layout
        mRecyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recycler_view);
        mProgressBar = getView().findViewById(R.id.paging_loading);
        mProfilePic = getView().findViewById(R.id.iv_profile_picture);
        mProfileName = getView().findViewById(R.id.tv_profile_name);
        mProfileEmail = getView().findViewById(R.id.tv_profile_email);
        mPostCount = getView().findViewById(R.id.tv_post_count);
        mFollowersCount = getView().findViewById(R.id.tv_followers_count);
        mFollowsCount = getView().findViewById(R.id.tv_follows_count);
        mEditProfileButton = getView().findViewById(R.id.bt_edit_profile);
    }

    private void initUser() {
        SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String email = prefs.getString(getString(R.string.account_email_key), "");
        String username = prefs.getString(getString(R.string.account_name_key), "");
        String photoUri = prefs.getString(getString(R.string.account_photo_key), "");
        User mUser = new User();
        mUser.setEmail(email);
        mUser.setUsername(username);
        mUser.setPhotoUrl(photoUri);
        mPost.setUser(mUser);
    }
    private void showToast(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
