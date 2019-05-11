package android.example.com.practiceapp.ui.main.detail;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import android.example.com.practiceapp.ui.main.MainViewModel;
import android.example.com.practiceapp.ui.main.MainViewModelFactory;
import android.example.com.practiceapp.utilities.InjectorUtils;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailPostFragment extends Fragment {

    public static final String TAG = DetailPostFragment.class.getSimpleName();
    private Context mContext;
    private TextView tvDate;
    private ImageView ivUserProfile;
    private TextView tvUsername;
    private ImageView ivPhoto;
    private TextView tvTitle;
    private RecyclerView rvComments;

    public DetailPostFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
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
        subscribeToViewModel();
    }

    private void subscribeToViewModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(mContext);
        MainViewModel model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
        model.getPostSelected().observe(this, post -> {
            if (post != null) {
                changeUI(post);
            } else {
                Log.d(TAG, "Post it's empty");
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
        Glide.with(this)
                .load(uri)
                .into(ivPhoto);
        if (!TextUtils.isEmpty(post.getUser().getPhotoUrl())) {
        uri = Uri.parse(post.getUser().getPhotoUrl());
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(ivUserProfile);
        }

    }

}
