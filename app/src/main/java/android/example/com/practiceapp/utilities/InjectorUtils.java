package android.example.com.practiceapp.utilities;

import android.content.Context;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.database.PracticeAppDatabase;
import android.example.com.practiceapp.data.firebase.FirebaseDataSource;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class InjectorUtils {
    private static PracticeAppRepository provideRepository(Context context) {
        PracticeAppDatabase database = PracticeAppDatabase.getInstance(context.getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDataSource firebaseDataSource = FirebaseDataSource.getInstance(firestore, storage);
        return PracticeAppRepository.getInstance(database.userDao(), firebaseDataSource);
    }
    public static MainViewModelFactory provideUserViewModelFactory(Context context) {
        PracticeAppRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }
}
