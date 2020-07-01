package ru.ppr.cppk.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ppr.cppk.R;

public class ListViewSettingsAdapter extends BaseAdapter {

    private SparseArray<Fragment> dataAdapter;
    private LayoutInflater inflater = null;
    private Activity context = null;

    static class ViewHolder {
        public TextView textView;
        public LinearLayout relativeLayout;
        public ImageView arrowBottom;
        public ImageView arrowTop;
    }

    public ListViewSettingsAdapter(Activity context, SparseArray<Fragment> dataAdapter) {
        this.dataAdapter = dataAdapter;
        this.context = context;

        inflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {

        return dataAdapter.size();
    }

    @Override
    public Object getItem(int position) {

        return dataAdapter.valueAt(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.additional_setting_parent_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) rowView.findViewById(R.id.menu_item_text);
            viewHolder.relativeLayout = (LinearLayout) rowView.findViewById(R.id.additional_setting_place_for_fragment);
            viewHolder.relativeLayout.setId(View.generateViewId());
            viewHolder.arrowBottom = (ImageView) rowView.findViewById(R.id.arrowBottom);
            viewHolder.arrowTop = (ImageView) rowView.findViewById(R.id.arrowTop);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textView.setText(dataAdapter.keyAt(position));

        FragmentTransaction fragmentTransaction = context.getFragmentManager().beginTransaction();
        Fragment fragment = context.getFragmentManager().findFragmentById(holder.relativeLayout.getId());

        if (fragment == null) {
            fragment = dataAdapter.valueAt(position);
            fragmentTransaction.add(holder.relativeLayout.getId(), fragment).hide(fragment).commit();
        } else {
            fragmentTransaction.hide(fragment).commit();
        }

        return rowView;
    }

}
