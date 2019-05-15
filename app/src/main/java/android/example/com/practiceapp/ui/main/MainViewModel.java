package android.example.com.practiceapp.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import androidx.work.WorkInfo;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.database.PostEntry;
import android.example.com.practiceapp.data.database.UserEntry;
import android.example.com.practiceapp.data.models.Post;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.google.firebase.firestore.Query;

/**
 * ViewModel for {@link MainActivity}
 */
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private boolean mIsSigningIn;
    private LiveData<UserEntry> userSigned;
    private MutableLiveData<UserEntry> userSelected;
    private MutableLiveData<String> buttonText;
    private MutableLiveData<String> postCount;
    private MutableLiveData<String> followersCount;
    private MutableLiveData<String> followsCount;
    private MutableLiveData<Post> postSelected;
    private PracticeAppRepository mRepo;
    private LiveData<WorkInfo> mStatus;

    public MainViewModel(PracticeAppRepository repo) {
        this.mRepo = repo;
        this.userSigned = new MutableLiveData<>();
        this.userSelected = new MutableLiveData<>();
        this.postSelected = new MutableLiveData<>();
        mStatus = new MutableLiveData<>();
        mIsSigningIn = false;
    }


    public boolean isIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    /**
     *
      * @return
     */
    public LiveData<UserEntry> getUserSigned() { return userSigned; }

    /**
     *
     * @param user
     */
    public void select(UserEntry user) {
        userSelected.setValue(user);
        if (user != null){
            loadCounters();
            loadFollows();
        }
    }

    /**
     *
     * @return
     */
    public MutableLiveData<UserEntry> getUserSelected() { return userSelected; }

    /**
     *
     * @return
     */
    private boolean isMyProfile() {
        Log.d(TAG, "isMyProfile: comparing '" + userSelected.getValue().getEmail() + "' equals '" + userSigned.getValue().getEmail() + "'.");
        return (userSelected.getValue().getEmail().equals(userSigned.getValue().getEmail()));
    }

    /**
     *
     */
    private void loadFollows() {
        if (isMyProfile()){
            getActionButton().postValue(EDIT_PROFILE);
            return;
        }
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        mRepo.getFollows(email, emailSelected).observeForever(text -> {
            if(!TextUtils.isEmpty(text)) {
                buttonText.postValue(text);
            }
        });
    }

    /**
     *
     * @return
     */
    public MutableLiveData<String> getActionButton() {
        if (buttonText == null) buttonText = new MutableLiveData<>();
        return buttonText;
    }

    /**
     *
     */
    private void loadCounters() {
        String email = userSelected.getValue().getEmail();
        mRepo.getCounters(email).observeForever(counterMap -> {
            if (!counterMap.isEmpty()) {
                postCount.postValue(counterMap.get("posts"));
                followersCount.postValue(counterMap.get("followers"));
                followsCount.postValue(counterMap.get("follows"));
            }
        });

    }

    /**
     *
     * @return
     */
    public MutableLiveData<String> getPostCount() {
        if (postCount == null) postCount = new MutableLiveData<>();
        return postCount;
    }

    /**
     *
     * @return
     */
    public MutableLiveData<String> getFollowersCount() {
        if (followersCount == null) followersCount = new MutableLiveData<>();
        return followersCount;
    }

    /**
     *
     * @return
     */
    public MutableLiveData<String> getFollowsCount() {
        if (followsCount == null) followsCount = new MutableLiveData<>();
        return  followsCount; }

    /**
     *
     */
    public void followUser(){
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        Log.d(TAG, "'" + email + "' started to follow '" + emailSelected + "'.");
        mRepo.follow(email, emailSelected);
        // TODO (1) Send notification to the userSelected
    }

    /**
     *
     */
    public void unfollowUser(){
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        Log.d(TAG, "'" + email + "' stopped following '" + emailSelected + "'.");
        mRepo.unfollow(email, emailSelected);
    }

    /**
     * Get the user saved from FirebaseFirestore
     * @param email The email user that will be loaded
     */
    public void initUser(String email) {
        Log.d(TAG, "initUser()");
        userSigned = mRepo.get(email);
        select(userSigned.getValue());
    }

    /**
     * Create the user on FirebaseFirestore
     * @param user The user that will be saved
     */
    public void createUser(UserEntry user) {
        Log.d(TAG, "createUser()");
        userSigned = mRepo.create(user);
        select(userSigned.getValue());

    }

    /**
     * Update the user on the Repository
     */
    public void saveUser() {
        mRepo.update(userSigned.getValue());
    }

    /**
     *
     * @param mPhotoUri
     */
    public void uploadProfilePic(Uri mPhotoUri) {
        mRepo.uploadProfilePic(userSigned.getValue().getEmail(), mPhotoUri);
    }

    /**
     *
     */
    public void signOut() {
        userSigned = null;
        userSelected = null;
    }

    public Query getBaseQuery() { return mRepo.getBaseQuery(userSelected.getValue().getEmail()); }

    public void select(Post post) { postSelected.setValue(post); }

    public MutableLiveData<Post> getPostSelected() { return postSelected; }

    public LiveData<PagedList<PostEntry>> getFeed() { return mRepo.getCurrentFeed(); }

    public LiveData<WorkInfo> getStatus() { return mStatus; }
}
