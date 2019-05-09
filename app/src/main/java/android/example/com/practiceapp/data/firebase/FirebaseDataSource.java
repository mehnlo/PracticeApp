package android.example.com.practiceapp.data.firebase;

import androidx.lifecycle.MutableLiveData;
import android.example.com.practiceapp.AppExecutors;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.User;
import android.net.Uri;
import android.util.Log;

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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

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

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final AppExecutors executors;
    private final CollectionReference usersRef;
    private final CollectionReference countersRef;
    private final CollectionReference followersRef;
    private final CollectionReference followingsRef;
    private final CollectionReference postsRef;

    private FirebaseDataSource(FirebaseFirestore firestore, FirebaseStorage storage, AppExecutors executors) {
        this.firestore = firestore;
        this.storage = storage;
        this.executors = executors;
        usersRef = firestore.collection("users");
        countersRef = firestore.collection("counters");
        followersRef = firestore.collection("followers");
        followingsRef = firestore.collection("following");
        postsRef = firestore.collection("posts");
    }
    /**
     * Get the singleton for this class
     */
    public static FirebaseDataSource getInstance(FirebaseFirestore firestore, FirebaseStorage storage, AppExecutors executors) {
        Log.d(TAG, "Getting the firebase data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FirebaseDataSource(firestore, storage, executors);
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
        documentReference.addSnapshotListener((snapshots, e) -> {
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
                batch.commit().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "New user '" + documentName + "' saved in '" + usersRef.getId() + "' with success.");
                    } else {
                        Log.w(TAG, "Failed with: ", task.getException());
                    }
                });
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
                .addOnFailureListener(e -> Log.d(TAG, "There was an error updating '" + documentName + "' in '" + usersRef.getId() + "'.", e));

    }

    public void uploadProfilePic(String email, Uri photoUri) {
        // Create a storage reference from our app
        final String documentName = email;
        String location = "usuarios/" + documentName + "/profilePic/profilePicture";
        final DocumentReference documentReference = usersRef.document(documentName);

        final StorageReference mStorageRef = storage.getReference(location);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        UploadTask uploadTask = mStorageRef.putFile(photoUri, metadata);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, "Upload is " + progress + "% done");
        }).addOnPausedListener(taskSnapshot -> Log.d(TAG, "Upload is paused"));

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                assert (task.getException() != null);
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return mStorageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Log.d(TAG, "onComplete: " + downloadUri.toString());
                documentReference
                        .update(User.FIELD_PHOTO_URL, downloadUri.toString())
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(TAG, "Profile pic updated succesfully");
                            }
                            Log.w(TAG, "Failed on update profile pic: ", task1.getException());
                        });
            } else {
                // Handle failures
                Log.w(TAG, "onComplete: task has fail");
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

    public Query getBaseQuery(String email) {
        String postsPath = "/posts/" + email + "/userPosts";
        return firestore.collection(postsPath).orderBy(Photo.FIELD_DATE, Query.Direction.DESCENDING);
    }

    public MutableLiveData<Integer> uploadPhoto(String email, Photo photo) {
        final MutableLiveData<Integer> result = new MutableLiveData<>();
        Uri photoUri = Uri.parse(photo.getPhotoUrl());
        String location = "usuarios/" + email + "/posts/" + photoUri.getLastPathSegment();
        StorageReference mStorageRef = storage.getReference(location);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        UploadTask uploadTask = mStorageRef.putFile(photoUri, metadata);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, progress + "% done");
        }).addOnPausedListener(taskSnapshot -> Log.d(TAG, "Upload is paused"))
                .addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                }).addOnSuccessListener(taskSnapshot -> {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
        });
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return mStorageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Log.d(TAG, "onComplete: " + downloadUri.toString());
                photo.setPhotoUrl(downloadUri.toString());
                DocumentReference postDocRef = postsRef.document(email).collection("userPosts").document();
                DocumentReference counterRef = countersRef.document(email);
                final String photoID = postDocRef.getId();
                WriteBatch batch = firestore.batch();
                batch.set(postDocRef, photo.toMap());
                // TODO (6) Create a cloud function to update counter
                batch.update(counterRef, "posts", FieldValue.increment(1));
                batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onSuccess: Photo written with ID: " + photoID);
                    result.postValue(RESULT_OK);
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "onFailure: Error adding photo", e);
                    result.postValue(RESULT_CANCELED);
                });

            } else {
                // Handle failures
                Log.w(TAG, "onComplete: task has fail");
            }
        });
        return result;
    }
}
