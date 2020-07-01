package ru.ppr.cppk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.entity.log.Message;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Created by Dmitry Nevolin on 07.04.2016.
 */
public class DevicesActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(DevicesActivity.class);

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, DevicesActivity.class);
        return intent;
    }

    private Button printerBtn;
    private Button posTerminalBtn;
    private Button informationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        addServiceModeEventToLog(true);

        printerBtn = (Button) findViewById(R.id.printerBtn);
        posTerminalBtn = (Button) findViewById(R.id.posTerminalBtn);
        informationBtn = (Button) findViewById(R.id.informationBtn);

        printerBtn.setOnClickListener(v -> Navigator.navigateToSettingsPrinterActivity(this, null, -1));
        //настраиваем доступ к настройкам принтера
        printerBtn.setEnabled(di().permissionChecker().checkPermission(PermissionDvc.ConfigFiscalRegister));

        posTerminalBtn.setOnClickListener(v -> Navigator.navigateToSettingsPosTerminalActivity(this));
        //настраиваем доступ к настройкам Pos-терминала
        posTerminalBtn.setEnabled(isPosTerminalEnabled());

        informationBtn.setOnClickListener(v -> Navigator.navigateToDevicesInformationActivity(this));
    }

    /**
     * Делает проверку на доступность раздела PosTerminal
     *
     * @return
     */
    private boolean isPosTerminalEnabled() {
        if (di().getShiftManager().isShiftOpened()) return false;
        if (!di().permissionChecker().checkPermission(PermissionDvc.ConfigPosTerminal))
            return false;
        if (!di().getPrivateSettings().get().isPosEnabled()) return false;
        return true;
    }

    /**
     * Добавит событие входа/выхода в сервисный рижим
     */
    private void addServiceModeEventToLog(boolean isInService) {
        Logger.info(getClass(), "addServiceModeEventToLog(" + ((isInService) ? "вход в сервисный режим" : "выход из сервисного режима") + ")");
        LogEvent logEventStandard = Dagger.appComponent().logEventBuilder()
                .setLogActionType((isInService) ? LogActionType.SERVICE_MODE_ON : LogActionType.SERVICE_MODE_OFF)
                .setMessage((isInService) ? Message.IN_TO_DEVICES : Message.OUT_FROM_DEVICES)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventStandard);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addServiceModeEventToLog(false);
    }

}
