package android.example.com.practiceapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.example.com.practiceapp.R;
import android.preference.PreferenceManager;

class SessionPreferences {

    private static final String FIREBASE_USER = "";

    public static String getFirebaseUser(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String keyForFirebaseUser = context.getString(R.string.pref_firebase_user_key);
        String defaultFirebaseUser = context.getString(R.string.pref_firebase_default);
        return prefs.getString(keyForFirebaseUser, defaultFirebaseUser);
    }
}
