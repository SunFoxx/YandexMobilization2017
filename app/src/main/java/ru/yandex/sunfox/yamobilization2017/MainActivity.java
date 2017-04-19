package ru.yandex.sunfox.yamobilization2017;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

import ru.yandex.sunfox.yamobilization2017.view.FragmentPageAdapter;
import ru.yandex.sunfox.yamobilization2017.view.HistoryFragment;
import ru.yandex.sunfox.yamobilization2017.view.TranslateFragment;
import ru.yandex.sunfox.yamobilization2017.yandex_api.Translation;


public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    /**
     * Application context. Stored as static field, so other classes can gain access to this context
     */
    private static Context context;
    /**
     * Representing connection status on app startup. It is needed, for ex, to check if lang spinners are filled (they are using API)
     */
    public static boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(viewPager);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  //hiding keyboard on startup
        load();
        /**
         * When we switching a tab to "history", translate fragment must be cleared and history fragment must update its content
         */
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    TranslateFragment fragment = (TranslateFragment) ((FragmentPageAdapter) viewPager.getAdapter()).getItem(0);
                    fragment.clear();
                    HistoryFragment.historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        isConnected = isConnectionAvailable();
    }

    /**
     * Transform encoding from ISO to UTF-8 (to ensure that cyrillic and others symbols are correctly displayed)
     * It seems like every respond from Yandex is using ISO, so this method must be used every time we getting respond from service
     * TODO
     * not sure if it works properly for all languages supported by YandexAPI
     *
     * @param transform string to encode
     * @return encoded string
     * @throws UnsupportedEncodingException if something went wrong
     */
    public static String encode(String transform) throws UnsupportedEncodingException {
        byte[] chars = transform.getBytes("ISO-8859-1");
        return new String(chars, Charset.forName("UTF-8"));
    }

    /**
     * Event handler for an arrow button inside TranslateFragment.
     * Here we must make sure that we are inside correct fragment (in case of unexpected behaviour)
     *
     * @param view doing nothing here
     */
    public void onSwitchLangPressed(View view) {
        //getting current fragment (selected tab). If it is translation tab, than we are OK to perform onClick action from fragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if (viewPager.getCurrentItem() == 0) {
            ((TranslateFragment) fragment).onSwitchLangPressed(view);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
    }

    /**
     * Serialization
     */
    public static void save() {
        try {
            FileOutputStream fos = new FileOutputStream(context.getFilesDir() + File.separator + "history.dat");
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(Translation.getTranslationsHistory());
            out.writeObject(Translation.getTranslationsFavorites());
            out.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("Serialization", "File not found");
        } catch (IOException e) {
            Log.e("Serialization", "IO Exception");
        }
    }

    /**
     * Deserialization
     */
    public static void load() {
        try {
            FileInputStream fis = new FileInputStream(context.getFilesDir() + File.separator + "history.dat");
            ObjectInputStream input = new ObjectInputStream(fis);
            ArrayList<Translation> newTranslations = (ArrayList<Translation>) input.readObject();
            ArrayList<Translation> newFavorites = (ArrayList<Translation>) input.readObject();
            Translation.setTranslationsAndFavorites(newTranslations, newFavorites);

            input.close();
            fis.close();
        } catch (FileNotFoundException e) {
            Log.e("Deserialization", "File not found. Possibly is first launch");
        } catch (IOException e) {
            Log.e("Deserialization", "IO Exception");
        } catch (ClassNotFoundException e) {
            Log.e("Deserialization", "Cant cast to class during deserialization");
        }
    }

    /**
     * Checking internet connectivity.
     * @return true if connection is available
     */
    public static boolean isConnectionAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast error = Toast.makeText(context, "No Internet connection. Application won't work without the Internet." +
                    "Please, fix connectivity issues and restart app", Toast.LENGTH_LONG);
            error.show();
            return false;
        }
    }
}
