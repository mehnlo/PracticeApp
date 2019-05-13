package android.example.com.practiceapp.ui.main;

import android.example.com.practiceapp.data.database.PostEntry;
import android.example.com.practiceapp.databinding.ItemFeedBinding;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.example.com.practiceapp.R;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class FeedAdapter extends PagedListAdapter<PostEntry, FeedItemViewHolder> {

    protected FeedAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public FeedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_feed, parent, false);
        return new FeedItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedItemViewHolder holder, int position) {
        PostEntry post = getItem(position);
        if (post != null) {
            holder.bindTo(post);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            // holder.clear();
        }
    }

    private static DiffUtil.ItemCallback<PostEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<PostEntry>() {
        // PostEntry details may have changed if reloaded from the database, but ID is fixed.
        @Override
        public boolean areItemsTheSame(@NonNull PostEntry oldPost, @NonNull PostEntry newPost) {
            return oldPost.getId() == newPost.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PostEntry oldPost, @NonNull PostEntry newPost) {
            return oldPost.equals(newPost);
        }
    };
}
