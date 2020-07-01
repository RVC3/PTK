package ru.ppr.cppk.ui.adapter.spinner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Fine;

/**
 * @author Dmitry Nevolin
 */
public class FineListManagementAdapter extends BaseListAdapter<Fine> {

    private final LayoutInflater layoutInflater;
    private final Map<Fine, Boolean> checkedFineMap;
    private List<Long> preDefinedCheckedCodes;
    private boolean editable;

    public FineListManagementAdapter(@NonNull Context context, boolean editable) {
        this.layoutInflater = LayoutInflater.from(context);
        this.checkedFineMap = new HashMap<>();
        this.preDefinedCheckedCodes = new ArrayList<>();
        this.editable = editable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Fine fine = getItem(position);
        Boolean isChecked = checkedFineMap.get(fine);

        if (isChecked == null && preDefinedCheckedCodes.contains(fine.getCode())) {
            checkedFineMap.put(fine, isChecked = true);
        }

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_fine_list_management, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.short_name)).setText(fine.getShortName());

        CheckBox isFineChecked = (CheckBox) convertView.findViewById(R.id.is_checked);
        isFineChecked.setChecked(isChecked != null && isChecked);
        isFineChecked.setEnabled(editable);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return editable && super.isEnabled(position);
    }

    public void setPreDefinedCheckedCodeList(@NonNull List<Long> preDefinedCheckedCodes) {
        this.preDefinedCheckedCodes = preDefinedCheckedCodes;

        notifyDataSetChanged();
    }

    public boolean inverseAndGetChecked(@NonNull Fine fine) {
        Boolean isChecked = checkedFineMap.get(fine);
        // Inversion
        isChecked = isChecked == null || !isChecked;

        checkedFineMap.put(fine, isChecked);

        notifyDataSetChanged();

        return isChecked;
    }

}
