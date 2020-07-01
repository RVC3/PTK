package ru.ppr.chit.bs.synchronizer.event;

import ru.ppr.chit.bs.synchronizer.base.Synchronizer;

/**
 * Информационнный event с информацией о выполняемом этапе синхронизации
 *
 * @author m.sidorov
 */

public class SyncInfoEvent implements SyncEvent {

    private String message;
    private Synchronizer.SynchronizeType syncType;



    public SyncInfoEvent(Synchronizer.SynchronizeType syncType, String message){
        this.syncType = syncType;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Synchronizer.SynchronizeType getSyncType() {
        return syncType;
    }

}
