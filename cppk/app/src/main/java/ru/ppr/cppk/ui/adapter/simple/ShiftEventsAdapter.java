package ru.ppr.cppk.ui.adapter.simple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

public class ShiftEventsAdapter extends BaseAdapter<ShiftEvent> {

    private final LayoutInflater mLayoutInflater;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("(dd.MM.yyyy HH:mm)");
    private final boolean shiftIsOpen;

    public ShiftEventsAdapter(final Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        shiftIsOpen = ShiftManager.getInstance().isShiftOpened();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ShiftEventViewHolder holder = null;

        if (view != null) {
            holder = (ShiftEventViewHolder) convertView.getTag();
        } else {
            view = mLayoutInflater.inflate(R.layout.item_shift, parent, false);

            holder = new ShiftEventViewHolder();
            holder.shiftDateTitleTextView = (TextView) view.findViewById(R.id.shiftDateTitle_textView);
            holder.shiftNumberTextView = (TextView) view.findViewById(R.id.shiftNumber_textView);
            holder.shiftDateTextView = (TextView) view.findViewById(R.id.shiftDate_textView);

            view.setTag(holder);
        }

        holder.shiftNumberTextView.setText(String.valueOf(items.get(position).getShiftNumber()));
        holder.shiftDateTextView.setText(dateFormat.format(items.get(position).getStartTime()));

        final ShiftEvent currentShift = ShiftManager.getInstance().getCurrentShiftEvent();
        final boolean isLastItem = (position == (items.size() - 1));
        final boolean isCurrentShift = (currentShift.getShiftNumber() == items.get(position).getShiftNumber());
        final boolean visible = isLastItem && shiftIsOpen && isCurrentShift;

        holder.shiftDateTitleTextView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    private static class ShiftEventViewHolder {
        public TextView shiftDateTitleTextView;
        public TextView shiftNumberTextView;
        public TextView shiftDateTextView;
    }

}
