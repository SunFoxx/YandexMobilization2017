package ru.yandex.sunfox.yamobilization2017.view;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import ru.yandex.sunfox.yamobilization2017.R;

import static ru.yandex.sunfox.yamobilization2017.MainActivity.encode;

/**
 * Adapter for a ListView with synonyms
 */
public class SynonymAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Map<String, String> data;

    /**
     * storage for textviews
     */
    private class ViewHolder {
        TextView number;
        TextView text;
    }

    public SynonymAdapter(Context context, Map<String, String> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        if (data.containsKey("" + (position + 1)))
            return data.get("" + (position + 1));
        return "";
    }

    @Override
    public long getItemId(int position) {
        if (data.containsKey("" + (position + 1)))
            return position + 1;
        return 0;
    }

    /**
     * This method used implicitly by listview
     *
     * @param position    number in list
     * @param convertView view to inflate from xml
     * @param parent      no usage here
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.synonym_list_item, null);
            holder.number = (TextView) convertView.findViewById(R.id.synNumber);
            holder.text = (TextView) convertView.findViewById(R.id.synText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.number.setText(getItemId(position) + "");
        holder.text.setText((String) getItem(position));

        return convertView;
    }
}
