package android.example.com.pseudogram.ui.post;


import android.example.com.pseudogram.data.PseudogramRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PostViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final PseudogramRepository mRepository;

    public PostViewModelFactory(PseudogramRepository repository) { mRepository = repository; }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PostViewModel(mRepository);
    }
}
