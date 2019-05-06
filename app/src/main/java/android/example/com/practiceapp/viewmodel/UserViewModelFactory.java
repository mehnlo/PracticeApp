package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.example.com.practiceapp.repository.UserRepository;
import android.support.annotation.NonNull;

public class UserViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final UserRepository mRepository;

    public UserViewModelFactory(UserRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(mRepository);
    }
}
