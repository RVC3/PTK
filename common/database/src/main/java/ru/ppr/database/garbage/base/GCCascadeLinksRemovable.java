package ru.ppr.database.garbage.base;

import ru.ppr.database.Database;

/**
 * Интерфейс, который должен реализовываться в таблицах,
 * поддерживающих удаление каскадных ссылок
 *
 * @author m.sidorov
 */
public interface GCCascadeLinksRemovable extends GCDeletedMarkSupported {

    /**
     * Вызывается перед удалением записей по каскадным ссылкам
     * @param referenceTable имя таблицы из каскадной ссылки
     * @param referenceField имя ссылочного поля из каскадной ссылки
     *
     * @return возвращает флаг необходимости дальнейшей обработки ссылки,
     * Если true, то значит класс сам реализовал удаление данных по каскадной ссылке и сборщик мусора не будет дальше обрабатывать эту ссылку
     * Если false, то сборщик мусора применит к этой ссылке свой стандартный алгоритм обработки удаления данных
     */
    boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField);

}
