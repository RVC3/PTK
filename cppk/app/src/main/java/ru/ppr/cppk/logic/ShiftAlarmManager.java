package ru.ppr.cppk.logic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;

/**
 * Created by Александр on 27.09.2016.
 */
//@LoggerAspect.IncludeClass
public class ShiftAlarmManager {

    private static final String TAG = Logger.makeLogTag(ShiftAlarmManager.class);

    private static final String ACTION_ALARM_ATTENTION_DIALOG = "ACTION_ALARM_ATTENTION_DIALOG";
    private static final String ACTION_ALARM_CLOSE_SHIFT = "ACTION_ALARM_CLOSE_SHIFT";
    private static final String ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG = "ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG";

    private static final String EXTRA_UID = "EXTRA_UID";

    private static final int ALARM_TYPE_DEFAULT = -1;
    private static final int ALARM_TYPE_ATTENTION_DIALOG = 1;
    private static final int ALARM_TYPE_CLOSE_SHIFT = 2;
    private static final int ALARM_TYPE_CRITICAL_NSI_CLOSE_DIALOG = 4;

    private final AlarmManager alarmManager;
    private final Context mContext;

    private PendingIntent attentionDialogPendingIntent;
    private PendingIntent closeShiftPendingIntent;
    private PendingIntent criticalNsiCloseDialogPendingIntent;
    private int actualAlarmType = ALARM_TYPE_DEFAULT;
    private final Set<AlarmListener> alarmListeners = new HashSet<>();
    private final NsiVersionManager nsiVersionManager;
    private final CriticalNsiChecker criticalNsiChecker;

    public ShiftAlarmManager(Context context, NsiVersionManager nsiVersionManager, CriticalNsiChecker criticalNsiChecker) {
        mContext = context.getApplicationContext();
        alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        attentionDialogPendingIntent = getPendingIntent(ACTION_ALARM_ATTENTION_DIALOG, PendingIntent.FLAG_NO_CREATE);
        Logger.trace(TAG, "attentionDialogPendingIntent при старте = " + attentionDialogPendingIntent);
        closeShiftPendingIntent = getPendingIntent(ACTION_ALARM_CLOSE_SHIFT, PendingIntent.FLAG_NO_CREATE);
        Logger.trace(TAG, "closeShiftPendingIntent при старте = " + closeShiftPendingIntent);
        criticalNsiCloseDialogPendingIntent = getPendingIntent(ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG, PendingIntent.FLAG_NO_CREATE);
        Logger.trace(TAG, "criticalNsiCloseDialogPendingIntent при старте = " + criticalNsiCloseDialogPendingIntent);
        mContext.registerReceiver(wakefulBroadcastReceiver, new IntentFilter(ACTION_ALARM_ATTENTION_DIALOG));
        mContext.registerReceiver(wakefulBroadcastReceiver, new IntentFilter(ACTION_ALARM_CLOSE_SHIFT));
        mContext.registerReceiver(wakefulBroadcastReceiver, new IntentFilter(ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG));
        this.nsiVersionManager = nsiVersionManager;
        this.criticalNsiChecker = criticalNsiChecker;
    }

    public void destroy() {
        mContext.unregisterReceiver(wakefulBroadcastReceiver);
    }

    private PendingIntent getPendingIntent(String action, int flags) {
        Intent intent = new Intent(action);
        String uid = UUID.randomUUID().toString();
        Logger.trace(TAG, "getPendingIntent, UID = " + uid);
        intent.putExtra(EXTRA_UID, uid);

        return PendingIntent.getBroadcast(mContext, 0, intent, flags);
    }

    public void startAlarmsForShift(Date shiftStartTime, int attentionDialogTimeIntervalInMinutes) {
        if (!GlobalConstants.ENABLE_AUTO_CLOSE_SHIFT) return;

        Calendar currentTime = Calendar.getInstance();
        Calendar shiftCloseTimeLocal = Calendar.getInstance();

        Date criticalNsiChangeDate = nsiVersionManager.getCriticalNsiChangeDate();

        if (criticalNsiChangeDate != null) {
            shiftCloseTimeLocal.setTime(criticalNsiChangeDate);

            if (criticalNsiChecker.checkCriticalNsiCloseDialogShouldBeShown()) {
                Logger.trace(TAG,
                        "currentTime is after shiftCloseTimeLocal: " +
                                DateFormatOperations.getDateddMMyyyyHHmmss(currentTime.getTime()) +
                                " is after " +
                                DateFormatOperations.getDateddMMyyyyHHmmss(shiftCloseTimeLocal.getTime()) +
                                " so call onCriticalNsiAttentionDialogShouldBeShown");
                // показываем диалог-уведомление о закрытии смены
                startCriticalNsiCloseDialogAlarm(criticalNsiChangeDate);
                notifyAllListenersWithActualAlarmType();
                return;
            }

            startCriticalNsiCloseDialogAlarm(criticalNsiChangeDate);
        }

        shiftCloseTimeLocal.setTime(shiftStartTime);
        shiftCloseTimeLocal.add(Calendar.HOUR_OF_DAY, 24);

        //Сохарним время в которое смена должна быть закрыта
        Date closeShiftTime = shiftCloseTimeLocal.getTime();

        if (currentTime.after(shiftCloseTimeLocal)) {
            Logger.trace(TAG,
                    "currentTime is after shiftCloseTimeLocal: " +
                            DateFormatOperations.getDateddMMyyyyHHmmss(currentTime.getTime()) +
                            " is after " +
                            DateFormatOperations.getDateddMMyyyyHHmmss(shiftCloseTimeLocal.getTime()) +
                            " so call sendBroadcastForCloseShift");
            // закрываем смену, т.к. 24 часа уже прошло
            actualAlarmType = ALARM_TYPE_CLOSE_SHIFT;
            notifyAllListenersWithActualAlarmType();
            return;
        }

        shiftCloseTimeLocal.add(Calendar.MINUTE, attentionDialogTimeIntervalInMinutes * -1);

        //Сохарним время в которое нужно показать диалог
        Date attentionDialogTime = shiftCloseTimeLocal.getTime();

        if (currentTime.after(shiftCloseTimeLocal)) {
            Logger.trace(TAG,
                    "(currentTime - 5m) is after shiftCloseTimeLocal: " +
                            DateFormatOperations.getDateddMMyyyyHHmmss(currentTime.getTime()) +
                            " is after " +
                            DateFormatOperations.getDateddMMyyyyHHmmss(shiftCloseTimeLocal.getTime()) +
                            " so call sendBroadcastForAttentionClosheShift");
            // показываем предупреждение о скором закрытии смены
            actualAlarmType = ALARM_TYPE_ATTENTION_DIALOG;
            startShiftCloseAlarm(closeShiftTime);
            notifyAllListenersWithActualAlarmType();
            return;
        }

        Logger.trace(TAG, "nothing happened, just call startShiftWarningAlarm");
        startShiftCloseAlarm(closeShiftTime);
        startAttentionDialogAlarm(attentionDialogTime);
    }

