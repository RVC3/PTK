package ru.ppr.cppk.ui.adapter.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;

/**
 * Created by Александр on 18.03.2016.
 */
public class TrainCategoryAdapter extends BaseListAdapter<TrainCategory> {

    private LayoutInflater layoutInflater;
    private Context context;

    public TrainCategoryAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }

        TrainCategory trainCategory = getItem(position);

        TextView textView = (TextView) view;
        textView.setText(trainCategory.name);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

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

    @Override
    public long getItemId(int position) {
        TrainCategory trainCategory = getItem(position);
        return trainCategory.code;
    }
}
