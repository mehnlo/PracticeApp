package android.example.com.pseudogram.data;

import android.example.com.pseudogram.AppExecutors;
import android.example.com.pseudogram.data.database.PostEntry;
import android.example.com.pseudogram.data.database.PseudogramDatabase;
import android.example.com.pseudogram.data.database.UserEntry;
import android.example.com.pseudogram.data.firebase.NetworkDataSource;
import android.example.com.pseudogram.data.models.Photo;
import android.example.com.pseudogram.utilities.PseudogramDateUtils;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.work.WorkInfo;

import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.Map;

/**
 * Handles data operations in PracticeApp. Acts as a mediator between {@link NetworkDataSource}
 * and {@link PseudogramDatabase}
 */
public class PseudogramRepository {
    private static final String TAG = PseudogramRepository.class.getSimpleName();
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static PseudogramRepository sInstance;

    private final PseudogramDatabase db;
    private final NetworkDataSource network;
    private final AppExecutors mExecutors;

    private final LiveData<PagedList<PostEntry>> postList;
    private boolean mInitialized = false;
    private LiveData<WorkInfo> mStatus;

    private PseudogramRepository(PseudogramDatabase database, NetworkDataSource networkDataSource, AppExecutors executors) {
        db = database;
        network = networkDataSource;
        mExecutors = executors;
        mStatus = new MutableLiveData<>();
        LiveData<PostEntry[]> networkData = network.functions.getCurrentPosts();
        networkData.observeForever(newFeedFromNetwork -> mExecutors.diskIO().execute(() -> {
            // Delete old historical data
            deleteOldData();
            Log.d(TAG, "Old feed deleted");
            // Insert our new feed data into PracticeApp's database
            db.postDao().bulkInsert(newFeedFromNetwork);
            Log.d(TAG, "New values inserted");
        }));
        postList = new LivePagedListBuilder<>(db.postDao().getCurrentFeed(), 10).build();
    }

    public synchronized static PseudogramRepository getInstance(PseudogramDatabase database, NetworkDataSource network, AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PseudogramRepository(database, network, executors);
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
        mStatus = network.functions.scheduleRecurringFetchFeedSync();

        // TODO (5) isFetchNeeded
    }
    /**
     * Deletes old weather data because we don't need to keep multiple day's data
     */
    private void deleteOldData() {
        Date today = PseudogramDateUtils.getNormalizedUtcDateForToday();
        db.postDao().deleteOldFeed(today);
    }

    /**
     * Checks if there are enough posts for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    private boolean isFetchNeeded() {
//        Date today = PseudogramDateUtils.getNormalizedUtcDateForToday();
        return false;
    }

    /**
     * Firestore related operations
     */

    /**
     * Get the user from {@link PseudogramDatabase}, if the user is null, get the user from {@link NetworkDataSource} and insert it in {@link PseudogramDatabase}
     * @param email
     * @return a @{@link LiveData} of {@link UserEntry}
     */
    public LiveData<UserEntry> get(String email) {
        network.firestore.get(email).observeForever(userFromNetwork -> {
            if (userFromNetwork != null) {
                Log.d(TAG, "Inserting userFromNetwork in db");
                mExecutors.diskIO().execute(() -> db.userDao().bulkInsert(userFromNetwork));
            }
        });
        return db.userDao().getUserByEmail(email);
    }

    /**
     *
     * @param user
     * @return a @{@link LiveData} of {@link UserEntry}
     */
    public LiveData<UserEntry> create(UserEntry user) {
        network.firestore.create(user).observeForever(userEntry -> mExecutors.diskIO().execute(() -> {
            if (userEntry != null) {
                db.userDao().bulkInsert(userEntry);
            }
        }));
        return db.userDao().getUserByEmail(user.getEmail());
    }

    /**
     * Update the user in {@link PseudogramDatabase} and in backend server
     * @param user
     */
    public void update(UserEntry user) {
        mExecutors.diskIO().execute(() -> {
            db.userDao().bulkInsert(user);
            network.firestore.update(user);
        });
    }

    /**
     *
     * @param email
     * @param mPhotoUri
     */
    public void uploadProfilePic(String email, Uri mPhotoUri) {
        network.firestore.uploadProfilePic(email, mPhotoUri);
    }

    /**
     *
     * @param email
     * @param emailSelected
     * @return
     */
    public MutableLiveData<String> getFollows(String email, String emailSelected) {
        return network.firestore.loadFollows(email, emailSelected);
    }

    /**
     *
     * @param email
     * @return
     */
    public MutableLiveData<Map<String, String>> getCounters(String email) {
        return network.firestore.loadCounters(email);
    }

    /**
     *
     * @param email
     * @param emailSelected
     */
    public void follow(String email, String emailSelected) {
        network.firestore.follow(email, emailSelected);
    }

    /**
     *
     * @param email
     * @param emailSelected
     */
    public void unFollow(String email, String emailSelected) {
        network.firestore.unFollow(email, emailSelected);
    }

    public Query getBaseQuery(String email) {
        return network.firestore.getBaseQuery(email);
    }

    // TODO (enhancement #4) Create delete method

    public MutableLiveData<Integer> uploadPhoto(String email, Photo photo) {
        return network.firestore.uploadPhoto(email, photo);
    }

    /**
     * Functions related operations
     */


    /**
     *
     * @return
     */
    public LiveData<PagedList<PostEntry>> getCurrentFeed() {
        initializeData();
        return postList;
    }

    public LiveData<WorkInfo> getStatus() { return mStatus; }

    public void removeListeners() {
        network.firestore.removeListeners();
        network.functions.cancelAllWork();
    }
}
