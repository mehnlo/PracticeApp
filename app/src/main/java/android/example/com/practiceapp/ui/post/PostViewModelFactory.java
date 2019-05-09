package android.example.com.practiceapp.ui.post;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.example.com.practiceapp.data.PracticeAppRepository;
import androidx.annotation.NonNull;

public class PostViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final PracticeAppRepository mRepository;

    public PostViewModelFactory(PracticeAppRepository mRepository) { this.mRepository = mRepository; }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PostViewModel(mRepository);
    }
}
