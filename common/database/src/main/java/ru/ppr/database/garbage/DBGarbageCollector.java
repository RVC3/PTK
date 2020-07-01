package ru.ppr.database.garbage;

import android.content.Context;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import ru.ppr.database.Database;
import ru.ppr.database.R;
import ru.ppr.database.base.BaseTableDao;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.garbage.base.GCDeletedMarkSupported;
import ru.ppr.database.garbage.base.GCListener;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.database.references.References;
import ru.ppr.logger.Logger;

/**
 * Класс сборщика мусора и удаления не актуальных данных из БД
 *
 * @author m.sidorov
 */
public class DBGarbageCollector <T extends BaseTableDao> {

    public static final String TAG = DBGarbageCollector.class.getSimpleName();

    private final Context context;
    private final Database database;
    private final Map<String, T> entities;
    private final  References references;

    private final GCListener listener;

    public DBGarbageCollector(Context context, Database database, Map<String, T> entities, References references, GCListener listener){
        this.context = context;
        this.database = database;
        this.entities = entities;
        this.references = references;
        this.listener = listener;
    }

    // Удаляет из базы данных мусор и данные, созданные раньше даты dateBefore
    public void execute(Date dateBefore){
        database.beginTransaction();
        try {
            Logger.info(TAG, "Database garbage process: started..........");

            // Первым проходом обрабатываем таблицы, которые поддерживают удаление старых данных по дате
            deleteTablesByDate(dateBefore);

            // вторым проходом обрабатываем удаление записей по каскадным ссылкам (ссылки на попомеченные для удаления записи из первого прохода)
            deleteCascadeLinks();

            // третьим проходом удаляем помеченные на удаление записи
            deleteMarkedRecords();

            // последней итерацией выполняем сборку мусора (удаление записей, на которые никто не ссылается)
            deleteNoLinks();

            database.setTransactionSuccessful();

            Logger.info(TAG, "Database garbage process: finished..........");
        } finally {
            database.endTransaction();
        }
    }


    private void processInfo(String message){
        listener.onProgress(message);
    }

    // Обрабатывает таблицы, которые поддерживают удаление по дате (по сути основные таблицы, с которых все начинается)
    private void deleteTablesByDate(Date dateBefore) {
        Logger.info(TAG, "start deleting records by date..........");
        for (BaseTableDao table : entities.values()) {
            if (table instanceof GCOldDataRemovable){

                Logger.info(TAG, String.format("%s.gcRemoveOldData()", table.getClass().getSimpleName()));
                processInfo(context.getString(R.string.gc_delete_old_data_table) + " " + table.getTableName());

                try {
                    ((GCOldDataRemovable) table).gcRemoveOldData(database, dateBefore);
                }catch (Exception e){
                    Logger.error(TAG, "deleteTablesByDate(): Error of method " + table.getClass().getSimpleName() + ".gcRemoveOldData() ", e);
                }
            }
        }
    }

    // Физически удаляет записи, помеченные на удаление
    private void deleteMarkedRecords(){
        Logger.info(TAG, "start deleting marker record..........");
        for (BaseTableDao table : entities.values()) {
            if (table instanceof GCDeletedMarkSupported){

                Logger.info(TAG, String.format("deleteMarkedRecords for table %s", table.getClass().getSimpleName()));
                processInfo(context.getString(R.string.gc_delete_old_data_table) + " " + table.getTableName());

                execSQL("delete from " + table.getTableName() + " where " + ((GCDeletedMarkSupported)table).getDeletedMarkField() + " = 1");
            }
        }
    }

    // Обрабатываем каскадные ссылки для основных таблиц (которые удаляются по дате)
    // удаление каскадных ссылок выполняется вторым проходом, после обработки удаления основных таблиц по дате
    private void deleteCascadeLinks(){
        Logger.info(TAG, "start deleting cascade links..........");
        for (BaseTableDao table : entities.values()) {
            if (table instanceof GCOldDataRemovable) {
                deleteCascadeLinks(table);
            }
        }
    }

