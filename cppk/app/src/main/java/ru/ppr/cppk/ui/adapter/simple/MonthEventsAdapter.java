package ru.ppr.cppk.ui.adapter.simple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

public class MonthEventsAdapter extends BaseAdapter<MonthEvent> {

    private final LayoutInflater mLayoutInflater;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("(dd.MM.yyyy HH:mm)");
    private final long currentMonthId;

    public MonthEventsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        currentMonthId = Globals.getInstance()
                .getLocalDaoSession()
                .getMonthEventDao()
                .getLastMonthEvent()
                .getId();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ShiftEventViewHolder holder = null;

        if (view != null) {
            holder = (ShiftEventViewHolder) convertView.getTag();
        } else {
            view = mLayoutInflater.inflate(R.layout.item_month_shift, parent, false);

            holder = new ShiftEventViewHolder();
            holder.monthShiftDateTitleTextView = (TextView) view.findViewById(R.id.monthShiftDateTitle_textView);
            holder.monthShiftNumberTextView = (TextView) view.findViewById(R.id.monthShiftNumber_textView);
            holder.monthShiftDateTextView = (TextView) view.findViewById(R.id.monthShiftDate_textView);

            view.setTag(holder);
        }

        holder.monthShiftNumberTextView.setText(String.valueOf(items.get(position).getMonthNumber()));
        holder.monthShiftDateTextView.setText(dateFormat.format(items.get(position).getOpenDate()));

        final boolean isLastItem = (position == (items.size() - 1));
        final boolean isCurrentMonth = (currentMonthId == getItem(position).getId());
        final boolean visible = isLastItem && isCurrentMonth;

        holder.monthShiftDateTitleTextView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    private static class ShiftEventViewHolder {
        public TextView monthShiftDateTitleTextView;
        public TextView monthShiftNumberTextView;
        public TextView monthShiftDateTextView;
    }
}
