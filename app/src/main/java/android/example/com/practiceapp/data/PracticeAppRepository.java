package android.example.com.practiceapp.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.work.WorkInfo;
import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.database.PostEntry;
import android.example.com.practiceapp.data.database.PracticeAppDatabase;
import android.example.com.practiceapp.data.database.UserEntry;
import android.example.com.practiceapp.data.firebase.NetworkDataSource;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.utilities.PracticeAppDateUtils;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.firestore.Query;
import java.util.Date;
import java.util.Map;

/**
 * Handles data operations in PracticeApp. Acts as a mediator between {@link NetworkDataSource}
 * and {@link PracticeAppDatabase}
 */
public class PracticeAppRepository {
    private static final String TAG = PracticeAppRepository.class.getSimpleName();
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static PracticeAppRepository sInstance;

    private final PracticeAppDatabase db;
    private final NetworkDataSource network;
    private final AppExecutors mExecutors;

    private final LiveData<PagedList<PostEntry>> postList;
    private boolean mInitialized = false;
    private LiveData<WorkInfo> mStatus;

    private PracticeAppRepository(PracticeAppDatabase database, NetworkDataSource networkDataSource, AppExecutors executors) {
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

    public synchronized static PracticeAppRepository getInstance(PracticeAppDatabase database, NetworkDataSource network, AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PracticeAppRepository(database, network, executors);
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
        mStatus = network.functions.scheduleRecurringFethcFeedSync();

        // TODO (5) isFetchNeeded
    }
    /**
     * Deletes old weather data because we don't need to keep multiple day's data
     */
    private void deleteOldData() {
        Date today = PracticeAppDateUtils.getNormalizedUtcDateForToday();
        db.postDao().deleteOldFeed(today);
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
     * Get the user from {@link PracticeAppDatabase}, if the user is null, get the user from {@link NetworkDataSource} and insert it in {@link PracticeAppDatabase}
     * @param email
     * @return a @{@link LiveData} of {@link UserEntry}
     */
    public LiveData<UserEntry> get(String email) {
        LiveData<UserEntry> result;
        result = db.userDao().getUserByEmail(email);
        result.observeForever(userEntry -> {
            if (userEntry == null) network.firestore.get(email).observeForever(userFromNetwork -> {
                    if (userFromNetwork != null) mExecutors.diskIO().execute(() -> db.userDao().bulkInsert(userFromNetwork));
                });
        });
        return result;
    }

    /**
     *
     * @param user
     * @return a @{@link LiveData} of {@link UserEntry}
     */
    public LiveData<UserEntry> create(UserEntry user) {
        network.firestore.create(user).observeForever(userEntry -> mExecutors.diskIO().execute(() ->{
            if (userEntry != null) {
                db.userDao().bulkInsert(userEntry);
            }
        }));
        return db.userDao().getUserByEmail(user.getEmail());
    }

    /**
     * Update the user in {@link PracticeAppDatabase} and in backend server
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
    public void unfollow(String email, String emailSelected) {
        network.firestore.unfollow(email, emailSelected);
    }

    public Query getBaseQuery(String email) {
        return network.firestore.getBaseQuery(email);
    }

    // TODO (7) Create delete method

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
}
