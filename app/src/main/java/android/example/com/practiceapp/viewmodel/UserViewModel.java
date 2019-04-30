package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.models.User;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * ViewModel for {@link android.example.com.practiceapp.MainActivity}
 */
public class UserViewModel extends ViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String EDIT_PROFILE = "EDIT_PROFILE";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean mIsSigningIn = false;
    private MutableLiveData<User> userSigned;
    private MutableLiveData<User> userSelected;
    private MutableLiveData<String> buttonText;
    private MutableLiveData<String> postCount;
    private MutableLiveData<String> followersCount;
    private MutableLiveData<String> followsCount;
    private ListenerRegistration eventListener;
    private ListenerRegistration countersListener;

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
            Log.d(TAG, "UserSelected");
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

        eventListener = db.collection("following/" + userSigned.getValue().getEmail() + "/userFollowing")
                .whereEqualTo(FieldPath.documentId(), userSelected.getValue().getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Failed with ", e.fillInStackTrace());
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            Log.d(TAG, "You are following " + userSelected.getValue().getEmail());
                            getActionButton().setValue(UNFOLLOW);
                        } else {
                            Log.d(TAG, "You aren't following " + userSelected.getValue().getEmail());
                            getActionButton().setValue(FOLLOW);
                        }
                    }
                });
    }

    public MutableLiveData<String> getActionButton() {
        if (buttonText == null) buttonText = new MutableLiveData<>();
        return buttonText;
    }

    private void loadCounters() {
        Log.d(TAG, "counters/" + userSelected.getValue().getEmail());
        countersListener = db.document("counters/" + userSelected.getValue().getEmail())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Failed with ", e.fillInStackTrace());
                            return;
                        } if (snapshots.contains("posts")) {
                            getPostCount().setValue(snapshots.get("posts").toString());
                        } if (snapshots.contains("followers")) {
                            getFollowersCount().setValue(snapshots.get("followers").toString());
                        } if (snapshots.contains("follows")) {
                            getFollowsCount().setValue(snapshots.get("follows").toString());
                        }
                    }
                });
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
        Map<String, Object> data = new HashMap<>();
        String emailSigned = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        DocumentReference followingRef = db.collection("following/" + emailSigned + "/userFollowing").document(emailSelected);
        DocumentReference followersRef = db.collection("followers/" + emailSelected + "/userFollowers").document(emailSigned);
        DocumentReference countFollowsRef = db.collection("counters").document(emailSigned);
        DocumentReference countFollowersRef = db.collection("counters").document(emailSelected);
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

        // TODO (1) Send notification to the userSelected
    }

    public void unollowUser(){
        Map<String, Object> data = new HashMap<>();
        String emailSigned = userSigned.getValue().getEmail();
        String emailSelected = userSelected.getValue().getEmail();
        DocumentReference followingRef = db.collection("following/" + emailSigned + "/userFollowing").document(emailSelected);
        DocumentReference followersRef = db.collection("followers/" + emailSelected + "/userFollowers").document(emailSigned);
        DocumentReference countFollowsRef = db.collection("counters").document(emailSigned);
        DocumentReference countFollowersRef = db.collection("counters").document(emailSelected);
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

    public void saveUser(User user) {
        Log.d(TAG, "saveUser()");
        setUserSigned(user);
        select(user);
        db.collection("user").document(user.getEmail()).set(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User saved with success");
                } else {
                    Log.w(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        eventListener.remove();
        countersListener.remove();
    }
}
