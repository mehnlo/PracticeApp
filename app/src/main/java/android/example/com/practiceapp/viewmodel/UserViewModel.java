package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.repository.UserRepository;
import android.net.Uri;
import android.util.Log;
import java.util.Map;

import javax.inject.Inject;

/**
 * ViewModel for {@link android.example.com.practiceapp.MainActivity}
 */
public class UserViewModel extends ViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();
    private static final String EDIT_PROFILE = "EDIT_PROFILE";
    private boolean mIsSigningIn = false;
    private MutableLiveData<User> userSigned;
    private MutableLiveData<User> userSelected;
    private MutableLiveData<String> buttonText;
    private MutableLiveData<String> postCount;
    private MutableLiveData<String> followersCount;
    private MutableLiveData<String> followsCount;
    private UserRepository userRepo;

    // Instructs Dagger 2 to provide the UserRepository parameter
    @Inject
    public UserViewModel(UserRepository userRepo) {
        this.userRepo = userRepo;
    }


    public boolean isIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public void setUserSigned(User user) {
        getUserSigned().setValue(user);
    }

    public MutableLiveData<User> getUserSigned() {
        if (userSigned == null) userSigned = new MutableLiveData<>();
        return userSigned;
    }

    public void select(User user) {
        getUserSelected().setValue(user);
        if (user != null){
            loadCounters();
            loadFollows();
        }
    }

    public MutableLiveData<User> getUserSelected() {
        if (userSelected == null) userSelected = new MutableLiveData<>();
        return userSelected;
    }

    private boolean isMyProfile() { return (userSelected.getValue().getEmail().equals(userSigned.getValue().getEmail())); }

    private void loadFollows() {
        if (isMyProfile()){
            getActionButton().setValue(EDIT_PROFILE);
            return;
        }
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        buttonText = userRepo.loadFollows(email, emailSelected);
    }

    public MutableLiveData<String> getActionButton() {
        if (buttonText == null) buttonText = new MutableLiveData<>();
        return buttonText;
    }

    private void loadCounters() {
        String email = userSelected.getValue().getEmail();
        Map<String, MutableLiveData<String>> counterMap = userRepo.loadCounters(email);
        postCount = counterMap.get("posts");
        followersCount = counterMap.get("followers");
        followsCount = counterMap.get("follows");
    }

    public MutableLiveData<String> getPostCount() {
        if (postCount == null) postCount = new MutableLiveData<>();
        return postCount;
    }

    public MutableLiveData<String> getFollowersCount() {
        if (followersCount == null) followersCount = new MutableLiveData<>();
        return followersCount;
    }

    public MutableLiveData<String> getFollowsCount() {
        if (followsCount == null) followsCount = new MutableLiveData<>();
        return  followsCount; }

    public void followUser(){
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        userRepo.follow(email, emailSelected);
        // TODO (1) Send notification to the userSelected
    }

    public void unfollowUser(){
        String email = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        userRepo.unfollow(email, emailSelected);
    }

    /**
     * Get the user saved from FirebaseFirestore
     * @param email The email user that will be loaded
     */
    public void initUser(String email) {
        Log.d(TAG, "initUser()");
        userSelected = userSigned = userRepo.get(email);
    }

    /**
     * Create the user on FirebaseFirestore
     * @param user The user that will be saved
     */
    public void createUser(User user) {
        Log.d(TAG, "createUser()");
        userSelected = userSigned = userRepo.create(user);

    }

    /**
     * Update the user on the Repository
     */
    public void saveUser() {
        userRepo.update(userSigned.getValue());
    }
    public void uploadProfilePic(Uri mPhotoUri) {
        userRepo.uploadProfilePic(userSigned.getValue().getEmail(), mPhotoUri);
    }

}
