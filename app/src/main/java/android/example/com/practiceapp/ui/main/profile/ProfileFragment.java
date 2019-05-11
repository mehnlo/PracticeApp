package android.example.com.practiceapp.ui.main.profile;

import android.example.com.practiceapp.R;
import android.example.com.practiceapp.ui.main.user.UserFragment;
import android.view.View;

import androidx.navigation.Navigation;

public class ProfileFragment extends UserFragment {
    @Override
    protected void editProfile(View view) {
        super.editProfile(view);
        Navigation.findNavController(view).navigate(R.id.action_profile_to_editProfile);
    }
}
