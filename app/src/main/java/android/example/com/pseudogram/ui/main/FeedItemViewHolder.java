package android.example.com.pseudogram.ui.main;

import android.example.com.pseudogram.data.database.PostEntry;
import android.example.com.pseudogram.databinding.ItemFeedBinding;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FeedItemViewHolder extends RecyclerView.ViewHolder {
    private ItemFeedBinding itemBinding;
    public FeedItemViewHolder(@NonNull ItemFeedBinding itemView) {
        super(itemView.getRoot());
        itemBinding = itemView;
    }

    public void bindTo(PostEntry post){
        itemBinding.setItem(post);
        itemBinding.executePendingBindings();
    }
}
