package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.utilities.GlideApp;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileFragment extends Fragment {

    private Context context;
    private ImageView profilePic;
    private EditText name;
    private EditText username;
    private TextView email;
    private TextView tlf;
    private Spinner sex;

    public EditProfileFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.edit_profile_fragment_title));
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        subscribeToModel();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update) {
            showToast("TODO");
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void bindView() {
        profilePic = getView().findViewById(R.id.profile_pic);
        username = getView().findViewById(R.id.et_username);
        name = getView().findViewById(R.id.et_name);
        email = getView().findViewById(R.id.tv_email);
        tlf = getView().findViewById(R.id.tv_tlf);
        sex = getView().findViewById(R.id.sp_sex);
    }

    private void subscribeToModel() {
        UserViewModel model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        model.getUserSigned().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    updateUI(user);
                }
            }
        });
    }
    private void updateUI(User user) {
        Uri uri = Uri.parse(user.getPhotoUrl());
        GlideApp.with(context)
                .load(uri)
                .circleCrop()
                .into(profilePic);
        if (!TextUtils.isEmpty(user.getDisplayName())) name.setText(user.getDisplayName());
        if (!TextUtils.isEmpty(user.getUsername())) username.setText(user.getUsername());
        if (!TextUtils.isEmpty(user.getEmail())) email.setText(user.getEmail());
    }
}
