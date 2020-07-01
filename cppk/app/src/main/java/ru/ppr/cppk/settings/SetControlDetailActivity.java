package ru.ppr.cppk.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SellAndControlFragment;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Activity для окна настроек категории поезда контроля или маршрута трансфера
 */
public class SetControlDetailActivity extends SystemBarActivity {

    private TextView title;
    private PrivateSettings privateSettings;

    public static Intent getNewIntent(Context context) {
        return new Intent(context, SetControlDetailActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_control_detail);
        privateSettings = Dagger.appComponent().privateSettings();

        title = (TextView) findViewById(R.id.title);
        title.setText(privateSettings.isTransferControlMode() ? R.string.settings_control_detail_transfer_route : R.string.settings_control_detail_train_category);

        Fragment sellAndControlFragment = SellAndControlFragment.newInstance(false, true);
        getFragmentManager().beginTransaction().add(R.id.container, sellAndControlFragment).commit();

    }
}
