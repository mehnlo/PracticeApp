package android.example.com.practiceapp.data;

import android.arch.lifecycle.MutableLiveData;
import android.example.com.practiceapp.data.database.UserDao;
import android.example.com.practiceapp.data.firebase.FirebaseDataSource;
import android.example.com.practiceapp.data.models.User;
import android.net.Uri;
import android.util.Log;

import java.util.Map;

/**
 * Handles data operations in PracticeApp. Acts as a mediator between {@link FirebaseDataSource}
 * and {@link UserDao}
 */
public class PracticeAppRepository {
    private static final String TAG = PracticeAppRepository.class.getSimpleName();
    // For singleton instantiation
    private static final Object LOCK = new Object();

    private static PracticeAppRepository sInstance;
    private final UserDao mUserDao;
    private final FirebaseDataSource mFirebaseDataSource;
//    private final AppExecutor mExecutor;

    private PracticeAppRepository(UserDao userDao, FirebaseDataSource firebaseDataSource) {
        mUserDao = userDao;
        mFirebaseDataSource = firebaseDataSource;
    }
    public synchronized static PracticeAppRepository getInstance(UserDao userDao, FirebaseDataSource firebaseDataSource) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PracticeAppRepository(userDao, firebaseDataSource);
                Log.d(TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Database related operations
     */

    /**
     * Deletes old weather data because we don't need to keep multiple day's data
     */
    private void deleteOldData() {
        // TODO Finish this method when instructed
    }

    /**
     * Checks if there are enough posts for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    private boolean isFetchNeeded() {
        // TODO Finish this method when instructed
        return true;
    }

    /**
     * Firebase related operations
     */

    /**
     *
     * @param email
     * @return
     */
    public MutableLiveData<User> get(String email) {
        return mFirebaseDataSource.get(email);
    }

    /**
     *
     * @param user
     * @return
     */
    public MutableLiveData<User> create(User user) {
        return mFirebaseDataSource.create(user);
    }

    /**
     *
     * @param user
     */
    public void update(User user) {
        mFirebaseDataSource.update(user);
    }

    /**
     *
     * @param email
     * @param mPhotoUri
     */
    public void uploadProfilePic(String email, Uri mPhotoUri) {
        mFirebaseDataSource.uploadProfilePic(email, mPhotoUri);
    }

    /**
     *
     * @param email
     * @param emailSelected
     * @return
     */
    public MutableLiveData<String> loadFollows(String email, String emailSelected) {
        return mFirebaseDataSource.loadFollows(email, emailSelected);
    }

    /**
     *
     * @param email
     * @return
     */
    public MutableLiveData<Map<String, String>> loadCounters(String email) {
        return mFirebaseDataSource.loadCounters(email);
    }

    /**
     *
     * @param email
     * @param emailSelected
     */
    public void follow(String email, String emailSelected) {
        mFirebaseDataSource.follow(email, emailSelected);
    }

    /**
     *
     * @param email
     * @param emailSelected
     */
    public void unfollow(String email, String emailSelected) {
        mFirebaseDataSource.unfollow(email, emailSelected);
    }

    // TODO (7) Delete method

}
