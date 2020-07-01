package ru.ppr.cppk.ui.adapter.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.TicketType;

/**
 * Created by Александр on 18.03.2016.
 */
public class TicketTypeAdapter extends BaseListAdapter<TicketType> {

    private LayoutInflater layoutInflater;
    private Context context;

    public TicketTypeAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        if (position != 0) {
            TicketType ticketType = getItem(position - 1);

            TextView textView = (TextView) view;
            textView.setText(ticketType.toString());
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        if (position != 0 && getSelectedPosition() != position) {

            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            TicketType ticketType = getItem(position - 1);
            if (ticketType != null) {
                textView.setText(ticketType.toString());
            }

        } else {
            view = new View(context);
            view.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        } else {
            TicketType ticketType = getItem(position - 1);
            return ticketType.getCode();
        }
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }
}
