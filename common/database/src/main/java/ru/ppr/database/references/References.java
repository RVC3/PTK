package ru.ppr.database.references;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import ru.ppr.database.base.BaseTableDao;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;

/**
 * Класс, хранящий информацию о свзях таблиц
 *
 * @author m.sidorov
 */
public class References {

    // мап, хранящий для таблицы связи, в которых другие таблицы ссылаются на нее
    private HashMap<String, HashMap<String, ReferenceInfo>> childs = new HashMap<>();
    // мап, хранящий для таблицы ее ссылки на другие таблицы
    private HashMap<String, HashMap<String, ReferenceInfo>> links = new HashMap<>();

    // Регистрирует свзяь между таблицами
    // MasterTable - на кого ссылаются,
    // ReferenceTable - кто ссылается
    public void registerReference(String masterTable, String masterField, String referenceTable, String referenceField, ReferenceInfo.ReferencesType referencesType){
        registerReference(new ReferenceInfo(masterTable, masterField, referenceTable, referenceField, referencesType));
    }

    public void registerReference(ReferenceInfo reference){
        registerChildLink(reference);
        registerMasterLink(reference);
    }

    // Возвращает ссылки таблицы table
    public Collection<ReferenceInfo> getLinks(String table){
        HashMap<String, ReferenceInfo> refs = links.get(table);
        if (refs != null){
            return refs.values();
        } else {
            return new ArrayList<ReferenceInfo>();
        }
    }

    // Возвращает ссылку по таблице и ее полю
    @Nullable
    public ReferenceInfo getLink(String table, String referenceField){
        HashMap<String, ReferenceInfo> refs = links.get(table);
        if (refs != null){
            return refs.get(referenceField);
        } else {
            return null;
        }
    }

    // Возвращает ссылки других таблиц на таблицу table
    public Collection<ReferenceInfo> getChilds(String table){
        HashMap<String, ReferenceInfo> refs = childs.get(table);
        if (refs != null){
            return refs.values();
        } else {
            return new ArrayList<ReferenceInfo>();
        }
    }

    // Проверка коректности регистрации ссылок
    // Нужно вызывать, после того, как все таблицы и ссылки зарегистрированы
    // Пока проверяет только то, что в таблицах на которые есть каскадные ссылки реализован интерфейс GC_CascadeLinksRemovable
    // T - базовый тип DAO для таблицы
    public <T extends BaseTableDao> void checkReferencesDeclaration(Collection<T> tables) {
        for (BaseTableDao table: tables) {
            if (hasCascadeLinks(table.getTableName())){
                if (!(table instanceof GCCascadeLinksRemovable)){
                    throw new RuntimeException(String.format("Table [%s] has cascade links and not implemented interface GC_CascadeLinksRemovable", table.getTableName()));
                }
            }
        }
    }

    // Определяет, если ли на таблицу каскадные ссылки
    private boolean hasCascadeLinks(String table){
        Collection<ReferenceInfo> childs = getChilds(table);
        for (ReferenceInfo ri : childs) {
            if (ri.getReferencesType().equals(ReferenceInfo.ReferencesType.CASCADE)){
                return true;
            }
        }
        return false;
    }

    private void registerMasterLink(ReferenceInfo reference){
        HashMap<String, ReferenceInfo> tableLinks = childs.get(reference.getMasterTable());
        if (tableLinks == null){
            tableLinks = new HashMap<>();
            childs.put(reference.getMasterTable(), tableLinks);
        }
        tableLinks.put(reference.getReferenceTable() + "." + reference.getReferenceField(), reference);
    }

    private void registerChildLink(ReferenceInfo reference){
        HashMap<String, ReferenceInfo> tableLinks = links.get(reference.getReferenceTable());
        if (tableLinks == null){
            tableLinks = new HashMap<>();
            links.put(reference.getReferenceTable(), tableLinks);
        }
        tableLinks.put(reference.getReferenceField(), reference);
    }


}
