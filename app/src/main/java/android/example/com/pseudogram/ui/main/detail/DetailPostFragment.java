package android.example.com.pseudogram.ui.main.detail;

import android.example.com.pseudogram.R;
import android.example.com.pseudogram.databinding.FragmentDetailPostBinding;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.example.com.pseudogram.ui.main.MainViewModelFactory;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class DetailPostFragment extends Fragment {

    public DetailPostFragment() { }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDetailPostBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_post, container, false);
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        MainViewModel model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewmodel(model);
        return binding.getRoot();
    }
}
