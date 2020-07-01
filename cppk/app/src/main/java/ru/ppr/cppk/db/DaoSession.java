package ru.ppr.cppk.db;

import ru.ppr.database.DbOpenHelper;
import ru.ppr.database.Database;

/**
 * Высокоуровневая обертка над  БД.
 * Точка входа в слой для работы БД.
 * Объединяет в себе все мелкие DAO-объекты.
 * Никак не управляет подключением!
 * В случае закрытия соединения с БД через {@link DbOpenHelper}
 * и повторного получения БД через {@link DbOpenHelper#getReadableDatabase()}
 * нужно создавать новый объект {@link DaoSession} на основе {@link Database}
 *
 * @author Aleksandr Brazhkin
 */
public interface DaoSession {

    /**
     * Стуртует транзацкию на БД
     */
    void beginTransaction();

    /**
     * Завершает транзакцию на БД
     */
    void endTransaction();

    /**
     * Помечает транзакцию успешной на БД
     */
    void setTransactionSuccessful();
}
