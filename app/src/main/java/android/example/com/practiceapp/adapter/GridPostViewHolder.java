package android.example.com.practiceapp.adapter;

import android.example.com.practiceapp.utilities.OnPostSelectedListener;
import android.example.com.practiceapp.utilities.GlideApp;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.viewmodel.PostViewModel;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GridPostViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
    private static final String TAG = GridPostViewHolder.class.getSimpleName();
    private ImageView mImageView;
    private Post item;
    private PostViewModel model;
    private OnPostSelectedListener callback;

    GridPostViewHolder(@NonNull View itemView, PostViewModel model, OnPostSelectedListener callback) {
        super(itemView);
        this.model = model;
        this.callback = callback;
        mImageView = itemView.findViewById(R.id.imageView);
        itemView.setOnClickListener(this);
    }

    void bind(@NonNull Post item) {
        this.item = item;
        Uri uri;
        try {
            uri = Uri.parse(item.getPhoto().getPhotoUrl());
            GlideApp.with(itemView.getContext())
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
        callback.onPostSelected();
    }


}
