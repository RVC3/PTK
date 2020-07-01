package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.CalendarDao;
import ru.ppr.nsi.entity.TypeOfDay;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class CalendarRepository extends BaseRepository<String, Integer> {

    private static final String TAG = Logger.makeLogTag(CalendarRepository.class);

    @Inject
    CalendarRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<String, Integer> selfDao() {
        return daoSession().getCalendarDao();
    }

    /**
     * Достает код дня из таблицы календарей (не обрабатывает региональный календарь).
     *
     * @param timestamp
     * @return
     */
    @NonNull
    public TypeOfDay getTypeOfDateFromCalendarTable(Date timestamp, int nsiVersion) {
        TypeOfDay out = TypeOfDay.UNKNOWN;
        // в соответствии с багом http://aj.srvdev.ru/browse/CPPKPP-18628 нужно всегда брать последнюю версию календаря
        if (nsiVersion == -1) {
            Logger.trace(TAG, "Actual version database notfound");
            return out;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select ").append(CalendarDao.Properties.TypeOfDate)
                .append(" from ").append(CalendarDao.TABLE_NAME)
                .append(" WHERE strftime('%Y-%m-%d' , Date) = strftime('%Y-%m-%d' , '")
                .append(dateString)
                .append("') AND VersionId <= ")
                .append(nsiVersion)
                .append(" AND (deleteInVersionId > ")
                .append(nsiVersion)
                .append(" OR DeleteInVersionId IS NULL) AND Year =  ")
                .append(calendar.get(Calendar.YEAR));

        Cursor cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);

        try {
            if (cursor.moveToFirst() && cursor.getCount() == 1) {
                out = TypeOfDay.valueOf(cursor.getInt(0));
            }
        } finally {
            cursor.close();
        }

        return out;
    }

}
