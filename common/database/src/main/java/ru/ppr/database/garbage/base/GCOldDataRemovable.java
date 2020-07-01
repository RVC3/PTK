package ru.ppr.database.garbage.base;

import java.util.Date;

import ru.ppr.database.Database;

/**
 * Интерфейс, который должен реализовываться в таблицах,
 * поддерживающих удаление старых данных по дате (удаляются данные, созданные ранее заданной даты)
 *
 * @author m.sidorov
 */
public interface GCOldDataRemovable {

    /**
     * Вызывается при удалении старых записей сброщиком мусора
     * @param dateBefore Удаляются данные, раньше этой даты
     *
     */
    void gcRemoveOldData(Database database, Date dateBefore);

}
