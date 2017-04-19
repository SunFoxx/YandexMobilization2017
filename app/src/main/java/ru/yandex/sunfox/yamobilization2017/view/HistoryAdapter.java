package ru.yandex.sunfox.yamobilization2017.view;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.sunfox.yamobilization2017.R;
import ru.yandex.sunfox.yamobilization2017.yandex_api.Translation;


/**
 * Adapter which fills HistoryFragment either with History or Favorites list
 */
public class HistoryAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    /**
     * this list depends on which display mode selected. By default we want to see a history
     */
    private List<Translation> currentList = Translation.getTranslationsHistory();

    public HistoryAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    /**
     * Inner class containing links to a views inside each list element
     */
    private class ViewHolder {
        TextView origin;
        TextView translate;
        TextView direction;
        CheckBox favorite;
    }

    /**
     * Setting display mode. If true - only favorite translations will be shown. Else - all history (max - 50)
     *
     * @param b true if we need to display only fav translation
     */
    public void setFavorite(boolean b) {
        if (b) {
            currentList = Translation.getTranslationsFavorites();
            notifyDataSetChanged();
        } else {
            currentList = Translation.getTranslationsHistory();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return currentList.size();
    }

    @Override
    public Object getItem(int position) {
        return currentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //listener for fav checkbox
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            final Translation linkedTranslation = currentList.get(position);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    Translation.addTranslationToFavorites(linkedTranslation);
                else
                    Translation.removeTranslationFromFavorites(linkedTranslation);
            }
        };
        ViewHolder holder = null;
        //if we didn't set convertView, creating it, and filling ViewHolder with views inside convertView layout
        //else we just getting existing ViewHolder from convertView tag
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.history_list_item, null);
            holder.origin = (TextView) convertView.findViewById(R.id.history_origin);
            holder.translate = (TextView) convertView.findViewById(R.id.history_translate);
            holder.direction = (TextView) convertView.findViewById(R.id.history_direction);
            holder.favorite = (CheckBox) convertView.findViewById(R.id.history_favorite);
            holder.favorite.setOnCheckedChangeListener(listener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String defText = currentList.get(position).getDefaultText();
        String trText = currentList.get(position).getTranslatedText();
        if (defText.length() >= 25) {
            defText = defText.substring(0, 23) + "...";
        }
        if (trText.length() >= 25) {
            trText = trText.substring(0, 23) + "...";
        }

        holder.origin.setText(defText);
        holder.translate.setText(trText);
        holder.direction.setText(currentList.get(position).getDirection().toUpperCase());
        //simple trick to avoid triggering OnCheck event when setting checked status to fav checkbox
        holder.favorite.setOnCheckedChangeListener(null);
        holder.favorite.setChecked(Translation.getTranslationsFavorites().contains(currentList.get(position)));
        holder.favorite.setOnCheckedChangeListener(listener);
        return convertView;
    }

}
