package ru.ppr.cppk.ui.activity.base.settings;

import android.support.annotation.NonNull;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.adapter.SettingsManagementAdapter;

/**
 * @author Dmitry Nevolin
 */
public abstract class SettingsManagementActivity extends LoggedActivity {

    private SettingsManagementAdapter adapter;
    private FeedbackProgressDialog applySettingsProgress;

    @NonNull
    protected abstract Map<String, Boolean> providedInitialSettingsMap();

    @NonNull
    protected abstract Map<String, String> providedSettingsNameMap();

    protected abstract void onSettingChanged(@NonNull String name, @NonNull Boolean value);

    @NonNull
    protected abstract String providedSettingsTitle();

    protected void initialize() {
        adapter = new SettingsManagementAdapter(this, providedInitialSettingsMap(), providedSettingsNameMap());

        applySettingsProgress = new FeedbackProgressDialog(this);
        applySettingsProgress.setMessage(getString(R.string.settings_management_apply_settings));
        applySettingsProgress.setCancelable(false);
        applySettingsProgress.setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.settings_title)).setText(providedSettingsTitle());

        ListView fineListView = (ListView) findViewById(R.id.settings_list_view);
        fineListView.setAdapter(adapter);
        fineListView.setOnItemClickListener((parent, view, position, id) -> {
            int updatedPosition = position - fineListView.getHeaderViewsCount();
            String setting = adapter.getItem(updatedPosition);
            boolean isChecked = adapter.inverseAndGetChecked(setting);

            onSettingChanged(setting, isChecked);
        });
    }

    @Override
    public void onBackPressed() {
        applySettingsAndExit();
    }

    @Override
    protected void onDestroy() {
        if (applySettingsProgress.isShowing()) {
            applySettingsProgress.dismiss();
        }

        super.onDestroy();
    }

    protected void showApplySettingsProgress() {
        applySettingsProgress.show();
    }

    protected abstract void applySettingsAndExit();

}
