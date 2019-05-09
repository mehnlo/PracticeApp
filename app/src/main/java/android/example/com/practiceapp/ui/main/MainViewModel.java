package android.example.com.practiceapp.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.data.models.User;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;

/**
 * ViewModel for {@link MainActivity}
 */
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private boolean mIsSigningIn;
    private MutableLiveData<User> userSigned;
    private MutableLiveData<User> userSelected;
    private MutableLiveData<String> buttonText;
    private MutableLiveData<String> postCount;
    private MutableLiveData<String> followersCount;
    private MutableLiveData<String> followsCount;
    private MutableLiveData<Post> postSelected;
    private PracticeAppRepository userRepo;

    public MainViewModel(PracticeAppRepository userRepo) {
        this.userRepo = userRepo;
        this.userSigned = new MutableLiveData<>();
        this.userSelected = new MutableLiveData<>();
        this.postSelected = new MutableLiveData<>();
        this.mIsSigningIn = false;
    }


    public boolean isIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    /**
     *
     * @param user
     */
    private void setUserSigned(User user) {
        Log.d(TAG, "setUserSigned to '" + user.getEmail() + "'.");
        userSigned.setValue(user);
    }

    /**
     *
      * @return
     */
    public MutableLiveData<User> getUserSigned() { return userSigned; }

    /**
     *
     * @param user
     */
    public void select(User user) {
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
    public MutableLiveData<User> getUserSelected() { return userSelected; }

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
        userRepo.loadFollows(email, emailSelected).observeForever(text -> {
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
        userRepo.loadCounters(email).observeForever(counterMap -> {
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
        userRepo.follow(email, emailSelected);
        // TODO (1) Send notification to the userSelected
    }

    /**
     *
     */
    public void unfollowUser(){
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        Log.d(TAG, "'" + email + "' stopped following '" + emailSelected + "'.");
        userRepo.unfollow(email, emailSelected);
    }

    /**
     * Get the user saved from FirebaseFirestore
     * @param email The email user that will be loaded
     */
    public void initUser(String email) {
        Log.d(TAG, "initUser()");
        userSigned = userRepo.get(email);
        select(userSigned.getValue());
    }

    /**
     * Create the user on FirebaseFirestore
     * @param user The user that will be saved
     */
    public void createUser(User user) {
        Log.d(TAG, "createUser()");
        userSigned = userRepo.create(user);
        select(userSigned.getValue());

    }

    /**
     * Update the user on the Repository
     */
    public void saveUser() {
        userRepo.update(userSigned.getValue());
    }

    /**
     *
     * @param mPhotoUri
     */
    public void uploadProfilePic(Uri mPhotoUri) {
        userRepo.uploadProfilePic(userSigned.getValue().getEmail(), mPhotoUri);
    }

    /**
     *
     */
    public void signOut() {
        userSigned = null;
        userSelected = null;
    }

    /**
     *
     * @return
     */
    public void loadFeed() {
        userRepo.loadFeed();
    }

    public Query getBaseQuery() {
        return userRepo.getBaseQuery(userSelected.getValue().getEmail());
    }

    public void select(Post post) { postSelected.setValue(post); }

    public MutableLiveData<Post> getPostSelected() { return postSelected; }

}
