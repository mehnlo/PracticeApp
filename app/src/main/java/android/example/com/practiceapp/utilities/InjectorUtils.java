package android.example.com.practiceapp.utilities;

import android.content.Context;
import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.database.PracticeAppDatabase;
import android.example.com.practiceapp.data.firebase.NetworkDataSource;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.ui.post.PostViewModelFactory;

public class InjectorUtils {
    private static PracticeAppRepository provideRepository(Context context) {
        PracticeAppDatabase database = PracticeAppDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        NetworkDataSource network = NetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return PracticeAppRepository.getInstance(database, network, executors);
    }
    public static NetworkDataSource provideNetworkDataSource(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return NetworkDataSource.getInstance(context.getApplicationContext(), executors);
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
