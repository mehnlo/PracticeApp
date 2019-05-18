package android.example.com.pseudogram.ui.main;

import android.example.com.pseudogram.data.PseudogramRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final PseudogramRepository mRepository;

    public MainViewModelFactory(PseudogramRepository repository) {
        this.mRepository = repository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mRepository);
    }
}
