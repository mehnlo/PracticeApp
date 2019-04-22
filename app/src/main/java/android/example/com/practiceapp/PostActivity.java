package android.example.com.practiceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.com.practiceapp.models.Photo;
import android.example.com.practiceapp.utilities.FirebaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = PostActivity.class.getSimpleName();
    private ImageView mPostPhoto;
    private EditText mPostTitle;
    private String mPhotoPath;
    private Uri mPhotoUri;
    private StorageReference mStorageRef;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_post));
        mPostPhoto = findViewById(R.id.iv_post_photo);
        mPostTitle = findViewById(R.id.et_post_title);
        Intent intentThatStartedPostActivity = this.getIntent();
        if(intentThatStartedPostActivity.hasExtra(Intent.EXTRA_TEXT)) {
            mPhotoPath = intentThatStartedPostActivity.getStringExtra(Intent.EXTRA_TEXT);
           //Log.d(TAG, "onCreate: " + mPhotoPath);
        }
        if (intentThatStartedPostActivity.hasExtra("photoUri")) {
            mPhotoUri = Uri.parse(intentThatStartedPostActivity.getStringExtra("photoUri"));
            Log.d(TAG, "onCreate: mPhotoUri" + mPhotoUri.toString());
        }
    }

    // Establecer la foto hecha una vez se tenga el focus de la pantalla
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setPic();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        } else if (item.getItemId() == R.id.action_send) {
            send();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's an upload in progress, save the reference so you can query it later
        if(mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was an upload in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all UploadTasks under this StorageReference
        List<UploadTask> tasks = mStorageRef.getActiveUploadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the upload
            UploadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Success!
                }
            });
        }
    }

    private void setPic() {
        GlideApp.with(this).load(mPhotoUri).into(mPostPhoto);
    }

    public void send() {
        mPostTitle.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mPostTitle.setEnabled(false);
        showProgressLoading();
        uploadPhoto();
        //Toast.makeText(this, "Send post\nuserID: " + userID + "\npictureId: referencia de la base de datos" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return true;
    }

    private void uploadPhoto(){
        // Create a storage reference from our app
        // /usuarios/${user.email}/perfil/imagen1.jpg
        // /usuarios/${user.email}/post/imagen1.jpg
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(getString(R.string.account_email_key), "");
        String location = "usuarios/" + userEmail + "/posts/" + mPhotoUri.getLastPathSegment();
        mStorageRef = storage.getReference(location);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        UploadTask uploadTask = mStorageRef.putFile(mPhotoUri, metadata);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 + taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return mStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    hideProgressLoading();
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    Log.d(TAG, "onComplete: " + downloadUri.toString());
                    Photo photo = new Photo();
                    photo.setTitle(mPostTitle.getText().toString());
                    photo.setPhotoUri(downloadUri.toString());
                    FirebaseUtils.sendPost(PostActivity.this, photo);
                } else {
                    // Handle failures
                    Log.w(TAG, "onComplete: task has fail");
                }
            }
        });


    }

    private void showProgressLoading(){
        ProgressBar pb = findViewById(R.id.pb_loading_indicator);
        pb.setVisibility(View.VISIBLE);
    }

    private void hideProgressLoading(){
        ProgressBar pb = findViewById(R.id.pb_loading_indicator);
        pb.setVisibility(View.INVISIBLE);
    }
}
