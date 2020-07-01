package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TicketTypeValidityTimeDao;
import ru.ppr.nsi.entity.TicketTypesValidityTimes;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TicketTypesValidityTimesRepository extends BaseRepository<TicketTypesValidityTimes, Integer> {

    @Inject
    TicketTypesValidityTimesRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TicketTypesValidityTimes, Integer> selfDao() {
        return daoSession().getTicketTypeValidityTimeDao();
    }

    /**
     * Возвращает интевалы времени, когда доступно оформления билетов указанного типа.
     *
     * @param ticketTypeCode Код типа билета
     * @param versionId      Версия НСИ
     * @return Список интервалов времени
     */
    @NonNull
    public List<TicketTypesValidityTimes> getTicketTypesValidityTimesList(
            int ticketTypeCode,
            int versionId) {

        List<TicketTypesValidityTimes> ticketTypeValidityTimeList = new ArrayList<>();
        String[] selectionArgs = new String[1];

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("*");
        sb.append(" FROM ");
        sb.append(TicketTypeValidityTimeDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(TicketTypeValidityTimeDao.Properties.TicketTypeCode).append(" = ").append("?");
        selectionArgs[0] = String.valueOf(ticketTypeCode);
        sb.append(" AND ");
        sb.append(NsiUtils.checkVersion(TicketTypeValidityTimeDao.TABLE_NAME, versionId));

        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(sb.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                TicketTypesValidityTimes ticketTypesValidityTimes = daoSession().getTicketTypeValidityTimeDao().fromCursor(cursor);
                ticketTypeValidityTimeList.add(ticketTypesValidityTimes);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ticketTypeValidityTimeList;
    }

}
