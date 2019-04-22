package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.models.User;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> userSelected;

    public void select(User user) {
        userSelected.setValue(user);
    }

    public MutableLiveData<User> getUserSelected() {
        if (userSelected == null) {
            userSelected = new MutableLiveData<>();
        }
        return userSelected;
    }

}
