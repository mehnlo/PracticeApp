package android.example.com.practiceapp.adapter;

import android.example.com.practiceapp.GlideApp;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Post;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import javax.annotation.Nonnull;

public class ProfileImagesViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ProfileImagesViewHolder.class.getSimpleName();
    private ImageView mImageView;
    public ProfileImagesViewHolder(@NonNull View itemView) {
        super(itemView);
        mImageView = itemView.findViewById(R.id.imageView);
    }
    public void bind(@Nonnull Post item) {
        Uri uri;
        try {
            uri = Uri.parse(item.getPhoto().getPhotoUri());
            GlideApp.with(itemView.getContext())
                    .load(uri)
                    .override(mImageView.getLayoutParams().width)
                    .into(mImageView);
        } catch (NullPointerException e) {
            Log.w(TAG, "bind: parse photo uri", e);
        }
    }
}
