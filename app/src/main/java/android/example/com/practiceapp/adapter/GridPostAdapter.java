package android.example.com.practiceapp.adapter;

import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.utilities.OnPostSelectedListener;
import android.example.com.practiceapp.viewmodel.PostViewModel;
import android.support.annotation.NonNull;
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
    private PostViewModel model;
    private OnPostSelectedListener callback;

    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     */
    GridPostAdapter(@NonNull FirestorePagingOptions<Post> options, Context context, ProgressBar progressBar) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
    }

    void setViewModel(PostViewModel model) {
        this.model = model;
    }

    void setCallback(OnPostSelectedListener callback) {
        this.callback = callback;
    }

    @Override
    protected void onBindViewHolder(@NonNull GridPostViewHolder holder, int position, @NonNull Post model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public GridPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new View
        View v = LayoutInflater.from(context).inflate(R.layout.item_grid_post, parent, false);
        return new GridPostViewHolder(v, model, callback);
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