package android.example.com.pseudogram.ui.main.search;

import android.content.Context;
import android.example.com.pseudogram.BuildConfig;
import android.example.com.pseudogram.R;
import android.example.com.pseudogram.data.database.UserEntry;
import android.example.com.pseudogram.ui.main.MainViewModel;
import android.example.com.pseudogram.ui.main.MainViewModelFactory;
import android.example.com.pseudogram.utilities.InjectorUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.algolia.instantsearch.core.events.CancelEvent;
import com.algolia.instantsearch.core.events.ResultEvent;
import com.algolia.instantsearch.core.events.SearchEvent;
import com.algolia.instantsearch.core.helpers.Searcher;
import com.algolia.instantsearch.core.model.AlgoliaErrorListener;
import com.algolia.instantsearch.core.model.AlgoliaResultsListener;
import com.algolia.instantsearch.ui.helpers.InstantSearch;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.instantsearch.ui.views.SearchBox;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

public class SearchFragment extends Fragment {

    public static final String TAG = SearchFragment.class.getSimpleName();
    private static final String ALGOLIA_APP_ID = BuildConfig.AlgoliaApiKey;
    private static final String ALGOLIA_SEARCH_API_KEY = BuildConfig.AlgoliaSearchApiKey;
    private static final String ALGOLIA_INDEX_NAME = BuildConfig.AlgoliaIndexName;

    private MainViewModel model;
    private Searcher mSearcher;
    private Hits mHits;
    private AlgoliaResultsListener mResultListener;
    private AlgoliaErrorListener mErrorListener;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(requireContext());
        model = ViewModelProviders.of(requireActivity(), factory).get(MainViewModel.class);
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView(view);
        subscribeToSearcher();
    }

    @Override public void onStart() {
        super.onStart();
        mErrorListener = (query, error) -> {
            Log.w(TAG, "Error searching" + query.getQuery() + ":" + error.getLocalizedMessage());
            Toast.makeText(requireContext(), R.string.error_searching + query.getQuery() + ":" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        };
        mSearcher.registerErrorListener(mErrorListener);
    }

    @Override public void onPause() {
        mSearcher.unregisterResultListener(mResultListener);
        mSearcher.unregisterErrorListener(mErrorListener);
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem itemSearch = menu.findItem(R.id.action_search);
        new InstantSearch(requireActivity(), menu, itemSearch.getItemId(), mSearcher); // link the Searcher to the UI
        mSearcher.search();

        SearchBox searchBox = (SearchBox) itemSearch.getActionView();
        searchBox.disableFullScreen();
        itemSearch.expandActionView(); // open SearchBar on startup
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void bindView(View view){
        mHits = view.findViewById(R.id.hits);
    }

    private void subscribeToSearcher() {
        mSearcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);

        mResultListener = (results, isLoadingMore) -> {
            if(TextUtils.isEmpty(mSearcher.getQuery().getQuery())) {
                mHits.setVisibility(View.INVISIBLE);
                return;
            }
            mHits.setVisibility(View.VISIBLE);
        };
        mSearcher.registerResultListener(mResultListener);

        mHits.setOnItemClickListener((recyclerView, position, v) -> {
            hideKeyboard();
            JSONObject json = mHits.get(position);
            Log.d(TAG, "JSONObject: " + json.toString());
            UserEntry user = new UserEntry(
                    json.optString(UserEntry.FIELD_EMAIL, null),
                    json.optString(UserEntry.FIELD_USERNAME, null),
                    json.optString(UserEntry.FIELD_DISPLAY_NAME, null),
                    json.optString(UserEntry.FIELD_PHOTO_URL, null),
                    json.optString(UserEntry.FIELD_TLF_NO, null),
                    json.optString(UserEntry.FIELD_GENDER, null),
                    null);
            model.select(user);
            Navigation.findNavController(v).navigate(R.id.action_search_to_other_profile);
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (requireActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Subscribe public void onCancelEvent(CancelEvent event){}

    @Subscribe public void onResultEvent(ResultEvent event){}

    @Subscribe public void onSearchEvent(SearchEvent event){}

}
