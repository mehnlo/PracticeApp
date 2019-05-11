package android.example.com.practiceapp.ui.main.user.grid;

import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.utilities.OnPostSelectedListener;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GridPostViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
    private static final String TAG = GridPostViewHolder.class.getSimpleName();
    private ImageView mImageView;
    private Post item;
    private MainViewModel model;

    GridPostViewHolder(@NonNull View itemView, MainViewModel model) {
        super(itemView);
        this.model = model;
        mImageView = itemView.findViewById(R.id.imageView);
        itemView.setOnClickListener(this);
    }

    void bind(@NonNull Post item) {
        this.item = item;
        Uri uri;
        try {
            uri = Uri.parse(item.getPhoto().getPhotoUrl());
            Glide.with(itemView.getContext())
                    .load(uri)
                    .override(mImageView.getLayoutParams().width)
                    .into(mImageView);
        } catch (NullPointerException e) {
            Log.w(TAG, "bind: parse photo uri", e);
        }
    }
    @Override
    public void onClick(View view) {
        model.select(item);
        Navigation.findNavController(view).navigate(R.id.action_global_user_to_defailPost);
    }


}
