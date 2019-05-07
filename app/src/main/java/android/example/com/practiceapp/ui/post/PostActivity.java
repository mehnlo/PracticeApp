package android.example.com.practiceapp.ui.post;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.firebase.FirebaseUtils;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String PHOTO_URI = "photoUri";
    private ImageView mPostPhoto;
    private EditText mPostTitle;
    private String mPhotoPath;
    private Uri mPhotoUri;
    // TODO (13) Remove this
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_post));
        mPostPhoto = findViewById(R.id.iv_post_photo);
        mPostTitle = findViewById(R.id.et_post_title);
        Intent intentThatStartedPostActivity = this.getIntent();
        if(intentThatStartedPostActivity.hasExtra(Intent.EXTRA_TEXT)) {
            mPhotoPath = intentThatStartedPostActivity.getStringExtra(Intent.EXTRA_TEXT);
           //Log.d(TAG, "onCreate: " + mPhotoPath);
        }
        if (intentThatStartedPostActivity.hasExtra("photoUri")) {
            mPhotoUri = Uri.parse(intentThatStartedPostActivity.getStringExtra(PHOTO_URI));
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
        // TODO (14) Pass it out to the repository
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all UploadTasks under this StorageReference
        List<UploadTask> tasks = mStorageRef.getActiveUploadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the upload
            UploadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, taskSnapshot -> {
                // Success!
            });
        }
    }

    private void setPic() {
        Glide.with(this).load(mPhotoUri).into(mPostPhoto);
    }

    public void send() {
        mPostTitle.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mPostTitle.setEnabled(false);
        showProgressLoading();
        uploadPhoto();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }
    // TODO(8) Move to PracticeAppRepository and FirebaseDataSource
    private void uploadPhoto(){
        // Create a storage reference from our app
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(getString(R.string.account_email_key), "");
        String location = "usuarios/" + userEmail + "/posts/" + mPhotoUri.getLastPathSegment();
        mStorageRef = storage.getReference(location);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        UploadTask uploadTask = mStorageRef.putFile(mPhotoUri, metadata);
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
                hideProgressLoading();
                Uri downloadUri = task.getResult();
                Log.d(TAG, "onComplete: " + downloadUri.toString());
                Photo photo = new Photo();
                photo.setTitle(mPostTitle.getText().toString());
                photo.setPhotoUrl(downloadUri.toString());
                FirebaseUtils.sendPost(PostActivity.this, photo);
            } else {
                // Handle failures
                Log.w(TAG, "onComplete: task has fail");
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
