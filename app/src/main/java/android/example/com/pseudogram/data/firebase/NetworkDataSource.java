package android.example.com.pseudogram.data.firebase;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;

public class NetworkDataSource {
    private static final String TAG = NetworkDataSource.class.getSimpleName();
    private static final String REGION = "europe-west1";
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static NetworkDataSource sInstance;
    public FirebaseFirestoreDataSource firestore;
    public FirebaseFunctionsDataSource functions;

    private NetworkDataSource(
            FirebaseFirestoreDataSource firestoreDataSource,
            FirebaseFunctionsDataSource functionsDataSource) {
        firestore = firestoreDataSource;
        functions = functionsDataSource;
    }

    /**
     * Get the singleton for this class
     */
    public static NetworkDataSource getInstance() {
        Log.d(TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                FirebaseFirestoreDataSource firestoreDataSource = FirebaseFirestoreDataSource.getInstance(firestore, storage);
                // NOTE: To call a function running in any location other than the default us-central-1,
                //  you must set the appropriate value at initialization. For example, on Android
                //  you would initialize with getInstance(FirebaseApp app, String region)
                FirebaseFunctions functions = FirebaseFunctions.getInstance(FirebaseApp.getInstance(), REGION);
                FirebaseFunctionsDataSource functionsDataSource = FirebaseFunctionsDataSource.getInstance(functions);
                sInstance = new NetworkDataSource(firestoreDataSource, functionsDataSource);
                Log.d(TAG, "Made a new network data source");
            }
        }
        return sInstance;
    }
}
