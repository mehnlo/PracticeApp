package android.example.com.practiceapp.utilities;

import android.example.com.practiceapp.repository.UserRepository;
import android.example.com.practiceapp.viewmodel.UserViewModelFactory;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class InjectorUtils {
    public static UserRepository provideRepository() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        return UserRepository.getInstance(firestore, storage);
    }
    public static UserViewModelFactory provideUserViewModelFactory() {
        UserRepository repository = provideRepository();
        return new UserViewModelFactory(repository);
    }
}
