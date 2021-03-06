package android.example.com.pseudogram.ui.main.search.views;

import android.content.Context;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.data.database.UserEntry;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class AvatarView extends AppCompatImageView implements AlgoliaHitView {

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void onUpdateView(JSONObject result) {
        String photoUrl = result.optString(UserEntry.FIELD_PHOTO_URL);
        if (TextUtils.isEmpty(photoUrl)) {
            setImageDrawable(getContext().getResources().getDrawable(R.drawable.placeholder_video));
            return;
        }
        Glide.with(getContext()).load(Uri.parse(photoUrl)).into(this);
    }
}
