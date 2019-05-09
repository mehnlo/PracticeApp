package android.example.com.practiceapp.ui.main.profile.list;

import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

public class ListPostAdapter extends FirestorePagingAdapter<Post, ListPostViewHolder> {

    private ProgressBar progressBar;
    private Context context;

    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     */
    ListPostAdapter(@NonNull FirestorePagingOptions<Post> options, Context context, ProgressBar progressBar) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListPostViewHolder holder, int position, @NonNull Post model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ListPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_post, parent, false);
        return new ListPostViewHolder(view);
    }

    @Override protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
            case LOADING_MORE:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                progressBar.setVisibility(View.GONE);
                break;
            case FINISHED:
                progressBar.setVisibility(View.GONE);
                showToast("Reached end of data set.");
                break;
            case ERROR:
                showToast("An error ocurred");
                retry();
                break;
        }
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
