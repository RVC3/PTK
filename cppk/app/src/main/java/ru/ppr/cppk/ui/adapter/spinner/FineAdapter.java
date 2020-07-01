package ru.ppr.cppk.ui.adapter.spinner;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Fine;

/**
 * @author Aleksandr Brazhkin
 */
public class FineAdapter extends BaseListAdapter<Fine> {

    private final LayoutInflater layoutInflater;
    private final Context context;
    private final int itemLayoutId;

    public FineAdapter(Context context, @LayoutRes int itemLayoutId) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemLayoutId = itemLayoutId;
        setSelectedPosition(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(itemLayoutId, parent, false);
        }

        if (position != 0) {
            Fine fine = getItem(position - 1);

            TextView textView = (TextView) view;
            textView.setText(fine.getName());

        }


        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        if (position != 0 && getSelectedPosition() != position) {

            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            Fine fine = getItem(position - 1);
            if (fine != null) {
                textView.setText(fine.getName());
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
            Fine fine = getItem(position - 1);
            return fine.getCode();
        }
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }
}
