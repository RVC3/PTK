package ru.ppr.cppk.export;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.google.common.base.Preconditions;

import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.service.ConnectService;
import ru.ppr.cppk.service.ConnectService.ServiceAction;
import ru.ppr.utils.FileUtils;

/**
 * Функции работы с сервисом синхронизации
 *
 * @author G.Kashka
 */
public class ServiceUtils {

    private static final String TAG = Logger.makeLogTag(ServiceUtils.class);

    private static final ServiceUtils INSTANCE = new ServiceUtils();

    private Globals g;
    private IntentReceiver connectReceiver, disconnectReceiver;

    private ServiceUtils() {
        connectReceiver = null;
        disconnectReceiver = null;
    }

    public static ServiceUtils get() {
        return INSTANCE;
    }

    public void init(Globals globals) {
        g = globals;
    }

    private void checkInit() {
        Preconditions.checkNotNull(g, "ServiceUtils not init");
    }

    /**
     * Проверяет подключен ли в данный момент ПТК к зарядке
     */
    public static boolean isConnected(Context context) {
        boolean isConnected = isConnected2(context) || isConnected1(context) || isConnected3(context);
        Logger.trace(TAG, "isConnected(), isConnected = " + isConnected);
        return isConnected;
    }

    /**
     * Проверяет подключен ли в данный момент ПТК к зарядке. Иногда не срабатывает, если ПТК заряжен на 100%
     */
    private static boolean isConnected1(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : -1;
        Logger.trace(TAG, "isConnected1(), plugged = " + plugged);

        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    /**
     * Проверяет подключен ли в данный момент ПТК к зарядке.
     */
    private static boolean isConnected2(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        boolean connected = intent != null && intent.getBooleanExtra("connected", false);
        Logger.trace(TAG, "isConnected2(), connected = " + connected);
        return connected;
    }

    /**
     * Проверяет идёт ли заряд батареи.
     */
    private static boolean isConnected3(Context context) {
        Intent ACTION_BATTERY_CHANGED = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        boolean connected = ACTION_BATTERY_CHANGED != null && ACTION_BATTERY_CHANGED.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
        Logger.trace(TAG, "isConnected3(), connected = " + connected);
        return connected;
    }

    /**
     * Регистрирует слушателя подключения к АРМ
     */
    public void registerPowerDetect(String who) {
        Logger.trace(TAG, "registerPowerDetect(), " + who);
        checkInit();
        unRegConnectReceiver();
        unRegDisconnectReceiver();
        stopARMservice(who);
        boolean isConnected = isConnected(g.getApplicationContext());
        if (isConnected) {
            regDisconnectReceiver();
            startARMservice();

        } else {
            regConnectReceiver();
        }
    }

    private void unRegConnectReceiver() {
        Logger.trace(TAG, "unRegConnectReceiver(), ");
        checkInit();
        if (connectReceiver != null) {
            g.unregisterReceiver(connectReceiver);
            connectReceiver = null;
        }
    }

    private void unRegDisconnectReceiver() {
        Logger.trace(TAG, "unRegDisconnectReceiver(), ");
        checkInit();
        if (disconnectReceiver != null) {
            g.unregisterReceiver(disconnectReceiver);
            disconnectReceiver = null;
        }
    }

    /**
     * Регистрирует слушателя подключения к АРМ
     */
    private void regConnectReceiver() {
        Logger.trace(TAG, "regConnectReceiver()");
        checkInit();
        connectReceiver = new IntentReceiver();
        IntentFilter connectedFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        g.registerReceiver(connectReceiver, connectedFilter);
    }

    /**
     * Регистрирует слушателя отключения от АРМ
     */
    private void regDisconnectReceiver() {
        Logger.trace(TAG, "regDisconnectReceiver()");
        checkInit();
        disconnectReceiver = new IntentReceiver();
        IntentFilter disconnectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        g.registerReceiver(disconnectReceiver, disconnectedFilter);
    }

    /**
     * Запускает сервис синхронизации с АРМ и создает state файлы
     */
    public void startARMservice() {
        Logger.trace(TAG, "startARMservice");
        checkInit();
//        stopARMservice("Остановка сервиса перед запуском");
        Intent sIntent = new Intent(g, ru.ppr.cppk.service.ConnectService.class);
        sIntent.putExtra("action", ServiceAction.START);
        g.startService(sIntent);
    }

    /**
     * Останавливает сервис синхронизации с АРМ, и удаляет state файлы
     */
    public void stopARMservice(String description) {
        Logger.trace(TAG, "stopARMservice, " + description);
        checkInit();
        g.getExchange().stop(description);
        Intent sIntent = new Intent(g, ru.ppr.cppk.service.ConnectService.class);
        g.stopService(sIntent);
        //Globals.getInstance().getResponse().clearStateFolder();
    }

    /**
     * получает событие подключение/отключение питания и запускает/отключает
     * синхронизацию с АРМ
     *
     * @author Grigoriy Kashka
     */
    public class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_POWER_CONNECTED)) {
                Logger.trace(TAG, "ACTION_POWER_CONNECTED " + this);
//				Toaster.showToast(context,  "POWER_CONNECTED");
                unRegConnectReceiver();
                unRegDisconnectReceiver();
                regDisconnectReceiver();

                startARMservice();
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_POWER_DISCONNECTED)) {
                Logger.trace(TAG, "ACTION_POWER_DISCONNECTED " + this);
//				Toaster.showToast(context,  "POWER_DISCONNECTED");
                unRegConnectReceiver();
                unRegDisconnectReceiver();
                regConnectReceiver();
                // очищаем папку Cancel
                FileUtils.clearFolderMtp(g, Exchange.CANCEL);
                stopARMservice("Зафиксировано событие: ACTION_POWER_DISCONNECTED");
            }
        }
    }

    /**
     * Проверяет запущен ли сервис синхронизации с АРМ
     */
    public static boolean isArmServiceRunning(Context context) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(500);
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (ConnectService.class.getName().equals(rsi.service.getClassName())) {
                isRunning = true;
                break;
            }
        }
        Logger.trace(TAG, "isArmServiceRunning, isRunning = " + isRunning);
        return isRunning;
    }

}
