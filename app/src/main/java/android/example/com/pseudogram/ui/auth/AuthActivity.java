package android.example.com.pseudogram.ui.auth;

import android.content.Intent;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.ui.main.MainActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private static final String FIREBASE_TOS_URL = "https://firebase.google.com/terms/";
    private static final String FIREBASE_PRIVACY_POLICY_URL = "https://firebase.google.com/terms/analytics/#7_privacy";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startSignIn();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);
        // Successfully signed in
        if (resultCode == RESULT_OK) {
            startMainActivity(response);
            finish();
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showToast(R.string.sign_in_cancelled);
                finish();
                return;
            }
            if (response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showToast(R.string.no_internet_connection);
                return;
            }

            showToast(R.string.unknown_error);
            Log.e(TAG, "Sign-in error: ", response.getError());
        }
    }

    private void startSignIn() { startActivityForResult(buildSignInIntent(), RC_SIGN_IN); }

    @Override protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startMainActivity(null);
            finish();
        }
    }

    private void startMainActivity(@Nullable IdpResponse response) {
        startActivity(new Intent(this, MainActivity.class).putExtra(ExtraConstants.IDP_RESPONSE, response));
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
                .setTosAndPrivacyPolicyUrls(FIREBASE_TOS_URL, FIREBASE_PRIVACY_POLICY_URL)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()));

        return builder.build();
    }

    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(this, errorMessageRes, Toast.LENGTH_LONG).show();
    }

}
