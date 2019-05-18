package android.example.com.pseudogram.ui.post;

import android.content.Intent;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.databinding.ActivityPostBinding;
import android.example.com.pseudogram.ui.main.MainActivity;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String PHOTO_URI = "photoUri";
    public static final String EMAIL = "email";
    private ActivityPostBinding binding;
    private PostViewModel viewModel;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        captureIntent();
        setupNavigation();
    }

    private void bindView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post);
        binding.setLifecycleOwner(this);
        PostViewModelFactory factory = InjectorUtils.providePostViewModelFactory(this.getApplicationContext());
        viewModel = ViewModelProviders.of(this, factory).get(PostViewModel.class);
        binding.setViewmodel(viewModel);
    }

    private void captureIntent() {
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            viewModel.setEmail(extras.getString(EMAIL));
            viewModel.setPhotoUrl(extras.getString(PHOTO_URI));
        }
    }

    private void setupNavigation() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_post));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        } else if (item.getItemId() == R.id.action_send) {
            send();
        }
        return super.onOptionsItemSelected(item);
    }

    public void send() {
        binding.etPostTitle.onEditorAction(EditorInfo.IME_ACTION_DONE);
        binding.etPostTitle.setEnabled(false);
        showProgressLoading();
        uploadPhoto();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    private void uploadPhoto(){
        viewModel.uploadPhoto().observe(this, integer -> {
            if (integer != null) {
                hideProgressLoading();
                Intent intentToStartMainActivity = new Intent(this, MainActivity.class);
                intentToStartMainActivity.putExtra(Intent.EXTRA_TEXT, integer);
                startActivity(intentToStartMainActivity);
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
