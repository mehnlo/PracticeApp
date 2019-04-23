package android.example.com.practiceapp;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.instantsearch.core.events.SearchEvent;
import com.algolia.instantsearch.core.helpers.Searcher;
import com.algolia.instantsearch.core.model.AlgoliaErrorListener;
import com.algolia.instantsearch.core.model.AlgoliaResultsListener;
import com.algolia.instantsearch.core.model.SearchResults;
import com.algolia.instantsearch.ui.helpers.InstantSearch;
import com.algolia.instantsearch.ui.utils.ItemClickSupport;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Query;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;


public class SearchFragment extends Fragment {

    public static final String TAG = SearchFragment.class.getSimpleName();
    public static final String ALGOLIA_APP_ID = "62MAN5SB6V";
    public static final String ALGOLIA_SEARCH_API_KEY = "a5b6026886482735cb7d88d84ce65848";
    public static final String ALGOLIA_INDEX_NAME = "usuarios";


    private Context mContext;
    private UserViewModel model;
    private Boolean userVisibleHint = true;
    private Searcher mSearcher;
    private Hits mHits;
    private AlgoliaResultsListener mResultListener;
    private AlgoliaErrorListener mErrorListener;
    private OnSearchSelectedListener callback;

    private ImageView mPictureHit;
    private TextView mUserNameHit;
    private TextView mEmailHit;


    public void setOnSearchSelectedListener(OnSearchSelectedListener callback) {
        this.callback = callback;
    }
    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    public interface OnSearchSelectedListener {
        void onUserSelected();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        bindView();
        userVisibleHint = true;
        mSearcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);
        new InstantSearch(getActivity(), mSearcher); // link the Searcher to the UI
        mResultListener = new AlgoliaResultsListener() {
            @Override
            public void onResults(@NonNull SearchResults results, boolean isLoadingMore) {
                if(TextUtils.isEmpty(mSearcher.getQuery().getQuery())) {
                    mHits.setVisibility(View.INVISIBLE);
                    return;
                }
                mHits.setVisibility(View.VISIBLE);
            }
        };
        mSearcher.registerResultListener(mResultListener);
        mSearcher.search(); // Show results for empty query (on app launch) / voice query (from intent)
        mHits.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, int position, View v) {
                // TODO hide keyboard
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) { e.printStackTrace(); }
                JSONObject jsonObject = mHits.get(position);

                Log.d(TAG, "JSONObject: " + jsonObject.toString());
                try {
                    User user = new User(
                            null,
                            jsonObject.getString(User.FIELD_USERNAME),
                            jsonObject.getString(User.FIELD_DISPLAYNAME),
                            jsonObject.getString(User.FIELD_EMAIL),
                            jsonObject.getString(User.FIELD_PHOTO_URL),
                            null,
                            null
                    );
                    // Comunica con UserFragment
                    model.select(user);
                    // Comunica con mainActivity
                    callback.onUserSelected();
                } catch (JSONException e) { e.printStackTrace(); }
                Toast.makeText(mContext, "TODO", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mErrorListener = new AlgoliaErrorListener() {
            @Override
            public void onError(@NonNull Query query, @NonNull AlgoliaException error) {
                Log.w(TAG, "Error searching" + query.getQuery() + ":" + error.getLocalizedMessage());
                Toast.makeText(mContext, "Error searching" + query.getQuery() + ":" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mSearcher.registerErrorListener(mErrorListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Highlight the selected item has been done by NavigationView
        ((MainActivity)getActivity()).setNavItemChecked(4);
    }

    @Override
    public void onPause() {
        userVisibleHint = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        mSearcher.unregisterResultListener(mResultListener);
        mSearcher.unregisterErrorListener(mErrorListener);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean getUserVisibleHint() { return userVisibleHint; }

    private void bindView(){
        mHits = getView().findViewById(R.id.hits);
        mPictureHit = getView().findViewById(R.id.imageView2);
        mEmailHit = getView().findViewById(R.id.user_email);
        mUserNameHit = getView().findViewById(R.id.user_name);
    }
    @Subscribe
    public void onSearchEvent(SearchEvent event){ }

}
