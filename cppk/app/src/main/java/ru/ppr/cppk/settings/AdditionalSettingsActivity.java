package ru.ppr.cppk.settings;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogButtonStyle;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogClickListener;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.entity.log.Message;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.AboutPtkFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.DateAndTimeFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.InterfaceFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SellAndControlFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SettingOtherFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SettingsUpdateFragment;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SettingsUpdateFragment.OnFragmentInteraction;
import ru.ppr.cppk.settings.ListViewSettingsAdapter.ViewHolder;
import ru.ppr.cppk.settings.inputs.InputDataActivity;
import ru.ppr.cppk.settings.inputs.InputDataActivity.ChangeAction;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.logger.Logger;

/**
 * Класс дополнительных настроек.
 */
public class AdditionalSettingsActivity extends SystemBarActivity implements OnItemClickListener,
        OnFragmentInteraction, DateAndTimeFragment.OnFragmentInteraction, CppkDialogClickListener {

    private static final int DIALOG_SYNC_ID = 4865785;

    private Fragment currentVisibleFragment = null;
    private View expandedView = null;
    private ListView listView = null;
    private Globals globals;
    private DateAndTimeFragment dateAndTimeFragment;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additional_settings);

        globals = Globals.getInstance();
        privateSettingsHolder = globals.getPrivateSettingsHolder();

        addServiceModeEventToLog(true);

        listView = (ListView) findViewById(R.id.main_menu);
        listView.getRootView().setBackgroundColor(Color.WHITE);

        final SparseArray<Fragment> dataArray = new SparseArray<>();

        Fragment aboutPtkFragment = new AboutPtkFragment();
        dataArray.append(R.string.about_ptk, aboutPtkFragment);

        dateAndTimeFragment = new DateAndTimeFragment();
        dataArray.append(R.string.date_and_time, dateAndTimeFragment);

        Fragment interfaceFragment = new InterfaceFragment();
        dataArray.append(R.string.interfacEE, interfaceFragment);

        Fragment otherFragment = new SettingOtherFragment();
        dataArray.append(R.string.other, otherFragment);

        Fragment sellAndControlFragment = SellAndControlFragment.newInstance(false, false);
        dataArray.append(R.string.sell_and_control, sellAndControlFragment);

        Fragment updateFragment = new SettingsUpdateFragment();
        dataArray.append(R.string.update, updateFragment);

        ListViewSettingsAdapter adapter = new ListViewSettingsAdapter(this, dataArray);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.post(this::openFirst);

        currentVisibleFragment = aboutPtkFragment;
    }

    /**
     * Добавит событие входа/выхода в сервисный режим.
     */
    private void addServiceModeEventToLog(boolean isInService) {
        Logger.info(getClass(), "addServiceModeEventToLog(" + ((isInService) ? "вход в сервисный режим" : "выход из сервисного режима") + ")");
        LogEvent logEventStandard = Dagger.appComponent().logEventBuilder()
                .setLogActionType((isInService) ? LogActionType.SERVICE_MODE_ON : LogActionType.SERVICE_MODE_OFF)
                .setMessage((isInService) ? Message.IN_TO_SETTINGS : Message.OUT_FROM_SETTINGS)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventStandard);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        final Fragment fragment = (Fragment) parent.getItemAtPosition(position);

        if (currentVisibleFragment == null) {
            // раскрываем, сохраняем
            fragmentTransaction.show(fragment).commit();
            expandedView = view;
            open(view);
            currentVisibleFragment = fragment;
        } else if (currentVisibleFragment == fragment) {
            // скрываем, обнуляем
            fragmentTransaction.hide(fragment).commit();
            currentVisibleFragment = null;
            close(view);
        } else {
            // скрываем старый, раскрываем новый, сохраняем в переменную
            fragmentTransaction.hide(currentVisibleFragment).show(fragment).commit();
            currentVisibleFragment = fragment;
            close(expandedView);
            expandedView = view;
            open(view);
        }
    }

    private void openFirst() {
        getFragmentManager().beginTransaction().show(currentVisibleFragment).commit();
        expandedView = listView.getChildAt(0);
        open(expandedView);
    }

    private void close(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.arrowBottom.setVisibility(View.VISIBLE);
        holder.arrowTop.setVisibility(View.GONE);
        holder.textView.setSelected(false);
    }

    private void open(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.arrowBottom.setVisibility(View.GONE);
        holder.arrowTop.setVisibility(View.VISIBLE);
        holder.textView.setSelected(true);
    }

    @Override
    public void onChangeStopListActionDayCount() {
        startActivity(InputDataActivity.getNewIntent(this, ChangeAction.COUNT_STOP_LIST_ACTION_DAY));
    }

    @Override
    public void syncTimeDialog() {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                getString(R.string.syncQuestion),
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_SYNC_ID,
                CppkDialogButtonStyle.HORIZONTAL);

        cppkDialogFragment.show(getFragmentManager(), null);
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int idDialog) {
        switch (idDialog) {
            case DIALOG_SYNC_ID:
                changeSyncTimeEnable();
                break;

            default:
                break;
        }
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int idDialog) {
        /* NOP */
    }

    @Override
    public void changeSyncTimeEnable() {
        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        boolean isTimeSyncEnabled = privateSettings.isTimeSyncEnabled();
        isTimeSyncEnabled = !isTimeSyncEnabled;
        privateSettings.setTimeSyncEnabled(isTimeSyncEnabled);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        setTimeSyncEnabled(isTimeSyncEnabled);

        int flag = isTimeSyncEnabled ? 1 : 0;
        Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, flag);
        Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, flag);
    }

    private void setTimeSyncEnabled(boolean isTimeSyncEnabled) {
        if (dateAndTimeFragment != null) {
            dateAndTimeFragment.setTimeSyncEnabled(isTimeSyncEnabled);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addServiceModeEventToLog(false);
    }
}
