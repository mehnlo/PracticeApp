package android.example.com.practiceapp.data.firebase;

import android.content.Context;
import android.example.com.practiceapp.utilities.InjectorUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PracticeAppFirebaseWorker extends Worker {
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public PracticeAppFirebaseWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseFunctionsDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.loadFeed();
        return Result.success();
    }

}
