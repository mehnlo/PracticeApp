package android.example.com.pseudogram.data.firebase;

import android.content.Context;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.functions.FirebaseFunctionsException;

public class PseudogramFirebaseWorker extends ListenableWorker {
    private static final String TAG = PseudogramFirebaseWorker.class.getSimpleName();
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public PseudogramFirebaseWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(this::attachCompleter);
    }


    private Object attachCompleter(CallbackToFutureAdapter.Completer<Result> completer) {
        NetworkDataSource network = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());
        return network.functions
                .fetchFeed()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                            Log.w(TAG, "Exception: " + code + " " + details.toString());
                            completer.setException(e);
                        }
                        completer.set(Result.failure());
                    } else {
                        Log.d(TAG, "fetchFeed completed successfully");
                        completer.set(Result.success());
                    }
                });
    }
}
