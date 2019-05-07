package android.example.com.practiceapp.data.firebase;

import android.arch.lifecycle.MutableLiveData;
import android.example.com.practiceapp.data.models.User;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class FirebaseDataSource {
    private static final String TAG = FirebaseDataSource.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String POSTS = "posts";
    private static final String FOLLOWERS = "followers";
    private static final String FOLLOWS = "follows";
    private static final String USER_FOLLOWING = "userFollowing";
    private static final String USER_FOLLOWERS = "userFollowers";
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static FirebaseDataSource sInstance;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private CollectionReference usersRef;
    private CollectionReference countersRef;
    private CollectionReference followersRef;
    private CollectionReference followingsRef;

    public FirebaseDataSource(FirebaseFirestore firestore, FirebaseStorage storage) {
        this.firestore = firestore;
        this.storage = storage;
        usersRef = firestore.collection("users");
        countersRef = firestore.collection("counters");
        followersRef = firestore.collection("followers");
        followingsRef = firestore.collection("following");
    }
    /**
     * Get the singleton for this class
     */
    public static FirebaseDataSource getInstance(FirebaseFirestore firestore, FirebaseStorage storage) {
        Log.d(TAG, "Getting the firebase data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FirebaseDataSource(firestore, storage);
                Log.d(TAG, "Made a new firebase data source");
            }
        }
        return sInstance;
    }

    /**
     * Get the user saved from FirebaseFirestore
     * @param email The user that will be loaded
     */
    public MutableLiveData<User> get(String email) {
        final String documentName = email;
        DocumentReference documentReference = usersRef.document(email);
        Log.i(TAG, "Getting '" + documentName + "' in '" + usersRef.getId() + "'.");
        final MutableLiveData<User> data = new MutableLiveData<>();
        usersRef.document(documentName)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "get failed with", e.fillInStackTrace());
                            return;
                        } if (documentSnapshot.exists()) {
                            data.setValue(documentSnapshot.toObject(User.class));
                        } else {
                            Log.d(TAG, "Document '" + documentName + "' does not exist in '" + usersRef.getId() + "'.");
                        }
                    }
                });
        return data;
    }

    /**
     * Create the user on FirebaseFirestore
     * @param user The user that will be saved
     */
    public MutableLiveData<User> create(User user) {
        final String documentName = user.getEmail();
        final Map<String, Object> userMap = user.toMap();
        DocumentReference documentReference = usersRef.document(documentName);

        Log.i(TAG, "Creating '" + documentName + "' in '" + usersRef.getId() + "'.");
        final MutableLiveData<User> data = new MutableLiveData<>();
        // First read for existing user, if not create it
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "create '" + documentName + "' failed with: ", e.fillInStackTrace());
                    return;
                } if (snapshots.exists()) {
                    Log.d(TAG, "Existing user");
                    data.setValue(snapshots.toObject(User.class));
                } else {
                    Log.d(TAG, "New user");
                    Map<String, Object> emptyData = new HashMap<>();
                    Map<String, Object> counters = new HashMap<>();
                    counters.put("followers", 0);
                    counters.put("follows" ,0);
                    counters.put("posts", 0);
                    DocumentReference counterDocRef = countersRef.document(documentName);
                    DocumentReference followersDocRef = followersRef.document(documentName);
                    DocumentReference followingDocRef = followingsRef.document(documentName);
                    DocumentReference usersDocRef = usersRef.document(documentName);
                    WriteBatch batch = firestore.batch();
                    batch.set(counterDocRef, counters);
                    batch.set(followersDocRef, emptyData);
                    batch.set(followingDocRef, emptyData);
                    batch.set(usersDocRef, userMap);
                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "New user '" + documentName + "' saved in '" + usersRef.getId() + "' with success.");
                            } else {
                                Log.w(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
                }
            }
        });
        return data;
    }

    public void update(User user) {
        final String documentName = user.getEmail();
        final Map<String, Object> userMap = user.toMap();
        DocumentReference documentReference = usersRef.document(documentName);

        Log.i(TAG, "Updating '" + documentName + "' in '" + usersRef.getId() + "'.");

        documentReference.update(userMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was an error updating '" + documentName + "' in '" + usersRef.getId() + "'.", e);
                    }
                });

    }

    public void uploadProfilePic(String email, Uri mPhotoUri) {
        // Create a storage reference from our app
        final String documentName = email;
        String location = "usuarios/" + documentName + "/profilePic/profilePicture";
        final DocumentReference documentReference = usersRef.document(documentName);

        final StorageReference mStorageRef = storage.getReference(location);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        UploadTask uploadTask = mStorageRef.putFile(mPhotoUri, metadata);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload is paused");
            }
        });

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    assert (task.getException() != null);
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return mStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d(TAG, "onComplete: " + downloadUri.toString());
                    documentReference
                            .update(User.FIELD_PHOTO_URL, downloadUri.toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Profile pic updated succesfully");
                                    }
                                    Log.w(TAG, "Failed on update profile pic: ", task.getException());
                                }
                            });
                } else {
                    // Handle failures
                    Log.w(TAG, "onComplete: task has fail");
                }
            }
        });
    }

    public MutableLiveData<String> loadFollows(String email, String emailSelected) {
        final String documentSelected = emailSelected;
        final MutableLiveData<String> data = new MutableLiveData<>();
        CollectionReference collectionReference = followingsRef.document(email).collection("userFollowing");
        Query query = collectionReference.whereEqualTo(FieldPath.documentId(), documentSelected);
        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Failed with ", e.fillInStackTrace());
                return;
            }
            if (snapshots != null && !snapshots.isEmpty()) {
                Log.d(TAG, "You are following " + documentSelected);
                data.postValue(UNFOLLOW);
            } else {
                Log.d(TAG, "You aren't following " + documentSelected);
                data.postValue(FOLLOW);
            }
        });
        return data;
    }

    public MutableLiveData<Map<String, String>> loadCounters(String email) {
        final MutableLiveData<Map<String, String>> data = new MutableLiveData<>();
        DocumentReference documentReference = countersRef.document(email);
        documentReference.addSnapshotListener((snapshots, e) -> {
            Map<String, String> aux = new HashMap<>();
            if (e != null) {
                Log.w(TAG, "Failed with ", e.fillInStackTrace());
                return;
            } if (snapshots.contains(POSTS)) {
                aux.put(POSTS, snapshots.get(POSTS).toString());
            } if (snapshots.contains(FOLLOWERS)) {
                aux.put(FOLLOWERS, snapshots.get(FOLLOWERS).toString());
            } if (snapshots.contains(FOLLOWS)) {
                aux.put(FOLLOWS, snapshots.get(FOLLOWS).toString());
            }
            data.postValue(aux);
        });
        return data;
    }

    public void follow(String email, String emailSelected) {
        Map<String, Object> data = new HashMap<>();

        DocumentReference followingDocRef = followingsRef.document(email).collection(USER_FOLLOWING).document(emailSelected);
        DocumentReference followersDocRef = followersRef.document(emailSelected).collection(USER_FOLLOWERS).document(email);
        // TODO (6) Create a cloud function to update counter
        DocumentReference countFollowsRef = countersRef.document(email);
        DocumentReference countFollowersRef = countersRef.document(emailSelected);
        // Get a new write batch
        WriteBatch batch = firestore.batch();
        batch.set(followingDocRef, data);
        batch.set(followersDocRef, data);
        batch.update(countFollowsRef, FOLLOWS, FieldValue.increment(1));
        batch.update(countFollowersRef, FOLLOWERS, FieldValue.increment(1));
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "followUser success");
            } else {
                Log.w(TAG, "followUser failed with: ", task.getException());
            }
        });
    }

    public void unfollow(String email, String emailSelected) {
        Map<String, Object> data = new HashMap<>();

        DocumentReference followingDocRef = followingsRef.document(email).collection(USER_FOLLOWING).document(emailSelected);
        DocumentReference followersDocRef = followersRef.document(emailSelected).collection(USER_FOLLOWERS).document(email);
        // TODO (6) Create a cloud function to update counter
        DocumentReference countFollowsRef = countersRef.document(email);
        DocumentReference countFollowersRef = countersRef.document(emailSelected);
        // Get a new write batch
        WriteBatch batch = firestore.batch();
        batch.delete(followingDocRef);
        batch.delete(followersDocRef);
        batch.update(countFollowsRef, "follows", FieldValue.increment(-1));
        batch.update(countFollowersRef, "followers", FieldValue.increment(-1));
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "unfollowUser success");
            } else {
                Log.w(TAG, "unfollowUser failed with: ", task.getException());
            }
        });
    }
}
