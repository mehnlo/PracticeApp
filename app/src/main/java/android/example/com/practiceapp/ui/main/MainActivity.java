package android.example.com.practiceapp.ui.main;

import android.Manifest;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.database.UserEntry;
import android.example.com.practiceapp.databinding.ActivityMainBinding;
import android.example.com.practiceapp.databinding.NavHeaderBinding;
import android.example.com.practiceapp.ui.post.PostActivity;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

// TODO (2) Separate Signed Logic from Main Activity
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 88;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int PICK_PHOTO_CODE = 1046;
    public static final String PHOTO_URI = "photoUri";
    public static final String EMAIL = "email";

    private ActivityMainBinding mBinding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private String mCurrentPhotoPath;
    private Uri mPhotoUri;
    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        setupNavigation();
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(false);

        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            Bundle extras = getIntent().getExtras();
            Log.d(TAG, "onCreate: hasExtra(Intent.EXTRA_TEXT)");
            assert extras != null;
            if (extras.getInt(Intent.EXTRA_TEXT, 0) == RESULT_OK){
                Toast.makeText(this, R.string.image_result_ok, Toast.LENGTH_SHORT).show();
            } else if (extras.getInt(Intent.EXTRA_TEXT, 0) == RESULT_CANCELED){
                Toast.makeText(this, R.string.image_result_cancelled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bindView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        NavHeaderBinding navHeader = NavHeaderBinding.bind(mBinding.navView.getHeaderView(0));
        mBinding.setLifecycleOwner(this);
        navHeader.setLifecycleOwner(this);
        // View model
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(this.getApplicationContext());
        model = ViewModelProviders.of(this, factory).get(MainViewModel.class);
        navHeader.setViewmodel(model);
    }

    private void setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        setSupportActionBar(mBinding.toolbar);

        appBarConfiguration = new AppBarConfiguration.Builder(R.id.home, R.id.profile, R.id.search)
                .setDrawerLayout(mBinding.drawerLayout)
                .build();

        // Show and Manage the Drawer and Back Icon
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Handle Navigation item clicks
        // This works with no further action on your part if the menu and destination id's match

        NavigationUI.setupWithNavController(mBinding.navView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getLabel() != null && destination.getLabel().equals(getString(R.string.action_profile))) {
                model.select(model.getUserSigned().getValue());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }
        saveUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == RESULT_OK) {
                galleryAddPic();
                model.getUserSigned().observe(this, user -> {
                    if (user != null) {
                        Intent intentToPostActivity = new Intent(MainActivity.this, PostActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(PHOTO_URI, mPhotoUri.toString());
                        extras.putString(EMAIL, user.getEmail());

                        intentToPostActivity.putExtras(extras);
                        startActivity(intentToPostActivity);
                    }
                });

            }
        } else if (requestCode == RC_SIGN_IN) {
            model.setIsSigningIn(false);
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                saveUser();
            }
            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "onActivityResult: response: " + response.getError().getErrorCode());
                startSignIn();
            }
        } else if (requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                model.uploadProfilePic(data.getData());
            }
        }
        
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty
        if (requestCode == MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The permission was granted!
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        // Error occurred while creating the File
                        Log.w(TAG, "onRequestPermissionsResult: ", e.fillInStackTrace());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(this,
                                "android.example.com.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    Log.w(TAG, "El usuario no tiene camara");
                }
            } else {
                Toast.makeText(this, "Permission for write storage not granted.", Toast.LENGTH_SHORT).show();
                finish();
                // The permission was denied, so we can show a message why we can't run the app
                // and then close the app
            }
            // Other permissions could go down here
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Allows NavigationUI to support proper up navigation or the drawer layout
        // drawer menu, depending on the situation
        hideKeyboard();
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    private void saveUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // The user has already signed in
        if (user != null) {
            // TODO (4) Ask for, is getPhotoUrl() == null?
            //  in case that user register with email option
            //  assign a default avatar
            UserEntry userSigned = new UserEntry(user.getEmail(),
                    null,
                    user.getDisplayName(),
                    user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString(),
                    null,"unspecified",null);
            if (user.getMetadata()!= null && user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                // The user is the first time that sign in
                Log.d(TAG, "saveUser()");
                model.createUser(userSigned);

            } else {
                // This is an existing user
                Log.d(TAG, "initUser()");
                model.initUser(user.getEmail());
            }
        }
    }

    private boolean shouldStartSignIn() {
        Log.d(TAG, "shouldStartSignIn: " + !model.isIsSigningIn() + " " + (FirebaseAuth.getInstance().getCurrentUser() == null));
        return (!model.isIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private Intent buildSignInIntent() {
        Log.d(TAG, "buildSignInIntent()");
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.auth_method_picker)
                .setGoogleButtonId(R.id.google_signin_button)
                .setEmailButtonId(R.id.email_signin_button)
                .setTosAndPrivacyPolicyId(R.id.custom_tos_pp)
                .build();
        AuthUI.SignInIntentBuilder builder = AuthUI.getInstance().createSignInIntentBuilder()
                .setAuthMethodPickerLayout(customLayout)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()));

        return builder.build();
    }

    private void startSignIn() {
        Log.d(TAG, "startSignIn()");
        startActivityForResult(buildSignInIntent(), RC_SIGN_IN);
        model.setIsSigningIn(true);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void dispatchTakePictureIntent(View view) {
        Snackbar.make(view, R.string.action_take_picture, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        String[] permissionsWeNeed = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
        int permissionCheck = ContextCompat.checkSelfPermission(this, permissionsWeNeed[0]);
        if (permissionCheck != PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                Log.d(TAG, "dispatchTakePictureIntent: Build.VERSION.SKD_INT >= Build.VERSION_CODES.M ");
                ActivityCompat.requestPermissions(this, permissionsWeNeed, MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
                Log.d(TAG, "dispatchTakePictureIntent: ");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        // Error occurred while creating the File
                        Log.w(TAG, "dispatchTakePictureIntent: ", e.fillInStackTrace());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(this,
                                "android.example.com.fileprovider",
                                photoFile);
                        Log.d(TAG, "onActivityResult: mPhotoUri: " + mPhotoUri.toString());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else {
                        Log.d(TAG, "dispatchTakePictureIntent: photoFile == null");
                    }
                } else {
                    Log.w(TAG, "El usuario no tiene camara");
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsWeNeed, MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", new Locale("es", "ES")).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_PracticeApp";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "createImageFile: imageFileName:" + imageFileName);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: " + mCurrentPhotoPath);
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void signOut(View view) {
        Toast.makeText(this, R.string.action_sign_out, Toast.LENGTH_SHORT).show();
        signOut(MainActivity.this);
        navController.navigate(R.id.action_editProfileFragment_to_home);
        startSignIn();
    }

    private void signOut(@NonNull Context context) {
        model.signOut();
        Tasks.whenAll(signOutIdps(context), AuthUI.getInstance().signOut(context));
    }
    
    private Task<Void> signOutIdps(@NonNull Context context) {
        return GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
    }

    public void showOptions(View view) {
        showToast("TODO: showOptions()");
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // If you call startActivityForResult() using an intent that no app can handle, your app will crash,
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public void deleteAccount(View view) { showToast("TODO: deleteAccount()"); }
}
