package android.example.com.pseudogram.ui.post;

import android.example.com.pseudogram.data.PseudogramRepository;
import android.example.com.pseudogram.data.database.PostEntry;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for {@link PostActivity}
 */
public class PostViewModel extends ViewModel {
    private static  final String TAG = PostViewModel.class.getSimpleName();
    private MutableLiveData<PostEntry> postSelected;
    private String email;
    private PseudogramRepository mRepo;

    /**
     *
     * @param repository
     */
    public PostViewModel(PseudogramRepository repository) {
        mRepo = repository;
        postSelected = new MutableLiveData<>();
    }

    /**
     *
     * @param post
     */
    public void select(PostEntry post) { postSelected.setValue(post); }

    /**
     *
     * @return
     */
    public MutableLiveData<PostEntry> getPostSelected() { return postSelected; }

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
        PostEntry post = new PostEntry("", email, "", "", photoUrl, null);
        postSelected.setValue(post);
    }

    /**
     *
     * @return
     */
    public MutableLiveData<Integer> uploadPhoto() {
        return mRepo.uploadPhoto(postSelected.getValue());
    }
}
