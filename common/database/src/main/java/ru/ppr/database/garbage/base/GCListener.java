package ru.ppr.database.garbage.base;

/**
 * Обработчик событий сборщика мусора в базе данных
 *
 * @author m.sidorov
 */
public interface GCListener {
    void onProgress(String message);
}
