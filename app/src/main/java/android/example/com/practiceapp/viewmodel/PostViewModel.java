package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.models.Post;

public class PostViewModel extends ViewModel {
    private MutableLiveData<Post> postSelected;

    public void select(Post post) { postSelected.setValue(post); }

    public MutableLiveData<Post> getPostSelected() {
        return postSelected = (postSelected == null ? new MutableLiveData<Post>() : postSelected);
    }
}