    public void stopAlarms() {
        actualAlarmType = ALARM_TYPE_DEFAULT;

        if (attentionDialogPendingIntent != null) {
            alarmManager.cancel(attentionDialogPendingIntent);
            Logger.trace(TAG, "attentionDialogPendingIntent отменен");
            attentionDialogPendingIntent = null;
        }
        if (closeShiftPendingIntent != null) {
            alarmManager.cancel(closeShiftPendingIntent);
            Logger.trace(TAG, "closeShiftPendingIntent отменен");
            closeShiftPendingIntent = null;
        }
        if (criticalNsiCloseDialogPendingIntent != null) {
            alarmManager.cancel(criticalNsiCloseDialogPendingIntent);
            Logger.trace(TAG, "criticalNsiCloseDialogPendingIntent отменен");
            criticalNsiCloseDialogPendingIntent = null;
        }
    }

    public void startAttentionDialogAlarm(Date date) {
        attentionDialogPendingIntent = getPendingIntent(ACTION_ALARM_ATTENTION_DIALOG, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC, date.getTime(), attentionDialogPendingIntent);
    }

    public void startShiftCloseAlarm(Date date) {
        closeShiftPendingIntent = getPendingIntent(ACTION_ALARM_CLOSE_SHIFT, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC, date.getTime(), closeShiftPendingIntent);
    }

    public void startCriticalNsiCloseDialogAlarm(Date date) {
        criticalNsiCloseDialogPendingIntent = getPendingIntent(ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC, date.getTime(), criticalNsiCloseDialogPendingIntent);
    }

    public void addAlarmListener(AlarmListener alarmListener) {
        alarmListeners.add(alarmListener);
        notifyWithActualAlarmType(alarmListener);
    }

    public void removeAlarmListener(AlarmListener alarmListener) {
        alarmListeners.remove(alarmListener);
    }

    private void notifyWithActualAlarmType(AlarmListener alarmListener) {
        switch (actualAlarmType) {
            case ALARM_TYPE_ATTENTION_DIALOG: {
                alarmListener.onAttentionDialogShouldBeShown();
                break;
            }
            case ALARM_TYPE_CLOSE_SHIFT: {
                alarmListener.onShiftShouldBeClosedNow();
                break;
            }
            case ALARM_TYPE_CRITICAL_NSI_CLOSE_DIALOG: {
                alarmListener.onCriticalNsiCloseDialogShouldBeShown();
                break;
            }
            default: {
                /* NOP */
            }
        }
    }

    private void notifyAllListenersWithActualAlarmType() {
        for (AlarmListener alarmListener : alarmListeners) {
            notifyWithActualAlarmType(alarmListener);
        }
    }

    public void markActualActionAsHandled() {
        actualAlarmType = -1;
    }

    private WakefulBroadcastReceiver wakefulBroadcastReceiver = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Logger.trace(TAG, "Receive alarm with action - " + action);

            Bundle extras = intent.getExtras();
            if (extras != null) {
                Logger.trace(TAG, "UID = " + extras.getString(EXTRA_UID));
            }

            int alarmType;
            if (ACTION_ALARM_ATTENTION_DIALOG.equals(action)) {
                alarmType = ALARM_TYPE_ATTENTION_DIALOG;
                attentionDialogPendingIntent = null;
            } else if (ACTION_ALARM_CLOSE_SHIFT.equals(action)) {
                alarmType = ALARM_TYPE_CLOSE_SHIFT;
                closeShiftPendingIntent = null;
            } else if (ACTION_ALARM_CRITICAL_NSI_CLOSE_DIALOG.equals(action)) {
                alarmType = ALARM_TYPE_CRITICAL_NSI_CLOSE_DIALOG;
                criticalNsiCloseDialogPendingIntent = null;
            } else {
                throw new IllegalArgumentException("Action " + action + " is not supported");
            }

            Logger.trace(TAG, "New Alarm (alarmType = " + alarmType + "), actualAlarmType = " + actualAlarmType);
            actualAlarmType = alarmType;
            notifyAllListenersWithActualAlarmType();
        }
    };

    public interface AlarmListener {
        void onAttentionDialogShouldBeShown();

        void onShiftShouldBeClosedNow();

        void onCriticalNsiCloseDialogShouldBeShown();
    }
}
