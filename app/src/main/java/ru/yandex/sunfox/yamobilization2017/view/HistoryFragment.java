package ru.yandex.sunfox.yamobilization2017.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import ru.yandex.sunfox.yamobilization2017.R;

public class HistoryFragment extends Fragment {

    private Context context;
    //private ListView historyList;
    private RadioGroup radioGroup;

    public static HistoryAdapter historyAdapter;
    private static ListView historyList;

    public HistoryFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View view = getView();
        if (view != null) {
            historyList = (ListView) view.findViewById(R.id.history_list);
            historyAdapter = new HistoryAdapter(context);
            historyList.setAdapter(historyAdapter);
            radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.history_tab_anim);
                    RadioButton button = (RadioButton) group.findViewById(checkedId);
                    button.startAnimation(animation);
                    if (checkedId == R.id.radio_history) {
                        historyAdapter.setFavorite(false);
                    }
                    else if (checkedId == R.id.radio_favorite) {
                        historyAdapter.setFavorite(true);
                    }
                }
            });
        }
    }
}
