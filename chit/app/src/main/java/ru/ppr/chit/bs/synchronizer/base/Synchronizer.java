package ru.ppr.chit.bs.synchronizer.base;

import android.support.annotation.Nullable;

import io.reactivex.Completable;


/**
 * Общий интерфейс классов синхронизации
 *
 * @author m.sidorov
 */
public interface Synchronizer<T> {

    enum SynchronizeType {SYNC_GLOBAL, SYNC_SFT, SYNC_SFT_LICENSE, SYNC_SFT_DATA, SYNC_SECURE, SYNC_NSI, SYNC_SOFTWARE, SYNC_TRAININFO, SYNC_TICKETS, SYNC_BOARDING, GARBAGE}

    // тип синхронизации
    SynchronizeType getType();

    // название синхронизатора
    String getTitle();

    // Возвращает менеджер создания/восстановления бакапа
    BackupManager getBackupManager();

    // Возвращает загруженные в методе load() данные
    @Nullable
    T getLoadedData();

    boolean hasLoadedData();

    // запуск синхронизации, получение с сервера новых данных
    Completable load();

    // Сохранение полученных с сервера данных
    Completable apply();

}
