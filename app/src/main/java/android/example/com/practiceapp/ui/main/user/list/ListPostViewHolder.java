package android.example.com.practiceapp.ui.main.user.list;

import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.databinding.ItemListPostBinding;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

class ListPostViewHolder extends RecyclerView.ViewHolder {
    private ItemListPostBinding itemBinding;

    ListPostViewHolder(ItemListPostBinding itemView) {
        super(itemView.getRoot());
        itemBinding = itemView;
    }

    void bind(@NonNull final Post item) {
       itemBinding.setItem(item);
       itemBinding.executePendingBindings();
       itemBinding.btItemOptions.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        showPopupMenu(itemBinding.btItemOptions);
    }

    private void showPopupMenu(ImageButton mButton) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mButton.getContext(), mButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_list_post_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private final String TAG = MyMenuItemClickListener.class.getSimpleName();
        MyMenuItemClickListener() {}
        @Override public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_remove) {
                Log.d(TAG, "onMenuItemClick: action_remove " + item.getItemId());
            }
            return false;
        }
    }
}
