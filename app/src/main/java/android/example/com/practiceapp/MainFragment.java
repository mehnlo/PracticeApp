package android.example.com.practiceapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    public static final String FUNCTION_NAME = "loadFeed";
    public static final String TAG = MainFragment.class.getSimpleName();
    public static final String REGION = "us-central1";
    private Context context;
    private Boolean userVisibleHint = true;
    private FirebaseFunctions mFunctions;

    public MainFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Highlight the selected item has been done by NavigationView
        ((MainActivity)getActivity()).setNavItemChecked(0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userVisibleHint = true;
        // Initialize an instance of Cloud Functions:
        // NOTE: To call a function running in any location other than the default us-central-1,
        //  you must set the appropriate value at initialization. For example, on Android
        //  you would initialize with getInstance(FirebaseApp app, String region)
        mFunctions = FirebaseFunctions.getInstance(FirebaseApp.getInstance(), REGION);
        loadFeed()
        .addOnCompleteListener(new OnCompleteListener<Object>() {
            @Override
            public void onComplete(@NonNull Task<Object> task) {
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
            }
        });
    }

    private Task<Object> loadFeed() {
        Log.d(TAG, "loadFeed()");
        // Create the arguments to the callable function.
        Toast.makeText(context, "loadFeed()", Toast.LENGTH_SHORT).show();
        Map<String, Object> data = new HashMap<>();

        return mFunctions
                .getHttpsCallable(FUNCTION_NAME)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        if (task.getException() != null) {
                         throw new Exception(task.getException());
                        }
                        Object result = task.getResult().getData();
//                        Log.d(TAG, result.toString());
//                        Log.d(TAG, "then: result:" +  result);
                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<HashMap<String,Object>>(){}.getType();
                        Map<String, Object> map = gson.fromJson(result.toString(), collectionType);
                        if (map.containsKey("KJiori5411ppAZVB4nG1")) {
                            Log.d(TAG, String.valueOf(map.size()));
                        }

                        return task.getResult().getData();
                    }
                });
    }

    @Override
    public void onPause() {
        userVisibleHint = false;
        super.onPause();
    }

    @Override
    public boolean getUserVisibleHint() {
        return userVisibleHint;
    }
}
