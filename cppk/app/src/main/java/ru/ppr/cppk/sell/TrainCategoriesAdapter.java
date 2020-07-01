package ru.ppr.cppk.sell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

// В будущем: 30.05.2016 Нужен ли этот класс?
public class TrainCategoriesAdapter extends BaseAdapter<TrainCategory> {

    private LayoutInflater layoutInflater;
    private Context context;

    public TrainCategoriesAdapter(LayoutInflater layoutInflater) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        TrainCategory trainCategory = getItem(position);
        if (trainCategory != null) {
            textView.setText(trainCategory.name);
        }
        return view;
    }

}
