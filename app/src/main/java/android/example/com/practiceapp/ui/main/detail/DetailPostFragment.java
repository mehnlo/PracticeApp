package android.example.com.practiceapp.ui.main.detail;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.databinding.FragmentDetailPostBinding;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailPostFragment extends Fragment {

    public DetailPostFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDetailPostBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_post, container, false);
        View view = binding.getRoot();
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        MainViewModel model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewmodel(model);
        return view;
    }
}
