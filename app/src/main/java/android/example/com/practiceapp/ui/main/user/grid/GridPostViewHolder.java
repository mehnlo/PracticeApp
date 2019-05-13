package android.example.com.practiceapp.ui.main.user.grid;

import android.example.com.practiceapp.databinding.ItemGridPostBinding;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

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

    @Override
    public void onClick(View view) {
        model.select(itemBinding.getItem());
        Navigation.findNavController(view).navigate(R.id.action_global_user_to_defailPost);
    }
}
