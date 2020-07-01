package ru.ppr.database.base;

/**
 * Общий интерфейс таблицы Dao, отвязанный от конкретного проекта
 *
 * @author m.sidorov
 */
public interface BaseTableDao {

    String getTableName();
    String getPkField();

}
