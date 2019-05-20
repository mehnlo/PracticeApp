package android.example.com.pseudogram.data.firebase;

import android.example.com.pseudogram.data.database.PostEntry;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FirebaseFunctionsDataSource {
    private static final String TAG = FirebaseFunctionsDataSource.class.getSimpleName();
    private static final String FUNCTION_NAME = "loadFeed";
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static final String PRACTICEAPP_SYNC_TAG = "practiceapp-sync";
    private static FirebaseFunctionsDataSource sInstance;
    private final FirebaseFunctions mFunctions;

    private final MutableLiveData<PostEntry[]> mDownloadedPostsFeed;

    private FirebaseFunctionsDataSource(FirebaseFunctions functions) {
        mFunctions = functions;
        mDownloadedPostsFeed = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static FirebaseFunctionsDataSource getInstance(FirebaseFunctions functions) {
        Log.d(TAG, "Getting the firebase functions data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FirebaseFunctionsDataSource(functions);
                Log.d(TAG, "Made a new firebase functions data source");
            }
        }
        return sInstance;
    }

    /**
     * Schedules a periodic work request which fetches the feed.
     */
    public void scheduleRecurringFetchFeedSync() {
        // Create the Job to periodically sync Sunshine
        Constraints constraints = new Constraints.Builder()
                // The Woker needs Network connectivity
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request =
                // Tell which work to execute and set the interval
                new PeriodicWorkRequest.Builder(PseudogramFirebaseWorker.class, 15, TimeUnit.MINUTES)
                        // Set additional constraints
                        .setConstraints(constraints)
                        .build();
        WorkManager.getInstance()
                .enqueueUniquePeriodicWork(PRACTICEAPP_SYNC_TAG, ExistingPeriodicWorkPolicy.KEEP, request);
        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId());
        Log.d(TAG, "Work scheduled");
    }

    public void feedSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(PseudogramFirebaseWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance()
                .enqueueUniqueWork(PRACTICEAPP_SYNC_TAG, ExistingWorkPolicy.KEEP, request);
        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId());
        Log.d(TAG, "Sync in progress");
    }

    Task<Object> fetchFeed() {
        Log.d(TAG, "Starting to fetchFeed()");
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
         return mFunctions.getHttpsCallable(FUNCTION_NAME).call(data)
        .continueWith(response -> {
            // This continuation runs on either success or failure, but if the task
            // has failed then getResult() will throw an Exception which will be
            // propagated down.
            if (response.getException() != null) {
                Log.w(TAG, "throwing exception", response.getException().fillInStackTrace());
                throw new Exception(response.getException());
            }
            try {
                Object result = response.getResult().getData();
                // Parse the JSON into a list of Posts
                PostsResponse postsResponse = new PostsJsonParser().parse(result);
                Log.d(TAG, "JSON Parsing finished");
                // As long as there are Posts, update the LiveData storing the most recent
                // posts. This will trigger observers of that LiveData, such as the
                // PseudogramRepository
                if(postsResponse != null) {
                    Log.d(TAG, "JSON not null and has " + postsResponse.getPostsFeed().length + " values.");
                    if (postsResponse.getPostsFeed().length != 0) {
                        // Will eventually do something with the downloaded data
                        mDownloadedPostsFeed.postValue(postsResponse.getPostsFeed());
                    }
                }
            } catch (Exception e) {
                Log.wtf(TAG, "fetchFeed: ", e.fillInStackTrace());
            }

            return response.getResult().getData();
        });
    }

    public MutableLiveData<PostEntry[]> getCurrentPosts() {
        return mDownloadedPostsFeed;
    }

    public void cancelAllWork() {
        Log.d(TAG, "cancelAllWork()");
        WorkManager.getInstance().cancelAllWork();
    }
}