    // Обпрабатывает таблицы, реализующие способ удаления в случае остутствия ссылок
    private void deleteNoLinks(){
        Logger.info(TAG, "start deleting no links records..........");
        for (BaseTableDao table : entities.values()) {
            if (table instanceof GCNoLinkRemovable) {

                Logger.info(TAG, "delete no links records for table:" + table.getTableName());
                processInfo(context.getString(R.string.gc_delete_old_data_table) + " " + table.getTableName());

                // Вызываем обработку реализации удаления мусорных записей в таблице
                boolean deleteHandled = ((GCNoLinkRemovable) table).gcHandleNoLinkRemoveData(database);
                // Если таблица сама не обработала удаление мусорных данных, то вызываем стандартную обрбаотку
                if (!deleteHandled) {
                    execSQL(DBGarbageCollectorHelper.getDeleteNoLinkSql(references, table.getTableName()));
                }
            }
        }
    }

    // Обрабатывает каскадные ссылки мастер таблицы
    private void deleteCascadeLinks(BaseTableDao masterTable){
        Collection<ReferenceInfo> childs = references.getChilds(masterTable.getTableName());
        if (hasCascadeLinks(childs)){
            Logger.info(TAG, "start deleting cascade links for table" + masterTable.getTableName());

            if (masterTable instanceof GCCascadeLinksRemovable) {
                for (ReferenceInfo ri : childs) {
                    if (ri.getReferencesType().equals(ReferenceInfo.ReferencesType.CASCADE)){

                        Logger.info(TAG, "delete records by cascade link:" + ri.getReferenceTable() + "." + ri.getReferenceField());
                        processInfo(context.getString(R.string.gc_delete_old_data_table) + " " + ri.getReferenceTable());

                        // Вызываем обработку каскадной ссылки самой таблицей
                        boolean linkHandled = ((GCCascadeLinksRemovable) masterTable).gcHandleRemoveCascadeLink(database, ri.getReferenceTable(), ri.getReferenceField());

                        // если таблица сама не обработала каскадную ссылку, то удаляем данные по каскадной ссылке
                        if (!linkHandled){
                            deleteCascadeLinkRecords((GCCascadeLinksRemovable) masterTable, ri);
                        }
                    }
                }
            } else {
                throw new RuntimeException(String.format("Table %s is not supported cascade links and not implemented interface GC_CascadeLinksRemovable", masterTable.getTableName()));
            }
        }
    }

    // Удаляет данные по каскадной ссылке
    private void deleteCascadeLinkRecords(GCCascadeLinksRemovable masterTable, ReferenceInfo ri){
        BaseTableDao referencesTable = entities.get(ri.getReferenceTable());
        // Если удаляемая таблица поддерживает пометку записей на удаление, то помечаем их
        if (referencesTable instanceof GCDeletedMarkSupported){
            execSQL(DBGarbageCollectorHelper.getMarkDeletedCascadeSql(ri, masterTable.getDeletedMarkField(), ((GCDeletedMarkSupported) referencesTable).getDeletedMarkField()));

            // После пометки записей на удаление, обрабатываем каскадные ссылки на эту таблицу
            deleteCascadeLinks(referencesTable);
        } else {
            // Иначе просто удаляем записи в таблице, ссылающиеся на удаляемые строки в мастер таблице
            execSQL(DBGarbageCollectorHelper.getDeleteCascadeSql(ri, masterTable.getDeletedMarkField()));
        }
    }

    private boolean hasCascadeLinks(Collection<ReferenceInfo> childs){
        for (ReferenceInfo ri : childs) {
            if (ri.getReferencesType().equals(ReferenceInfo.ReferencesType.CASCADE)){
                return true;
            }
        }
        return false;
    }

    private void execSQL(String sql){
        Logger.info(TAG, "execute sql command:   " + "\n" + sql);
        database.execSQL(sql);
    }

}
