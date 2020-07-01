package ru.ppr.chit.bs.synchronizer.event;

import ru.ppr.chit.bs.synchronizer.base.Synchronizer;

/**
 * event с информацией о ходе выполнения синхронизации
 *
 * @author m.sidorov
 */

public interface SyncEvent {
    // Текстовое сообщение о ходе выполнения синхронизвации
    String getMessage();

    // Тип синхронизации
    Synchronizer.SynchronizeType getSyncType();

}
