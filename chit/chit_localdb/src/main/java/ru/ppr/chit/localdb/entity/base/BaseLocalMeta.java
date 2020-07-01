package ru.ppr.chit.localdb.entity.base;

import java.util.ArrayList;
import java.util.Collection;

import ru.ppr.database.base.BaseTableDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * Базовый класс для описания метаинформации о таблицах
 *
 * @author m.sidorov
 */
public abstract class BaseLocalMeta implements BaseTableDao {

    public final Collection<ReferenceInfo> references = new ArrayList<>();

    // Регистрирует ссылки на другие таблицы
    public void registerReference(String table, String referenceField, ReferenceInfo.ReferencesType referencesType){
        references.add(new ReferenceInfo(table, LocalEntityWithId.PropertyId, getTableName(), referenceField, referencesType));
    }

    // Регистрирует ссылки на другие таблицы (по умолчанию = NO_ACTION)
    public void registerReference(String table, String referenceField){
        references.add(new ReferenceInfo(table, LocalEntityWithId.PropertyId, getTableName(), referenceField, ReferenceInfo.ReferencesType.NO_ACTION));
    }

    @Override
    public String getPkField() {
        return LocalEntityWithId.PropertyId;
    }
}
