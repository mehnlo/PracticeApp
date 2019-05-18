package android.example.com.pseudogram.data.firebase;

import android.content.Context;
import android.example.com.pseudogram.AppExecutors;
import android.example.com.pseudogram.data.database.PostEntry;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
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
    public static final String PRACTICEAPP_SYNC_TAG = "practiceapp-sync";
    private static FirebaseFunctionsDataSource sInstance;
    private final Context mContext;
    private final FirebaseFunctions mFunctions;
    private final AppExecutors mExecutors;

    private final MutableLiveData<PostEntry[]> mDownloadedPostsFeed;

    private FirebaseFunctionsDataSource(Context context, FirebaseFunctions functions, AppExecutors executors) {
        mContext = context;
        mFunctions = functions;
        mExecutors = executors;
        mDownloadedPostsFeed = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static FirebaseFunctionsDataSource getInstance(Context context, FirebaseFunctions functions, AppExecutors executors) {
        Log.d(TAG, "Getting the firebase functions data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FirebaseFunctionsDataSource(context, functions, executors);
                Log.d(TAG, "Made a new firebase functions data source");
            }
        }
        return sInstance;
    }

    /**
     * Schedules a periodic work request which fetches the feed.
     */
    public LiveData<WorkInfo> scheduleRecurringFetchFeedSync() {
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
        LiveData<WorkInfo> status = WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId());
        Log.d(TAG, "Work scheduled");
        return status;
    }

    Task<Object> fetchFeed() {
        Log.d(TAG, "Starting to fetchFeed()");
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
         Task<Object> taskResult = mFunctions.getHttpsCallable(FUNCTION_NAME).call(data)
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
                if (postsResponse != null && postsResponse.getPostsFeed().length != 0) {
                    Log.d(TAG, "JSON not null and has " + postsResponse.getPostsFeed().length + " values.");
                    // Will eventually do something with the downloaded data
                    mDownloadedPostsFeed.postValue(postsResponse.getPostsFeed());
                }
            } catch (Exception e) {
                Log.wtf(TAG, "fetchFeed: ", e.fillInStackTrace());
            }

            return response.getResult().getData();
        });
         return taskResult;
    }

    public MutableLiveData<PostEntry[]> getCurrentPosts() {
        return mDownloadedPostsFeed;
    }

    public void cancelAllWork() {
        Log.d(TAG, "cancelAllWork()");
        WorkManager.getInstance().cancelAllWork();
    }
}
