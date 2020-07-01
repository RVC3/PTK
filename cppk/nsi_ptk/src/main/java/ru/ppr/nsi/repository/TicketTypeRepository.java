package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TicketStorageTypeToTicketTypeDao;
import ru.ppr.nsi.dao.TicketTypeDao;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TicketTypeRepository extends BaseRepository<TicketType, Integer> {

    @Inject
    TicketTypeRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TicketType, Integer> selfDao() {
        return daoSession().getTicketTypeDao();
    }

    @Override
    public TicketType load(Integer code, int versionId) {
        TicketType loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<TicketType> loadAll(Iterable<Long> codes, int versionId) {
        List<TicketType> loaded = super.loadAll(codes, versionId);

        for (TicketType ticketType : loaded) {
            ticketType.setVersionId(versionId);
        }

        return loaded;
    }

    /**
     * Возвращает список типов билетов, принадлежащих указанным категориям билетов.
     *
     * @param ticketCategoryCodes Коды категорий билетов
     * @return Список типов билетов
     */
    @NonNull
    public List<TicketType> getTicketTypesForTicketCategories(@Nullable List<Long> ticketCategoryCodes, int versionId) {
        List<TicketType> list = new ArrayList<>();

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(TicketTypeDao.TABLE_NAME).where();
        qb.appendRaw(NsiUtils.checkVersion(TicketTypeDao.TABLE_NAME, versionId));
        if (ticketCategoryCodes != null) {
            qb.and().field(TicketTypeDao.Properties.TicketCategoryCode).in(ticketCategoryCodes);
        }

        Cursor cursor = qb.build().run(daoSession().getNsiDb());
        try {
            while (cursor.moveToNext()) {
                TicketType ticket = selfDao().fromCursor(cursor);
                ticket.setVersionId(versionId);
                list.add(ticket);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * Проверяет возможность записи/печати ПД указанного типа на указаный тип носителя
     *
     * @param ticketStorageType
     * @param ticketTypeCode
     * @return
     */
    public boolean canTicketTypeBeWrittenWithTicketStorageType(@NonNull TicketStorageType ticketStorageType, long ticketTypeCode, int nsiVersion) {
        //select count(*) as count from TABLE_NAME where
        //VersionId <= 81 AND (DeleteInVersionId > 81 OR DeleteInVersionId is NULL) AND
        //ticketStorageTypeCode = 1

        String query = "select count(*) as count from " + TicketStorageTypeToTicketTypeDao.TABLE_NAME + " where " +
                BaseEntityDao.Properties.VersionId + " <= " + nsiVersion + " AND (" + BaseEntityDao.Properties.DeleteInVersionId +
                " > " + nsiVersion + " OR " + BaseEntityDao.Properties.DeleteInVersionId + " is NULL) AND " +
                TicketStorageTypeToTicketTypeDao.Properties.TicketStorageTypeCode + " = " + ticketStorageType.getDBCode() +
                " AND " + TicketStorageTypeToTicketTypeDao.Properties.TicketTypeCode + " = " + ticketTypeCode;

        Cursor cursor = null;
        boolean result;
        try {
            cursor = daoSession().getNsiDb().rawQuery(query, null);
            result = cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex("count")) > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

}
