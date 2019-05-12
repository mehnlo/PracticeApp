package android.example.com.practiceapp.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkInfo;

import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.database.PostDao;
import android.example.com.practiceapp.data.database.PostEntry;
import android.example.com.practiceapp.data.database.UserDao;
import android.example.com.practiceapp.data.firebase.FirebaseDataSource;
import android.example.com.practiceapp.data.firebase.FirebaseFunctionsDataSource;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.User;
import android.example.com.practiceapp.utilities.PracticeAppDateUtils;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;
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
    private final PostDao mPostDao;
    private final FirebaseDataSource mFirebaseDataSource;
    private final FirebaseFunctionsDataSource mFunctionsDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;
    private LiveData<WorkInfo> mStatus;

    private PracticeAppRepository(PostDao postDao, FirebaseDataSource firebaseDataSource, FirebaseFunctionsDataSource functionsDataSource, AppExecutors executors) {
        mPostDao = postDao;
        mFirebaseDataSource = firebaseDataSource;
        mFunctionsDataSource = functionsDataSource;
        mExecutors = executors;
        mStatus = new MutableLiveData<>();
        LiveData<PostEntry[]> networkData = mFunctionsDataSource.getCurrentPosts();
        networkData.observeForever(newFeedFromNetwork -> mExecutors.diskIO().execute(() -> {
            // Delete old historical data
            deleteOldData();
            Log.d(TAG, "Old feed deleted");
            // Insert our new feed data into PracticeApp's database
            mPostDao.bulkInsert(newFeedFromNetwork);
            Log.d(TAG, "New values inserted");
        }));

    }
    public synchronized static PracticeAppRepository getInstance(PostDao postDao, FirebaseDataSource firebaseDataSource, FirebaseFunctionsDataSource functionsDataSource, AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PracticeAppRepository(postDao, firebaseDataSource, functionsDataSource, executors);
                Log.d(TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Database related operations
     */

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    private synchronized void initializeData() {

        // Only perfom initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        // This method call triggers PracticeApp to create its task to synchronize post data
        // periodically
        mStatus = mFunctionsDataSource.scheduleRecurringFethcFeedSync();

        // TODO (5) isFetchNeeded
    }
    /**
     * Deletes old weather data because we don't need to keep multiple day's data
     */
    private void deleteOldData() {
        Date today = PracticeAppDateUtils.getNormalizedUtcDateForToday();
        mPostDao.deleteOldFeed(today);
    }

    /**
     * Checks if there are enough posts for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    private boolean isFetchNeeded() {
//        Date today = PracticeAppDateUtils.getNormalizedUtcDateForToday();
        return false;
    }

    /**
     * Firestore related operations
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

    public Query getBaseQuery(String email) {
        return mFirebaseDataSource.getBaseQuery(email);
    }

    // TODO (7) Create delete method

    public MutableLiveData<Integer> uploadPhoto(String email, Photo photo) {
        return mFirebaseDataSource.uploadPhoto(email, photo);
    }

    /**
     * Functions related operations
     */


    /**
     *
     * @return
     */
    public LiveData<List<PostEntry>> getCurrentFeed() {
        initializeData();
        return mPostDao.getCurrentFeed();
    }

    public LiveData<WorkInfo> getStatus() { return mStatus; }
}
