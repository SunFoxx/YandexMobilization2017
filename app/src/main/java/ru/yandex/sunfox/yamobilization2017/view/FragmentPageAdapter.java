package ru.yandex.sunfox.yamobilization2017.view;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter, which will fill ViewPager with needed Fragment
 */
public class FragmentPageAdapter extends FragmentPagerAdapter {

    /**
     * simple storage for all available tab names
     */
    private enum tabNames {
        TRANSLATE, HISTORY;
        @Override
        public String toString() {
            switch (this) {
                case TRANSLATE:
                    return "Translate";
                case HISTORY:
                    return "History";
                default:
                    return "Error";
            }
        }
    }

    private Context context;
    private TranslateFragment translateFragment;
    private HistoryFragment historyFragment;
    private TranslateFragment errorFragment;

    /**
     * Custom constructor with context setup, also initializing all fragments
     * @param fm support fragment manager
     * @param context selected context
     */
    public FragmentPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;

        translateFragment = new TranslateFragment(context);
        historyFragment = new HistoryFragment(context);
        errorFragment = new TranslateFragment(context);
    }

    @Override
    public Fragment getItem(int position) {
        tabNames name = tabNames.values()[position];
        switch (name) {
            case TRANSLATE:
                return translateFragment;
            case HISTORY:
                return historyFragment;
            default:
                return errorFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabNames.values()[position].toString();
    }

    @Override
    public int getCount() {
        return tabNames.values().length;
    }
}
