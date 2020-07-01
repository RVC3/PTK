package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.ProhibitedTicketTypeForExemptionCategoryDao;
import ru.ppr.nsi.entity.ProhibitedTicketTypeForExemptionCategory;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ProhibitedTicketTypeForExemptionCategoryRepository extends BaseRepository<ProhibitedTicketTypeForExemptionCategory, Long> {

    @Inject
    ProhibitedTicketTypeForExemptionCategoryRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ProhibitedTicketTypeForExemptionCategory, Long> selfDao() {
        return daoSession().getProhibitedTicketTypeForExemptionCategoryDao();
    }

    /**
     * Возвращает первое попавшееся ограничение по параметрам.
     *
     * @param ticketStorageTypeCode Тип носителя ПД
     * @param ticketTypeCode        Тип  ПД
     * @param passengerCategory     Категория пассажира
     * @param versionId             Версия НСИ
     * @return Первое попавшееся ограничение
     */
    public ProhibitedTicketTypeForExemptionCategory loadByParams(int ticketStorageTypeCode, int ticketTypeCode, @Nullable String passengerCategory, int versionId) {
        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(ProhibitedTicketTypeForExemptionCategoryDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(ProhibitedTicketTypeForExemptionCategoryDao.Properties.TicketStorageTypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(ticketStorageTypeCode));
        sb.append(" AND ");
        sb.append(ProhibitedTicketTypeForExemptionCategoryDao.Properties.TicketTypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(ticketTypeCode));
        if (passengerCategory != null) {
            sb.append(" AND ");
            sb.append(ProhibitedTicketTypeForExemptionCategoryDao.Properties.Category).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(passengerCategory));
        }
        sb.append(" AND ");
        sb.append(NsiUtils.checkVersion(ProhibitedTicketTypeForExemptionCategoryDao.TABLE_NAME, versionId));
        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        ProhibitedTicketTypeForExemptionCategory prohibitedTicketTypeForExemptionCategory = null;
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(sb.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                prohibitedTicketTypeForExemptionCategory = daoSession().getProhibitedTicketTypeForExemptionCategoryDao().readEntity(cursor, 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return prohibitedTicketTypeForExemptionCategory;
    }

}
