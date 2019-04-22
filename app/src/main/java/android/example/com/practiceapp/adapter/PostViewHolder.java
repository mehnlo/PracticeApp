package android.example.com.practiceapp.adapter;

import android.example.com.practiceapp.GlideApp;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.Post;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = PostViewHolder.class.getSimpleName() ;
    private final ImageView mUserPic;
    private final ImageView mPhoto;
    private final TextView mDate;
    private final TextView mUsername;
    private final TextView mTitle;
    private final ImageButton mButton;

    public PostViewHolder(View itemView) {
        super(itemView);
        mUserPic = itemView.findViewById(R.id.iv_item_user_profile);
        mPhoto = itemView.findViewById(R.id.iv_item_photo);
        mDate = itemView.findViewById(R.id.tv_item_date);
        mUsername = itemView.findViewById(R.id.tv_item_username);
        mTitle = itemView.findViewById(R.id.tv_item_title);
        mButton = itemView.findViewById(R.id.bt_item_options);
    }

    public void bind(@NonNull final Post item) {
        mTitle.setText(item.getPhoto().getTitle());
        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(item.getPhoto().getTimestamp());
        mDate.setText(timestamp);
        mUsername.setText(item.getUser().getUsername());
        Uri uri;
        try {
            uri = Uri.parse(item.getUser().getPhotoUrl());
            GlideApp.with(itemView.getContext())
                    .load(uri)
                    .circleCrop()
                    .override(ViewGroup.LayoutParams.MATCH_PARENT)
                    .into(mUserPic);
        } catch (NullPointerException e) {
            Log.w(TAG, "bind: parse user photo uri", e );
        } try {
            uri = Uri.parse(item.getPhoto().getPhotoUri());
            GlideApp.with(itemView.getContext())
                    .load(uri)
                    .override(ViewGroup.LayoutParams.MATCH_PARENT)
                    .into(mPhoto);
        } catch (NullPointerException e) {
            Log.w(TAG, "bind: parse photo uri", e);
        }
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(mButton);
            }
            private void showPopupMenu(ImageButton mButton) {
                // inflate menu
                PopupMenu popup = new PopupMenu(mButton.getContext(), mButton);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.gallery_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
                popup.show();
            }
            class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
                MyMenuItemClickListener() {}
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_remove:
                            Log.d(TAG, "onMenuItemClick: action_remove " + item.getItemId());
                        default:
                    }
                    return false;
                }
            }
        });
    }
}
