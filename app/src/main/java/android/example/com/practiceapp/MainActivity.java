package android.example.com.practiceapp;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.utilities.GlideApp;
import android.example.com.practiceapp.utilities.OnSearchSelectedListener;
import android.example.com.practiceapp.viewmodel.MainActivityViewModel;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnSearchSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SING_IN = 9001;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 88;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    public static final String USER_FRAGMENT = "USER_FRAGMENT";
    public static final String SEARCH_FRAGMENT = "SEARCH_FRAGMENT";

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

    private MainActivityViewModel mViewModel;
    private UserViewModel model;
    private User userSigned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        initDrawer();
        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        model = ViewModelProviders.of(this).get(UserViewModel.class);
        model.getUserSelected();
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
            Log.d(TAG, "onStart: shouldStartSignIn");
            startSignIn();
            return;
        }
        initUser();
    }

    private void initDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.action_take_picture, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                dispatchTakePictureIntent();
            }
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDreawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: pila: " + getSupportFragmentManager().getBackStackEntryCount());
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
                Log.d(TAG, "onActivityResult() ok");
                galleryAddPic();
                //setPic();
                Intent intentToPostActivity = new Intent(MainActivity.this, PostActivity.class);
                intentToPostActivity.putExtra(Intent.EXTRA_TEXT, mCurrentPhotoPath);
                intentToPostActivity.putExtra("photoUri", mPhotoUri.toString());

                startActivity(intentToPostActivity);
            }
        } else if (requestCode == RC_SING_IN) {
            mViewModel.setIsSigningIn(false);
            saveUser();

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
        
    }

    private void saveUser() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.account_email_key), account.getEmail());
        editor.putString(getString(R.string.account_name_key), account.getDisplayName());
        editor.putString(getString(R.string.account_photo_key), account.getPhotoUrl().toString());
        editor.putLong(getString(R.string.account_creation_date_key), FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp());
        editor.putLong(getString(R.string.account_last_sign_in_key), FirebaseAuth.getInstance().getCurrentUser().getMetadata().getLastSignInTimestamp());
        editor.apply();
        mNavHeaderTitle.setText(account.getDisplayName());
        mNavHeaderSubtitle.setText(account.getEmail());
        GlideApp.with(this)
                .load(account.getPhotoUrl())
                .circleCrop()
                .into(mNavHeaderiv);
        userSigned = new User(null, null, account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString(), null, null);
        model.setUserSigned(userSigned);
        model.select(userSigned);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users").document(account.getEmail());
        ref.set(userSigned.toMap());
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.isIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private Intent buildSignInIntent() {
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.auth_method_picker)
                .setGoogleButtonId(R.id.google_signin_button)
                .setEmailButtonId(R.id.email_signin_button)
                .setTosAndPrivacyPolicyId(R.id.custom_tos_pp)
                .build();
        AuthUI.SignInIntentBuilder builder = AuthUI.getInstance().createSignInIntentBuilder()
                .setAuthMethodPickerLayout(customLayout)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()));

        return builder.build();
    }

    private void startSignIn() {
        startActivityForResult(buildSignInIntent(), RC_SING_IN);
        mViewModel.setIsSigningIn(true);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace(); }
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
            model.select(userSigned);
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
        } else if (id == R.id.nav_settings) {
            Toast.makeText(MainActivity.this, R.string.action_settings, Toast.LENGTH_SHORT).show();
            Intent intentToStartSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intentToStartSettingsActivity);
        } else if (id == R.id.nav_invite) {
            // TODO(12) request permissions to read contacts
            showTodoToast();
        } else if (id == R.id.nav_sign_out) {
            Toast.makeText(MainActivity.this, R.string.action_sign_out, Toast.LENGTH_SHORT).show();
            signOut(MainActivity.this);
            startSignIn();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty
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
            }
            // Other permissions could go down here
        }
    }

    private void initUser(){
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE);
        String email = prefs.getString(getString(R.string.account_email_key), "");
        String username = prefs.getString(getString(R.string.account_name_key), "");
        String photoUri = prefs.getString(getString(R.string.account_photo_key), "");
        userSigned = new User(null, username, null, email, photoUri, null ,null);
        model.setUserSigned(userSigned);
        model.select(userSigned);
        Uri uri = Uri.parse(photoUri);
        if (!(TextUtils.isEmpty(email) && TextUtils.isEmpty(username) && TextUtils.isEmpty(photoUri))) {
            mNavHeaderTitle.setText(username);
            mNavHeaderSubtitle.setText(email);
            GlideApp.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(mNavHeaderiv);
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

    @NonNull
    private Task<Void> signOut(@NonNull Context context) {
        model.setUserSigned(null);
        model.select(null);
        AuthUI.getInstance().signOut(context);
        return Tasks.whenAll(
                signOutIdps(context));
    }
    
    private Task<Void> signOutIdps(@NonNull Context context) {
        return GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
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

    public void showOptions(View view) {
        Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
    }
}
