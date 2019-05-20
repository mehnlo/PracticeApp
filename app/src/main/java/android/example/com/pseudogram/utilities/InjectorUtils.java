package android.example.com.pseudogram.utilities;

import android.content.Context;
import android.example.com.pseudogram.AppExecutors;
import android.example.com.pseudogram.data.PseudogramRepository;
import android.example.com.pseudogram.data.database.PseudogramDatabase;
import android.example.com.pseudogram.data.firebase.NetworkDataSource;
import android.example.com.pseudogram.ui.main.MainViewModelFactory;
import android.example.com.pseudogram.ui.post.PostViewModelFactory;

public class InjectorUtils {
    private static PseudogramRepository provideRepository(Context context) {
        PseudogramDatabase database = PseudogramDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        NetworkDataSource network = NetworkDataSource.getInstance();
        return PseudogramRepository.getInstance(database, network, executors);
    }
    public static NetworkDataSource provideNetworkDataSource() {
        return NetworkDataSource.getInstance();
    }
    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        PseudogramRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }
    public static PostViewModelFactory providePostViewModelFactory(Context context) {
        PseudogramRepository repository = provideRepository(context.getApplicationContext());
        return new PostViewModelFactory(repository);
    }
}
