package ru.ppr.cppk.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;

/**
 * @author Dmitry Nevolin
 */
public class SettingsManagementAdapter extends BaseListAdapter<String> {

    private final LayoutInflater layoutInflater;
    private final Map<String, Boolean> currentSettingsMap;
    private final Map<String, String> settingsNameMap;

    public SettingsManagementAdapter(@NonNull Context context,
                                     @NonNull Map<String, Boolean> initialSettingsMap,
                                     @NonNull Map<String, String> settingsNameMap) {
        layoutInflater = LayoutInflater.from(context);
        currentSettingsMap = initialSettingsMap;
        this.settingsNameMap = settingsNameMap;

        setItems(new ArrayList<>(currentSettingsMap.keySet()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String setting = getItem(position);
        String settingName = settingsNameMap.get(setting);
        Boolean isChecked = currentSettingsMap.get(setting);

        if (isChecked == null) {
            currentSettingsMap.put(setting, isChecked = false);
        }

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_settings_management, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.short_name)).setText(settingName);

        CheckBox isFineChecked = (CheckBox) convertView.findViewById(R.id.is_checked);
        isFineChecked.setChecked(isChecked);

        return convertView;
    }

    public boolean inverseAndGetChecked(@NonNull String setting) {
        Boolean isChecked = currentSettingsMap.get(setting);
        // Inversion
        isChecked = isChecked == null || !isChecked;

        currentSettingsMap.put(setting, isChecked);

        notifyDataSetChanged();

        return isChecked;
    }

}
