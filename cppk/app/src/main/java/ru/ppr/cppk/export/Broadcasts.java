package ru.ppr.cppk.export;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import ru.ppr.cppk.Globals;
import ru.ppr.logger.Logger;

public class Broadcasts {

    public static final String ArmSyncEventAction = "ArmSyncEventActionFilter";

    private ArrayList<BroadcastEvent> list = new ArrayList<Broadcasts.BroadcastEvent>();

    /**
     * Послать бродкаст о новом событии при синхронизации с АРМ
     */
    public void newArmSyncEvent(Service context, ArmSyncEvents eventType, String message, ru.ppr.cppk.export.Exchange.Error error) {
        newArmSyncEvent(context.getApplicationContext(), new BroadcastEvent(eventType, message, error));
    }

    /**
     * Послать бродкаст о новом событии при синхронизации с АРМ
     */
    public void newArmSyncEvent(Globals context, ArmSyncEvents eventType, String message, ru.ppr.cppk.export.Exchange.Error error) {
        newArmSyncEvent(context.getApplicationContext(), new BroadcastEvent(eventType, message, error));
    }

    /**
     * Послать бродкаст о новом событии при синхронизации с АРМ
     */
    public void newArmSyncEvent(Context context, BroadcastEvent brEvent) {
        if (brEvent != null && brEvent.eventType != ArmSyncEvents.Unknown) {
            Intent broadcast = new Intent();
            broadcast.setAction(ArmSyncEventAction);
            broadcast.setPackage(context.getPackageName());
            broadcast.putExtra("timestamp", brEvent.time);
            broadcast.putExtra("eventTypeCode", brEvent.eventType.getСode());
            broadcast.putExtra("message", brEvent.message);
            broadcast.putExtra("erorrCode", brEvent.error.code);
            broadcast.putExtra("erorrMessage", brEvent.error.getMessage());

            // при подключении чистим предыдущюю историю
            if (brEvent.eventType == ArmSyncEvents.Connected)
                list.clear();
            list.add(brEvent);

            context.sendBroadcast(broadcast);
            String message = "Тип события: " + brEvent.eventType.toString() + " Code:" + brEvent.eventType.getСode() + " -- " +
                    brEvent.eventType.getDescription() + " -- " +
                    brEvent.message + " -- " +
                    "Error: " + brEvent.error.code + " (" + brEvent.error.getMessage() + ")";

            addLog(message);
        }
    }

    /**
     * Вернет список событий с момента последнего подключения
     *
     * @return
     */
    public ArrayList<BroadcastEvent> getEvents() {
        return new ArrayList<BroadcastEvent>(list);
    }

    public static class BroadcastEvent {
        public long time = 0;
        public ArmSyncEvents eventType = null;
        public String message;
        public ru.ppr.cppk.export.Exchange.Error error;

        public BroadcastEvent(ArmSyncEvents eventType, String message, ru.ppr.cppk.export.Exchange.Error error) {
            this.time = System.currentTimeMillis();
            this.message = message;
            this.eventType = eventType;
            this.error = error;
        }
    }

    private void addLog(String text) {
        Logger.trace(Broadcasts.class, text);
    }
}
