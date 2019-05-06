package android.example.com.practiceapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.models.Post;
import android.example.com.practiceapp.utilities.GlideApp;
import android.example.com.practiceapp.viewmodel.PostViewModel;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailPostFragment extends Fragment {

    public static final String TAG = DetailPostFragment.class.getSimpleName();
    private Context context;
    private TextView tvDate;
    private ImageView ivUserProfile;
    private TextView tvUsername;
    private ImageView ivPhoto;
    private TextView tvTitle;
    private RecyclerView rvComments;

    public DetailPostFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_post, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
        PostViewModel model = ViewModelProviders.of(getActivity()).get(PostViewModel.class);
        model.getPostSelected().observe(this, new Observer<Post>() {
            @Override
            public void onChanged(@Nullable Post post) {
                if (post != null) {
                    changeUI(post);
                } else {
                    Log.d(TAG, "Post it's empty");
                }
            }
        });
    }

    private void bindView() {
        tvDate = getView().findViewById(R.id.tv_item_date);
        tvUsername = getView().findViewById(R.id.tv_item_username);
        tvTitle = getView().findViewById(R.id.tv_item_title);
        ivUserProfile = getView().findViewById(R.id.iv_item_user_profile);
        ivPhoto = getView().findViewById(R.id.iv_item_photo);
        rvComments = getView().findViewById(R.id.rv_comments);

    }
    private void changeUI(Post post) {
        tvDate.setText(post.getPhoto().getDate().toString());
        tvUsername.setText(post.getUser().getUsername());
        tvTitle.setText(post.getPhoto().getTitle());

        Uri uri = Uri.parse(post.getPhoto().getPhotoUrl());
        GlideApp.with(this)
                .load(uri)
                .into(ivPhoto);
        if (!TextUtils.isEmpty(post.getUser().getPhotoUrl())) {
        uri = Uri.parse(post.getUser().getPhotoUrl());
        GlideApp.with(this)
                .load(uri)
                .circleCrop()
                .into(ivUserProfile);
        }

    }

}
