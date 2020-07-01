package ru.ppr.cppk.db.local.repository;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.ServiceTicketControlEventDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.repository.base.BaseCrudLocalDbRepository;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;

/**
 * @author Grigoriy Kashka
 */
@Singleton
public class ServiceTicketControlEventRepository extends BaseCrudLocalDbRepository<ServiceTicketControlEvent, Long> {

    @Inject
    ServiceTicketControlEventRepository(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ServiceTicketControlEvent, Long> dao() {
        return daoSession().getServiceTicketControlEventDao();
    }

    /**
     * Возвращет timestamp последнего события контроля сервисных карт
     */
    public long getLastServiceTicketControlCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKServiceSale
         * JOIN Event ON CPPKServiceSale.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(dao().getTableName());
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(ServiceTicketControlEventDao.Properties.EventId).append(" = ").append(daoSession().getEventDao().getIdWithTableName());

        Cursor cursor = daoSession().getLocalDb().rawQuery(sql.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                lastCreationTimeStamp = cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }

        return lastCreationTimeStamp;
    }

    /**
     * Возвращает количество событий контроля сервисных карт за смену.
     *
     * @param shiftId  Id смены
     * @param statuses Статусы событий
     * @return Количество событий
     */
    public int getServiceTicketControlEventsCountForShift(String shiftId, @Nullable EnumSet<ServiceTicketControlEvent.Status> statuses) {

        int count = 0;

        List<Integer> statusesList = null;

        if (statuses != null) {
            statusesList = new ArrayList<>();

            for (ServiceTicketControlEvent.Status status : statuses) {
                statusesList.add(status.getCode());
            }
        }

        QueryBuilder qb = new QueryBuilder();

        qb.select().count("*").from(ServiceTicketControlEventDao.TABLE_NAME);
        qb.innerJoin(ShiftEventDao.TABLE_NAME).on();
        qb.f1EqF2(ServiceTicketControlEventDao.TABLE_NAME, ServiceTicketControlEventDao.Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, BaseEntityDao.Properties.Id);
        qb.where().field(ShiftEventDao.TABLE_NAME, ShiftEventDao.Properties.ShiftId).eq(shiftId);

        if (statusesList != null) {
            qb.and().field(ServiceTicketControlEventDao.TABLE_NAME, ServiceTicketControlEventDao.Properties.Status).in(statusesList);
        }

        Cursor cursor = null;

        try {
            Query query = qb.build();
            cursor = query.run(daoSession().getLocalDb());

            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    count = cursor.getInt(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;

    }
}
