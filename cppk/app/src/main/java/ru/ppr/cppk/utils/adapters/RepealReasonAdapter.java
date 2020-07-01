package ru.ppr.cppk.utils.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.ppr.nsi.entity.RepealReason;

public class RepealReasonAdapter extends ArrayAdapter<RepealReason> {

    private final Activity context;
    private final List<RepealReason> list;
    private final int resId;
    private int selectedPosition = -1;


    public RepealReasonAdapter(Activity context, List<RepealReason> list, int resId) {
        super(context, resId, list);
        this.context = context;
        this.list = list;
        this.resId = resId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = context.getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = (TextView) view;
        textView.setText(list.get(position).getReasonRepeal());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view ;
        if (getSelectedPosition()!=position) {

            view = context.getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            textView.setText(list.get(position).getReasonRepeal());

        }
        else {
            view=new View(context);
            view.setVisibility(View.GONE);
        }

        return view;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition=selectedPosition;
    }


}
