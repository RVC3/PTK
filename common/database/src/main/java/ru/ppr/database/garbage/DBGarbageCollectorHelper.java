package ru.ppr.database.garbage;

import java.util.ArrayList;
import java.util.Collection;

import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.database.references.References;

/**
 * Вспомогательный класс, с функциями генерации sql для корректного удаления данных в таблицах
 *
 * @author m.sidorov
 */
public class DBGarbageCollectorHelper {

    /**
     * Возвращает sql, который удаляет записи в ссылочной таблице ri.getReferenceField(),
     * которые ссылаются на удаляемые записи в мастер таблице ri.getMasterTable()
     *
     * @param ri - Информация о ссылке
     * @param masterDeletedMarkField - Имя поля маркера удаленных записей из мастер таблицы ("deletedMark")
     *
     * @result - sql запрос, удаляющий записи, ссылающиеся на удаляемые в мастер таблице
     */
    public static String getDeleteCascadeSql(ReferenceInfo ri, String masterDeletedMarkField){
        StringBuilder sql = new StringBuilder();
        sql
                .append("delete from " ).append(ri.getReferenceTable()).append("\n")
                .append("where ").append(ri.getReferenceField()).append(" in ( ")
                .append("select M.").append(ri.getMasterField()).append(" from ").append(ri.getMasterTable()).append(" M ")
                .append("where M.").append(masterDeletedMarkField).append(" = 1").append(" )");

        return sql.toString();
    }

    /**
     * Возвращает sql, который помечает на удаление в ссылочной таблице ri.getReferenceField() записи,
     * которые ссылаются на удаляемые записи в мастер таблице ri.getMasterTable()
     *
     * @param ri - Информация о ссылке
     * @param masterDeletedMarkField - Имя поля маркера удаленных записей из мастер таблицы ("deletedMark")
     * @param referenceDeletedMarkField - Имя поля маркера удаленных записей из удаляемой таблицы ("deletedMark")
     *
     * @result - sql запрос, помечающий удаляемые записи
     */
    public static String getMarkDeletedCascadeSql(ReferenceInfo ri, String masterDeletedMarkField, String referenceDeletedMarkField){
        StringBuilder sql = new StringBuilder();
        sql
                .append("update " ).append(ri.getReferenceTable()).append(" set ").append(referenceDeletedMarkField).append(" = 1").append("\n")
                .append("where ").append(ri.getReferenceField()).append(" in ( ")
                .append("select M.").append(ri.getMasterField()).append(" from ").append(ri.getMasterTable()).append(" M ")
                .append("where M.").append(masterDeletedMarkField).append(" = 1").append(" )");

        return sql.toString();
    }

    /**
     * Возвращает sql, который удаляет для таблицы записи, на которые никто не ссылается
     * Обрабатываются ссылки по ВСЕМ таблицам
     *
     * @param references - Класс, содержащий метаинформацию по всем ссылкам базы данных
     * @param table - Имя таблицы, в которой удаляются данные
     *
     * @result - sql запрос, удаляющий записи, на которые нет ссылок
     */
    public static String getDeleteNoLinkSql(References references, String table){
        Collection<ReferenceInfo> links = references.getChilds(table);
        return getDeleteNoLinkSql(links, table);
    }

    /**
     * Возвращает sql, который удаляет для таблицы записи, на которые не ссылается ни одна запись в указанной ссылке
     * Обрабатывается только ОДНА ссылка
     *
     * @param childLink - Ссылка, по которой проверяется наличие ссылочных записей
     * @param table - Имя таблицы, в которой удаляются данные
     *
     * @result - sql запрос, удаляющий записи, на которые нет ссылок
     */
    public static String getDeleteNoLinkSql(ReferenceInfo childLink, String table){
        Collection<ReferenceInfo> childLinks = new ArrayList<>();
        childLinks.add(childLink);
        return getDeleteNoLinkSql(childLinks, table);
    }

    /**
     * Возвращает sql, который удаляет для таблицы записи, на которые не ссылается ни одна запись в переданных ссылках
     * Обрабатывается только переданные ссылки
     *
     * @param childLinks - Список ссылок, в которых проверяется наличие ссылочных записей
     * @param table - Имя таблицы, в которой удаляются данные
     *
     * @result - sql запрос, удаляющий записи, на которые нет ссылок
     */
    public static String getDeleteNoLinkSql(Collection<ReferenceInfo> childLinks, String table){
        if (childLinks.isEmpty()){
            throw new RuntimeException(DBGarbageCollectorHelper.class.getSimpleName() + ".getDeleteNoLinkSql() - table not contain references");
        }

        StringBuilder sql = new StringBuilder();
        sql
                .append("delete from ").append(table).append(" where 0 = 0").append("\n");

        // Формируем список условий с проверками на exists
        for (ReferenceInfo ri : childLinks){
            String masterFieldAlis = table + "." + ri.getMasterField();
            String referenceFieldAlias = ri.getReferenceTable() + "." + ri.getReferenceField();
            sql
                    .append("    and not exists( select ").append(referenceFieldAlias).append(" from ").append(ri.getReferenceTable())
                    .append(" where ").append(referenceFieldAlias).append(" = ").append(masterFieldAlis).append(" )").append("\n");
        }

        return sql.toString();
    }

    /**
     * Возвращает sql, который удаляет для таблицы записи, ссылающиеся на несуществующие записи мастер таблицы
     *
     * @param referenceInfo - Ссылка, на мастер таблицу, в которой проверяются отсутсвующие ссылки
     * @param table - Имя таблицы, в которой удаляются данные
     *
     * @result - sql запрос, удаляющий записи, которые ссылаются на несуществующие записи
     */
    public static String getDeleteNoMasterLinkSql(ReferenceInfo referenceInfo, String table){
        String masterFieldAlis = referenceInfo.getMasterTable() + "." + referenceInfo.getMasterField();
        String referenceFieldAlias = table + "." + referenceInfo.getReferenceField();

        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(table).append(" where not exists( ").append("\n")
                .append("    select ").append(masterFieldAlis).append(" from ").append(referenceInfo.getMasterTable())
                .append(" where ").append(masterFieldAlis).append(" = ").append(referenceFieldAlias).append("\n")
                .append(" )");

        return sql.toString();
    }

}
