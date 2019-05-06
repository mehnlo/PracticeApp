package android.example.com.practiceapp.views;

import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.utilities.GlideApp;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import org.json.JSONObject;

public class AvatarView extends AppCompatImageView implements AlgoliaHitView {
    private final Context context;

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public void onUpdateView(JSONObject result) {
        String photoUrl = result.optString(User.FIELD_PHOTO_URL);
        if (TextUtils.isEmpty(photoUrl)) {
            setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder_video));
            return;
        }
        GlideApp.with(context).load(Uri.parse(photoUrl)).into(this);
    }
}
