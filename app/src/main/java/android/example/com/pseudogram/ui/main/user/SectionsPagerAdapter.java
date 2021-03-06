package android.example.com.pseudogram.ui.main.user;

import android.example.com.pseudogram.ui.main.user.grid.GridPostFragment;
import android.example.com.pseudogram.ui.main.user.list.ListPostFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull @Override public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        if (position == 0) return new GridPostFragment();
        return new ListPostFragment();
    }

    @Override public int getCount() {
        return 2;
    }
}
