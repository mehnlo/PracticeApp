package android.example.com.pseudogram.ui.main.editProfile;

import android.content.Context;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.databinding.FragmentEditProfileBinding;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.example.com.pseudogram.ui.main.MainViewModelFactory;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private MainViewModel model;

    public EditProfileFragment() { }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewmodel(model);
        return binding.getRoot();
    }

    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        hideKeyboard();
        if (item.getItemId() == R.id.action_edit_profile) {
            model.updateUser();
            showToast(R.string.profile_updated);
        } else if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(binding.getRoot()).navigateUp();
        }
        return true;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (requireActivity().getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showToast(@StringRes int messageRes) { Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show(); }

}
