package ru.ppr.nsi.repository.base;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;

/**
 * Базовый репозиторий для таблиц НСИ.
 *
 * @param <T> Тип сущности, ассоциированой с таблицей НСИ.
 * @param <K> Тип ключа (Primary Key) для таблицы НСИ.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseRepository<T, K> {

    private final NsiDbSessionManager nsiDbSessionManager;

    public BaseRepository(NsiDbSessionManager nsiDbSessionManager) {
        this.nsiDbSessionManager = nsiDbSessionManager;
    }

    protected abstract BaseEntityDao<T, K> selfDao();

    protected NsiDaoSession daoSession() {
        return nsiDbSessionManager.getDaoSession();
    }

    /**
     * Получает запись таблицы НСИ по коду (PrimaryKey).
     *
     * @param code      Код
     * @param versionId Версия НСИ, для которой происходит выборка
     * @return Сущность с указанным кодом
     */
    public T load(K code, int versionId) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(selfDao().getTablename());
        stringBuilder.append(" WHERE ");
        stringBuilder.append(BaseEntityDao.Properties.Code).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(code));
        stringBuilder.append(" AND ");
        stringBuilder.append(NsiUtils.checkVersion(selfDao().getTablename(), versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        T entity = null;

        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);

            if (cursor.moveToFirst()) {
                entity = selfDao().readEntity(cursor, 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return entity;
    }

    /**
     * Возвращает список сущностей по списку кодов.
     * Реализован специально на ключи типа Long вместо Generic,
     * потому что большинство сущностей имеют Id типа Integer,
     * что неправильно и мешает работе.
     * Следует придерживаться id типа Long
     *
     * @param codes     Коды сущностей
     * @param versionId Версия НСИ
     * @return Список сущностей
     */
    public List<T> loadAll(Iterable<Long> codes, int versionId) {
        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(selfDao().getTablename());
        qb.where().field(BaseEntityDao.Properties.Code).in(codes);
        qb.and().appendRaw(NsiUtils.checkVersion(selfDao().getTablename(), versionId));

        Cursor cursor = null;
        List<T> entities = new ArrayList<>();

        try {
            cursor = qb.build().run(daoSession().getNsiDb());

            while (cursor.moveToNext()) {
                T entity = selfDao().readEntity(cursor, 0);
                entities.add(entity);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return entities;
    }

}
