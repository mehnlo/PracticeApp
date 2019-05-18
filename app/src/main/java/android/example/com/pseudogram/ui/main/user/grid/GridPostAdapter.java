package android.example.com.pseudogram.ui.main.user.grid;

import android.content.Context;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.data.models.Post;
import android.example.com.pseudogram.databinding.ItemGridPostBinding;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

public class GridPostAdapter extends FirestorePagingAdapter<Post, GridPostViewHolder> {
    private ProgressBar mProgressBar;
    private Context mContext;
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
        mContext = context;
        mProgressBar = progressBar;
    }

    void setViewModel(MainViewModel model) {
        this.model = model;
    }

    @Override protected void onBindViewHolder(@NonNull GridPostViewHolder holder, int position, @NonNull Post item) {
        holder.bind(item);
    }

    @NonNull @Override public GridPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGridPostBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.item_grid_post, parent, false);
        return new GridPostViewHolder(binding, model);
    }

    @Override protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
            case LOADING_MORE:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                mProgressBar.setVisibility(View.GONE);
                break;
            case FINISHED:
                mProgressBar.setVisibility(View.GONE);
                showToast(R.string.reached_end_data);
                break;
            case ERROR:
                showToast(R.string.error_ocurred);
                retry();
                break;
        }
    }

    private void showToast(@StringRes int messageRes) { Toast.makeText(mContext, messageRes, Toast.LENGTH_SHORT).show(); }
}
