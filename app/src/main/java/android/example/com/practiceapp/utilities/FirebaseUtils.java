package android.example.com.practiceapp.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.com.practiceapp.MainActivity;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Photo;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public final class FirebaseUtils {
    private static final String TAG = FirebaseUtils.class.getSimpleName();
    public static void sendPost(final Context context, Photo photo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Intent intentToStartMainActivity = new Intent(context, MainActivity.class);
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_file_key),
                Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString(context.getString(R.string.account_email_key),"");

        String collectionPath = "posts/" + userID + "/userPosts/";
        DocumentReference postRef = db.collection(collectionPath).document();
        DocumentReference counterRef = db.collection("counters").document(userID);
        final String photoID = postRef.getId();
        postRef.set(photo.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Photo written with ID: " + photoID);
                intentToStartMainActivity.putExtra(Intent.EXTRA_TEXT, RESULT_OK);
                context.startActivity(intentToStartMainActivity);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "onFailure: Error adding photo", e);
                intentToStartMainActivity.putExtra(Intent.EXTRA_TEXT, RESULT_CANCELED);
                context.startActivity(intentToStartMainActivity);
            }
        });
        counterRef.update("posts", FieldValue.increment(1));


    }
}
