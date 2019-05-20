package android.example.com.pseudogram.ui.main.user.list;

import android.example.com.pseudogram.data.database.PostEntry;
import android.example.com.pseudogram.databinding.ItemListPostBinding;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ListPostViewHolder extends RecyclerView.ViewHolder {
    private ItemListPostBinding itemBinding;

    ListPostViewHolder(ItemListPostBinding itemView) {
        super(itemView.getRoot());
        itemBinding = itemView;
    }

    void bind(@NonNull final PostEntry item) {
       itemBinding.setItem(item);
       itemBinding.executePendingBindings();
    }

}
