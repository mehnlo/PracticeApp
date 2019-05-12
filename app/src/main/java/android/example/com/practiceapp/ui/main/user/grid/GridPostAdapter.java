package android.example.com.practiceapp.ui.main.user.grid;

import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.ui.main.MainViewModel;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

public class GridPostAdapter extends FirestorePagingAdapter<Post, GridPostViewHolder> {

    private ProgressBar progressBar;
    private Context context;
    private MainViewModel model;

    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     * @param context
     * @param progressBar
     */
    GridPostAdapter(@NonNull FirestorePagingOptions<Post> options, Context context, ProgressBar progressBar) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
    }

    void setViewModel(MainViewModel model) {
        this.model = model;
    }

    @Override
    protected void onBindViewHolder(@NonNull GridPostViewHolder holder, int position, @NonNull Post model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public GridPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new View
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_post, parent, false);
        return new GridPostViewHolder(view, model);
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
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
