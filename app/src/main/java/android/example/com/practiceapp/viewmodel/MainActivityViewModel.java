package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * ViewModel for {@link android.example.com.practiceapp.MainActivity}
 */
public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;

    public MainActivityViewModel() {
        mIsSigningIn = false;
    }

    public boolean isIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

}
