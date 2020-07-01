package ru.ppr.chit.bs.synchronizer.base;

/**
 * Общий интерфейс для создания и восстановления данных из бакапа
 *
 * @author m.sidorov
 */

public interface BackupManager {

    // Создание резервной копии
    void backup() throws SynchronizeException;

    // восстановление резервной копии
    void restore() throws SynchronizeException;

    // Признак, что бакап был сделан
    boolean hasBackup();

}
