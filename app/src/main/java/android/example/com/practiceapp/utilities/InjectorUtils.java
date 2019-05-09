package android.example.com.practiceapp.utilities;

import android.content.Context;
import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.database.PracticeAppDatabase;
import android.example.com.practiceapp.data.firebase.FirebaseDataSource;
import android.example.com.practiceapp.data.firebase.FirebaseFunctionsDataSource;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.ui.post.PostViewModelFactory;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;

public class InjectorUtils {
    private static final String REGION = "us-central1";
    private static PracticeAppRepository provideRepository(Context context) {
        PracticeAppDatabase database = PracticeAppDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDataSource firebaseDataSource = FirebaseDataSource.getInstance(firestore, storage, executors);
        // NOTE: To call a function running in any location other than the default us-central-1,
        //  you must set the appropriate value at initialization. For example, on Android
        //  you would initialize with getInstance(FirebaseApp app, String region)
        FirebaseFunctions functions = FirebaseFunctions.getInstance(FirebaseApp.getInstance(), REGION);
        FirebaseFunctionsDataSource functionsDataSource = FirebaseFunctionsDataSource.getInstance(functions, executors);
        return PracticeAppRepository.getInstance(database.userDao(), firebaseDataSource, functionsDataSource, executors);
    }
    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        PracticeAppRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }
    public static PostViewModelFactory providePostViewModelFactory(Context context) {
        PracticeAppRepository repository = provideRepository(context.getApplicationContext());
        return new PostViewModelFactory(repository);
    }
}
