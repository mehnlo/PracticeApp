package android.example.com.practiceapp.ui.post;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.ui.main.MainActivity;
import android.example.com.practiceapp.utilities.InjectorUtils;
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

public class PostActivity extends AppCompatActivity {
    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String PHOTO_URI = "photoUri";
    public static final String EMAIL = "email";
    private ImageView mPostPhoto;
    private EditText mPostTitle;
//    private String mPhotoPath;
    private Uri mPhotoUri;
    private PostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_post));
        bindView();
        subscribeToModel();
        captureIntent();
    }

    private void subscribeToModel() {
        PostViewModelFactory factory = InjectorUtils.providePostViewModelFactory(this.getApplicationContext());
        viewModel = ViewModelProviders.of(this, factory).get(PostViewModel.class);
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
    private void uploadPhoto(){
        // Create a storage reference from our app
        Photo photo = new Photo(mPostTitle.getText().toString(), mPhotoUri.toString(), null, null, null);

        viewModel.uploadPhoto(photo).observe(this, integer -> {
            if (integer != null) {
                hideProgressLoading();
                Intent intentToStartMainActivity = new Intent(this, MainActivity.class);
                intentToStartMainActivity.putExtra(Intent.EXTRA_TEXT, integer);
                startActivity(intentToStartMainActivity);
            }
        });
    }
    private void captureIntent() {
        Log.d(TAG, "captureIntent: ");
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            mPhotoUri = Uri.parse(extras.getString(PHOTO_URI));
            Log.d(TAG, "mPhotoUri: '" + mPhotoUri.toString() + "'.");
            String email = extras.getString(EMAIL);
            Log.d(TAG, "email: '" + email + "'.");
            viewModel.setEmail(email);
        }
    }

    private void bindView() {
        mPostPhoto = findViewById(R.id.iv_post_photo);
        mPostTitle = findViewById(R.id.et_post_title);
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
