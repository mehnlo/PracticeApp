package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileFragment extends Fragment implements OnItemSelectedListener {

    private Context context;
    private UserViewModel model;
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
        setUpSpinnerAdapter();
        subscribeToModel();
    }

    private void setUpSpinnerAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.sex_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sex.setAdapter(adapter);
        sex.setOnItemSelectedListener(this);
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
            //TODO (3) hide keyboard and save User
            saveUser();
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
        model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
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
        if (!TextUtils.isEmpty(user.getPhotoUrl())) {
            Uri uri = Uri.parse(user.getPhotoUrl());
            GlideApp.with(context)
                    .load(uri)
                    .circleCrop()
                    .into(profilePic);
        }
        name.setText(!TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : "");
        username.setText(!TextUtils.isEmpty(user.getUsername()) ? user.getUsername() : "");
        email.setText(!TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "defaultuser@practiceapp.com");
        sex.setSelection(!TextUtils.isEmpty(user.getSex()) ? ((ArrayAdapter)sex.getAdapter()).getPosition(user.getSex()) : 0);
        tlf.setText(!TextUtils.isEmpty(user.getTlfNo()) ? user.getTlfNo() : "");
    }

    private void saveUser() {
        String name = this.name.getText().toString();
        String username = this.username.getText().toString();
        model.getUserSigned().getValue().setUsername(username);
        model.getUserSigned().getValue().setDisplayName(name);
        model.saveUser();
        saveSharedPreferences();
    }

    private void saveSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.account_username_key), model.getUserSigned().getValue().getUsername());
        editor.putString(getString(R.string.account_name_key), model.getUserSigned().getValue().getDisplayName());
        editor.putString(getString(R.string.account_photo_key),
                (!TextUtils.isEmpty(model.getUserSigned().getValue().getPhotoUrl()) ?
                        model.getUserSigned().getValue().getPhotoUrl() : ""));
        editor.putString(getString(R.string.account_sex_key), model.getUserSigned().getValue().getSex());
        editor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        model.getUserSigned().getValue().setSex(parent.getItemAtPosition(pos).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Another interface callback
    }
}
