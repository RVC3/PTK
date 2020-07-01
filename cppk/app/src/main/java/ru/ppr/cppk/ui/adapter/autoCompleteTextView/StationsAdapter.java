package ru.ppr.cppk.ui.adapter.autoCompleteTextView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Station;

public class StationsAdapter extends BaseListAdapter<Station> {

    private LayoutInflater layoutInflater;
    private Context context;

    public StationsAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View v = convertView;

        if (v == null) {
            v = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder();
            holder.stationName = (TextView) v.findViewById(android.R.id.text1);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Station station = getItem(position);

        holder.stationName.setText(station.getName().trim());
        if (equalsCurrentItem(station)) {
            holder.stationName.setTextColor(context.getResources().getColor(R.color.blue));
        } else {
            holder.stationName.setTextColor(Color.BLACK);
        }

        return v;
    }

    class ViewHolder {
        public TextView stationName;
    }
}
