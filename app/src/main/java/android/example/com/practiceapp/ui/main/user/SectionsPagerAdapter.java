package android.example.com.practiceapp.ui.main.user;

import android.example.com.practiceapp.ui.main.user.grid.GridPostFragment;
import android.example.com.practiceapp.ui.main.user.list.ListPostFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public static final String TAG = SectionsPagerAdapter.class.getSimpleName();

    SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        if (position == 0) return new GridPostFragment();
        return new ListPostFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
