package ru.ppr.cppk.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import ru.ppr.cppk.R;

public class AdditionalSettingsAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private SparseArray<Fragment> data;
    LayoutInflater inflater = null;

    public AdditionalSettingsAdapter(Activity context, SparseArray<Fragment> groupsArray) {

        this.context = context;
        this.data = groupsArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {

        return data.valueAt(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return data.keyAt(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {

        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {

        return childPosition;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.additional_setting_parent_item, null);

        TextView groupTitle = (TextView) convertView.findViewById(R.id.menu_item_text);
        groupTitle.setText(data.keyAt(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        Fragment fragment = data.valueAt(groupPosition);
        convertView = fragment.onCreateView(inflater, parent, null);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

}
