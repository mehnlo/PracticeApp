package android.example.com.pseudogram.ui.main.user.grid;

import android.example.com.pseudogram.R;
import android.example.com.pseudogram.data.models.Post;
import android.example.com.pseudogram.databinding.ItemGridPostBinding;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

public class GridPostViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
    private ItemGridPostBinding itemBinding;
    private MainViewModel model;

    GridPostViewHolder(@NonNull ItemGridPostBinding itemView, MainViewModel model) {
        super(itemView.getRoot());
        this.model = model;
        itemBinding = itemView;
        itemBinding.cardView.setOnClickListener(this);
    }

    public void bind(Post item) {
        itemBinding.setItem(item);
        itemBinding.executePendingBindings();
    }

    @Override public void onClick(View view) {
        model.select(itemBinding.getItem());
        Navigation.findNavController(view).navigate(R.id.action_profile_to_detail_post);
    }
}
