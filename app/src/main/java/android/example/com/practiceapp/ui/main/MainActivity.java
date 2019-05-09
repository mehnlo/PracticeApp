package android.example.com.practiceapp.ui.main;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.User;
import android.example.com.practiceapp.ui.main.search.SearchFragment;
import android.example.com.practiceapp.ui.main.profile.UserFragment;
import android.example.com.practiceapp.ui.post.PostActivity;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.example.com.practiceapp.utilities.OnSearchSelectedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;


public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnSearchSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 88;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    public static final String USER_FRAGMENT = "USER_FRAGMENT";
    public static final String SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    public static final int PICK_PHOTO_CODE = 1046;
    public static final String PHOTO_URI = "photoUri";
    public static final String EMAIL = "email";

    private MainFragment mainFragment = new MainFragment();
    private UserFragment userFragment = new UserFragment();
    private SearchFragment searchFragment = new SearchFragment();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDreawerToggle;
    private NavigationView navigationView;
    private ImageView mNavHeaderiv;
    private TextView mNavHeaderTitle;
    private TextView mNavHeaderSubtitle;

    private String mCurrentPhotoPath;
    private Uri mPhotoUri;
    @Inject
    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDrawer();
        // View model
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(this.getApplicationContext());
        model = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(false);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            Log.d(TAG, "onCreate: hasExtra(Intent.EXTRA_TEXT)");
            if (intentThatStartedThisActivity.getIntExtra(Intent.EXTRA_TEXT, 0) == RESULT_OK){
                Toast.makeText(this, R.string.image_result_ok, Toast.LENGTH_SHORT).show();
            } else if (intentThatStartedThisActivity.getIntExtra(Intent.EXTRA_TEXT, 0) == RESULT_CANCELED){
                Toast.makeText(this, R.string.image_result_cancelled, Toast.LENGTH_SHORT).show();
            }
        }
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
        subscribeToUserSigned();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDreawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (id == R.id.nav_home) {
            if (fragmentManager.findFragmentByTag(MAIN_FRAGMENT) == null) { // First Time
                fragmentManager.beginTransaction().replace(R.id.content_main, mainFragment, MAIN_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            } else if (!fragmentManager.findFragmentByTag(MAIN_FRAGMENT).getUserVisibleHint()) {
                fragmentManager.beginTransaction().replace(R.id.content_main, mainFragment, MAIN_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(MAIN_FRAGMENT)
                        .commit();
            }
        } else if (id == R.id.nav_profile) {
            model.select(model.getUserSigned().getValue());
            if (fragmentManager.findFragmentByTag(USER_FRAGMENT) == null) { // First Time
                fragmentManager.beginTransaction().replace(R.id.content_main, userFragment, USER_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(USER_FRAGMENT)
                        .commit();
            } else if (!fragmentManager.findFragmentByTag(USER_FRAGMENT).getUserVisibleHint()) { // Other times
                fragmentManager.beginTransaction().replace(R.id.content_main, userFragment, USER_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(USER_FRAGMENT)
                        .commit();
            }
        } else if (id == R.id.nav_search) {
            if (fragmentManager.findFragmentByTag(SEARCH_FRAGMENT) == null) { // First time
                fragmentManager.beginTransaction().replace(R.id.content_main, searchFragment, SEARCH_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(SEARCH_FRAGMENT)
                        .commit();
            } else if (!fragmentManager.findFragmentByTag(SEARCH_FRAGMENT).getUserVisibleHint()) { // Other times
                fragmentManager.beginTransaction().replace(R.id.content_main, searchFragment, SEARCH_FRAGMENT)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(SEARCH_FRAGMENT)
                        .commit();
            }
        } else if (id == R.id.nav_invite) {
            // TODO(2) request permissions to read contacts
            showTodoToast();
        }

        item.setChecked(true);
        // Set the action bar title
        setTitle(item.getTitle());
        // Close the navigation drawer        
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavItemChecked(0);
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
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof SearchFragment) {
            searchFragment = (SearchFragment) fragment;
            searchFragment.setOnSearchSelectedListener(this);
        }
    }

    @Override
    public void onUserSelected() {
        // The user selected the email from the SearchFragment
        // Do something here to display that user detail
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, userFragment, USER_FRAGMENT)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void initDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Snackbar.make(view, R.string.action_take_picture, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            dispatchTakePictureIntent();
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDreawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                toolbar, /* Toolbar */
                R.string.navigation_drawer_open, /* "open drawer" description */
                R.string.navigation_drawer_close) /* "close drawer" description */ {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                hideKeyboard();
            }
        };

        // Set the draawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDreawerToggle);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        View headerView = navigationView.getHeaderView(0);

        mNavHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        mNavHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);
        mNavHeaderiv = headerView.findViewById(R.id.nav_header_iv);
    }

    private void saveUser() {
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // The user has already signed in
        if (user != null) {
            // TODO (4) Ask for, is getPhotoUrl() == null?
            //  in case that user register with email option
            //  assign a default avatar
            User userSigned = new User(null,null,
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString(),
                    null,null,null);
            if (user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                // The user is the first time that sign in
                Log.d(TAG, "saveUser()");
                saveSharedPreferences(user);
                model.createUser(userSigned);

            } else {
                // This is an existing user
                Log.d(TAG, "initUser()");
                model.initUser(user.getEmail());
            }
        }
    }

    private void subscribeToUserSigned(){
        model.getUserSigned().observe(this, user -> {
            if (user != null) {
                updateNavHeader(user);
            }
        });

    }

    private void updateNavHeader(@NonNull User user) {
        mNavHeaderTitle.setText(!TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : "Default User");
        mNavHeaderSubtitle.setText(!TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "defaultuser@practiceapp.com");
        if(!TextUtils.isEmpty(user.getPhotoUrl())) {
            Uri uri = Uri.parse(user.getPhotoUrl());
            Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(mNavHeaderiv);
        }
    }

    private User loadSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String username = prefs.getString(getString(R.string.account_username_key), "");
        String displayName = prefs.getString(getString(R.string.account_name_key), "");
        String email = prefs.getString(getString(R.string.account_email_key), "");
        String photoUri = prefs.getString(getString(R.string.account_photo_key), "");
        String tlfNo = prefs.getString(getString(R.string.account_tlfno_key), "");
        String sex = prefs.getString(getString(R.string.account_sex_key), "");


        return new User(null, username, displayName, email, photoUri, tlfNo ,sex, null);
    }

    private void saveSharedPreferences(FirebaseUser user) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.account_email_key), user.getEmail());
        editor.putString(getString(R.string.account_name_key), user.getDisplayName());
        editor.putString(getString(R.string.account_photo_key), user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
        editor.putLong(getString(R.string.account_creation_date_key), user.getMetadata().getCreationTimestamp());
        editor.putLong(getString(R.string.account_last_sign_in_key), user.getMetadata().getLastSignInTimestamp());
        editor.apply();
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

    public void setNavItemChecked (int id) {
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.getItem(id);
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
    }

    private void dispatchTakePictureIntent() {
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

    private void showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show();
    }

    public void signOut(View view) {
        Toast.makeText(this, R.string.action_sign_out, Toast.LENGTH_SHORT).show();
        signOut(MainActivity.this);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainFragment, MAIN_FRAGMENT)
                .commit();
        startSignIn();
    }

    @NonNull
    private Task<Void> signOut(@NonNull Context context) {
        model.signOut();
        AuthUI.getInstance().signOut(context);
        return Tasks.whenAll(
                signOutIdps(context));
    }
    
    private Task<Void> signOutIdps(@NonNull Context context) {
        return GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
    }

    public void showOptions(View view) {
        Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
    }

    public void onPickPhoto(View view) {
        showTodoToast();
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
}
