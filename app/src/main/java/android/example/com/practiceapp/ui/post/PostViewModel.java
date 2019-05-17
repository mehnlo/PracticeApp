package android.example.com.practiceapp.ui.post;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.example.com.practiceapp.data.models.Photo;
import android.example.com.practiceapp.data.models.Post;
import android.util.Log;

/**
 * ViewModel for {@link PostActivity}
 */
public class PostViewModel extends ViewModel {
    private static  final String TAG = PostViewModel.class.getSimpleName();
    private MutableLiveData<Post> postSelected;
    private String email;
    private PracticeAppRepository mRepo;

    /**
     *
     * @param repository
     */
    public PostViewModel(PracticeAppRepository repository) {
        mRepo = repository;
        postSelected = new MutableLiveData<>();
    }

    /**
     *
     * @param post
     */
    public void select(Post post) { postSelected.setValue(post); }

    /**
     *
     * @return
     */
    public MutableLiveData<Post> getPostSelected() { return postSelected; }

    /**
     *
     * @return
     */
    public String getEmail() { return email; }

    /**
     *
     * @param email
     */
    public void setEmail(String email) {
        Log.d(TAG, "Setting email to: '" + email + "'.");
        this.email = email;
    }

    public void setPhotoUrl(String photoUrl) {
        Photo photo = new Photo();
        photo.setPhotoUrl(photoUrl);
        Post post = new Post();
        post.setPhoto(photo);
        postSelected.setValue(post);
    }

    /**
     *
     * @return
     */
    public MutableLiveData<Integer> uploadPhoto() {
        return mRepo.uploadPhoto(email, postSelected.getValue().getPhoto());
    }
}
