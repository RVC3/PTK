package ru.ppr.cppk.backup;

import android.util.Pair;

/**
 * Класс-помощник для создания бекапа БД.
 *
 * @author Grigoriy Kashka
 */
public interface DbBackupCreator {
    /**
     * Запускает создание резервной копии БД.
     *
     * @return Флаг успешности выполнения операции + путь к созданному файлу бекапа
     */
    Pair<Boolean, String> start();
}
