package ru.ppr.cppk.backup;

import android.util.Pair;

/**
 * Класс-помощник для восстановления бекапа БД.
 *
 * @author Grigoriy Kashka
 */
public interface DbBackupRestorer {
    /**
     * Запускает восстановление резервной копии БД.
     *
     * @return Флаг успешности выполнения операции + путь к восстановленному файлу бекапа
     */
    Pair<Boolean, String> start();
}