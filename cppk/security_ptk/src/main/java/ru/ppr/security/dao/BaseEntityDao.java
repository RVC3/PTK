package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * Базовый DAO для таблиц базы безопасности.
 *
 * @param <T> Тип сущности, ассоциированой с таблицей базы безопасности.
 * @param <K> Тип ключа (Primary Key) для таблицы базы безопасности.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseEntityDao<T, K> extends BaseDao {

    /**
     * Общие имена колонок для большинства таблиц НСИ.
     */
    public static class Properties {
    }

    public BaseEntityDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
    }

    /**
     * Возвращает имя таблицы, с которой ассоциирован данный DAO.
     *
     * @return Наименование таблицы
     */
    abstract String getTableName();

    /**
     * Собирает сущность {@link T}, с которой ассоциирован данный DAO.
     *
     * @param cursor Курсор с данными из таблицы
     * @return Сущность {@link T}
     */
    public abstract T fromCursor(Cursor cursor);
}
