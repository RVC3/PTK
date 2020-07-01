package ru.ppr.cppk.sell;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.ppr.nsi.entity.TariffPlan;

public class TariffPlansAdapter extends ArrayAdapter<TariffPlan> {

    //    private final List<TariffPlan> dataList;
    private final int resId;

    public TariffPlansAdapter(Activity context, int resId, List<TariffPlan> data) {
        super(context, resId, data);
//        dataList = data;
        this.resId = resId;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resId, null);
        }

        TariffPlan tariffPlan = getItem(position);
        TextView textView = (TextView) view;
        textView.setText(tariffPlan.getShortName());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);

        if (v == null) {
            v = new TextView(getContext());
        }

        v.setText(getItem(position).getShortName());
        return v;
    }

}
