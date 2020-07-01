package ru.ppr.cppk.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.log.Message;
import ru.ppr.cppk.export.ArmSyncEvents;
import ru.ppr.cppk.export.Broadcasts;
import ru.ppr.cppk.export.Broadcasts.BroadcastEvent;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.export.Exchange.Error;
import ru.ppr.cppk.export.ServiceUtils;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;

/**
 * Активити для отображения состояния подключения к ARM
 *
 * @author Brazhkin A.V.
 */
public class ArmConnectedStateActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(ArmConnectedStateActivity.class);

    // EXTRAS
    private static final String EXTRA_FLAG_KILL = "EXTRA_FLAG_KILL";

    public static Intent getCallingIntent(Context context, boolean flagKill) {
        Intent intent = new Intent(context, ArmConnectedStateActivity.class);
        intent.putExtra(EXTRA_FLAG_KILL, flagKill);
        return intent;
    }

    private LinearLayout logLayout;
    private ScrollView logScrollView;
    private Button finishSyncBtn;
    private IntentFilter connectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
    private IntentFilter armEventFilter = new IntentFilter(Broadcasts.ArmSyncEventAction);
    private Globals g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_connected_state);
        //запретим блокировку этого экрана
        denyScreenLock();
        // выключаем автозакрытие смены на данном экране, см. http://agile.srvdev.ru/browse/CPPKPP-35028
        resetRegisterReceiver();
        //Выключим проверку разрешений нахождения на данном экране
        disableCheckForScreen();
        g = (Globals) getApplication();

        findViews();

        registerReceiver(connectReceiver, connectedFilter);
        registerReceiver(armEventReceiver, armEventFilter);

        if (hasFlagKill(getIntent())) {
            Navigator.navigateToSplashActivity(this, false);
            finish();
            return;
        }

        addServiceModeEventToLog(true);

        LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.SYNCHRONISATION_WITH_ARM_START)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
    }

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //startSplash();
        }
    };

    private BroadcastReceiver armEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long time = intent.getLongExtra("timestamp", System.currentTimeMillis());
            ArmSyncEvents eventType = ArmSyncEvents.getTypeByCode(intent.getIntExtra("eventTypeCode", ArmSyncEvents.Unknown.getСode()));
            String message = intent.getStringExtra("message");
            Error error = new Error(intent.getIntExtra("erorrCode", Error.UNKNOWN), intent.getStringExtra("erorrMessage"));
            addEventToLog(time, eventType, message, error);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!ServiceUtils.isConnected(getApplicationContext())) {
            g.setIsSyncNow(false);
            Navigator.navigateToSplashActivity(this, false);
            finish();
        } else {
            //на этом экране нам RFID точно не понадобится - грохнем его
            SchedulersCPPK.rfidExecutorService().execute(() -> {
                IRfid iRfid = Dagger.appComponent().rfid();
                iRfid.close();
            });
            logLayout.removeAllViews();
            // при создании активити вручную добавим событие в лог
            ArrayList<BroadcastEvent> events = g.getBroadcasts().getEvents();
            for (BroadcastEvent event : events)
                addEventToLog(event.time, event.eventType, event.message, event.error);
            g.setIsSyncNow(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addServiceModeEventToLog(false);
        unregisterReceiver(connectReceiver);
        unregisterReceiver(armEventReceiver);
    }

    private void findViews() {
        finishSyncBtn = (Button) findViewById(R.id.finishSyncBtn);
        logScrollView = (ScrollView) findViewById(R.id.logScrollView);
        logLayout = (LinearLayout) findViewById(R.id.logLayout);
        finishSyncBtn.setOnClickListener(v -> {
            SimpleDialog simpleDialog = SimpleDialog.newInstance(
                    null,
                    getString(R.string.realyFinishSync),
                    getString(android.R.string.yes),
                    getString(android.R.string.no),
                    LinearLayout.HORIZONTAL,
                    -1
            );
            simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                // Пока касса это не обрабатывает, уберем
                // При желании раскомментировать учесеть следующий сценарий:
                // 1. Идет процесс синхронизации, мы готовим ответ на transmissoncompleted.info
                // 1.1 Очень сильно и долго дерагется sft в потоке ЕЦП (pullSft)
                // 2. Запускаем создание файла syncCanceled.info, который нужно подписать
                // 2.1 Создание подписи встает в очередь и долго ждет, пока отработает п 1.1
                // 2.2 UI Повис
                // 2.2.1 Вряд ли стоит просто выбрасывать эту задачу в фон, потому что неизвестно в какой неудобный момент потом мы вдруг словим syncCanceled.info
                //Globals.getInstance().getResponse().createSyncCancelledInfo();
                Globals.getInstance().getExchange().setExchangeState(Exchange.EXCHANGE_STATE_WAIT_CONNECT);
                startSplash();
            });
            simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClickSettings() {

    }

    @Override
    public void onClickBarcode() {

    }

    @Override
    public void onClickRfrid() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.trace(ArmConnectedStateActivity.class, "get new intent");
        if (hasFlagKill(intent)) {
            Logger.trace(ArmConnectedStateActivity.class, "kill ArmConnectedStateActivity");
            startSplash();
        }
    }

    public boolean hasFlagKill(Intent intent) {
        Bundle extras = intent.getExtras();
        return extras != null && extras.getBoolean(EXTRA_FLAG_KILL, false);
    }

    private void startSplash() {
        g.setIsSyncNow(false);

        LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.SYNCHRONISATION_WITH_ARM_END)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
        LogEvent logEventService = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.SERVICE_MODE_OFF)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventService);

        Navigator.navigateToSplashActivity(this, false);

        finish();
    }

    private void addEventToLog(long timestamp, ArmSyncEvents eventType, String message, Error error) {
        String log = (new SimpleDateFormat("HH:mm:ss.SSS").format(timestamp)) + " - ";
        log = log + eventType.getDescription() + ": ";
        if (!TextUtils.isEmpty(message))
            log = log + "\"" + message + "\" - ";
        if (error.isError()) {
            log = log + "\n" + "Error code=" + error.code + " \"" + error.getMessage() + "\"" + ": ";
        } else {
            log = log + "OK";
        }

        addLog(log);

        logActivitysStack();
    }

    private void addLog(String text) {
        if (logLayout.getChildCount() > 0) {
            TextView twLine = new TextView(getApplicationContext());
            twLine.setText("----------------------------------------------------------------------------------------");
            twLine.setSingleLine();
            logLayout.addView(twLine);
        }

        TextView tw = new TextView(getApplicationContext());
        tw.setText(text);
        logLayout.addView(tw);
        logScrollView.post(() -> logScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    /**
     * Добавит событие входа/выхода в сервисный рижим
     */
    private void addServiceModeEventToLog(boolean isInService) {
        Logger.info(getClass(), "addServiceModeEventToLog(" + ((isInService) ? "вход в сервисный режим" : "выход из сервисного режима") + ")");
        LogEvent logEventStandard = Dagger.appComponent().logEventBuilder()
                .setLogActionType((isInService) ? LogActionType.SERVICE_MODE_ON : LogActionType.SERVICE_MODE_OFF)
                .setMessage((isInService) ? Message.SYNC_START : Message.SYNC_FINISH)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventStandard);
    }

}
