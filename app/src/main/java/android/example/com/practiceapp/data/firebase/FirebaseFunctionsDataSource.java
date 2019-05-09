package android.example.com.practiceapp.data.firebase;

import android.arch.lifecycle.MutableLiveData;
import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.database.PostEntry;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FirebaseFunctionsDataSource {
    private static final String TAG = FirebaseFunctionsDataSource.class.getSimpleName();
    private static final String FUNCTION_NAME = "loadFeed";
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static FirebaseFunctionsDataSource sInstance;

    private final FirebaseFunctions functions;
    private final AppExecutors mExecutors;


    private final MutableLiveData<PostEntry[]> mDownloadedPostsFeed;

    private FirebaseFunctionsDataSource(FirebaseFunctions functions, AppExecutors executors) {
        this.functions = functions;
        mExecutors = executors;
        mDownloadedPostsFeed = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static FirebaseFunctionsDataSource getInstance(FirebaseFunctions functions, AppExecutors executors) {
        Log.d(TAG, "Getting the firebase functions data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FirebaseFunctionsDataSource(functions, executors);
                Log.d(TAG, "Made a new firebase functions data source");
            }
        }
        return sInstance;
    }

    public void loadFeed() {
        Log.d(TAG, "Starting to loadFeed()");
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        final Task<Object> loadFeedTask;
        mExecutors.networkIO().execute(() -> functions
            .getHttpsCallable(FUNCTION_NAME)
            .call(data)
            .continueWith(response -> {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                if (response.getException() != null) {
                    throw new Exception(response.getException());
                }
                Object result = response.getResult().getData();
                JSONObject res = (JSONObject) response.getResult().getData();
                JSONArray names = res.names();
                Log.d(TAG, names.toString('\t'));
                //Log.d(TAG, "then: result:" +  result);
                // Parse the JSON into a list of Posts
                Gson gson = new Gson();
                Type collectionType = new TypeToken<HashMap<String,Object>>(){}.getType();
                // TODO (3) Create a class to parse the result Json into a list of Posts
                Map<String, Object> map = gson.fromJson(result.toString(), collectionType);

                // As long as there are Posts, update the LiveData storing the most recent
                // posts. This will trigger observers of that LiveData, such as the
                // PracticeAppRepository
                if (map != null && !map.isEmpty()) {
                    Log.d(TAG, "JSON not null and has " + map.size() + " values");
                }
                // Will eventually do something with the downloaded data
//                mDownloadedPostsFeed.postValue(map);
                return response.getResult().getData();
            }).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                   Exception e = task.getException();
                   if (e instanceof FirebaseFunctionsException) {
                       FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                       FirebaseFunctionsException.Code code = ffe.getCode();
                       Object details = ffe.getDetails();
                   }
                } else {
                    Log.d(TAG, "onComplete: loadFeed completed successfully");
                }
            }));
    }

    public MutableLiveData<PostEntry[]> getCurrentPosts() {
        return mDownloadedPostsFeed;
    }
}
