package ru.ppr.cppk.data.summary;

import android.database.Cursor;

import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;

/**
 * Статистика по недавним станциям, спользованным для оформления ПД.
 * Информация строится на основе событий продажи ПД.
 */
@Singleton
public class RecentStationsStatistics {

    private static final String TAG = Logger.makeLogTag(RecentStationsStatistics.class);

    private final LocalDbManager localDbManager;
    /**
     * Список последних использованных станций отправления
     */
    private final LinkedList<Integer> departureStationsCodes = new LinkedList<>();
    /**
     * Список последних использованных станций назначения
     */
    private final LinkedList<Integer> destinationsStationsCodes = new LinkedList<>();
    /**
     * Ограничение на размер списка недавних станций
     */
    private int stationsLimit = 20;
    /**
     * Ограничение на количество сканируемых событий продажи ПД при инициализации
     */
    private int loadLimit = 500;

    @Inject
    RecentStationsStatistics(LocalDbManager localDbManager) {
        this.localDbManager = localDbManager;
    }

    public RecentStationsStatistics setStationsLimit(final int value) {
        if (0 >= value) {
            throw new IllegalArgumentException("The limit value must be greater than 0.");
        }
        stationsLimit = value;
        return this;
    }

    public RecentStationsStatistics setLoadLimit(final int value) {
        if (0 >= value) {
            throw new IllegalArgumentException("The limit value must be greater than 0.");
        }
        loadLimit = value;
        return this;
    }

    /**
     * Возвращает список кодов недавно использованных станций отправления.
     */
    public LinkedList<Integer> getRecentDepartureStationsCodes() {
        return departureStationsCodes;
    }

    /**
     * Возвращает список кодов недавно использованных станций назначения.
     */
    public LinkedList<Integer> getRecentDestinationsStationsCodes() {
        return destinationsStationsCodes;
    }

    /**
     * Добавляет станцию отправления в список недавно использованных.
     *
     * @param stationCode Код станции отправления
     */
    public void addDepartureStationCode(final int stationCode) {
        addStationCode(stationCode, true);
    }

    /**
     * Добавляет станцию назначения в список недавно использованных.
     *
     * @param stationCode Код станции назначения
     */
    public void addDestinationStationCode(final int stationCode) {
        addStationCode(stationCode, false);
    }

    private void addStationCode(final int stationCode, final boolean isDepartureStation) {
        final LinkedList<Integer> list = isDepartureStation ? departureStationsCodes : destinationsStationsCodes;
        final int index = list.indexOf(stationCode);

        if (index >= 0) {
            list.remove(index);
        }

        if (list.size() == stationsLimit) {
            list.removeFirst();
        }

        list.addLast(stationCode);
    }

    /**
     * Выполняет предварительную инициализацию
     */
    public void init() {
        load(loadLimit, true);
        load(loadLimit, false);
    }

    private void load(final int recordLimit, final boolean isDepartureStation) {
        Cursor cursor = null;
        String columnName = "";
        LinkedList<Integer> list;

        if (0 >= recordLimit) {
            throw new IllegalArgumentException("The value of limit must be greater than 0.");
        }

        if (isDepartureStation) {
            list = departureStationsCodes;
            list.clear();

            columnName += TicketEventBaseDao.Properties.DepartureStationId;
        } else {
            list = destinationsStationsCodes;
            list.clear();

            columnName += TicketEventBaseDao.Properties.DestinationStationId;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT ").append(columnName).append(" ");
        sb.append("FROM (SELECT CPPKTicketSales.*, ");
        sb.append(TicketEventBaseDao.TABLE_NAME).append(".").append(columnName);
        sb.append(" FROM CPPKTicketSales ");
        sb.append("JOIN TicketSaleReturnEventBase ON CPPKTicketSales.TicketSaleReturnEventBaseId = TicketSaleReturnEventBase._id ");
        sb.append("JOIN TicketEventBase ON TicketSaleReturnEventBase.TicketEventBaseId = TicketEventBase._id ");
        sb.append("WHERE 1 AND CPPKTicketSales.ProgressStatus IN (1, 64) ");
        sb.append("ORDER BY CPPKTicketSales._id DESC ");
        sb.append("LIMIT ? ");
        sb.append(") LIMIT ?");
        sb.append(";");
        sb.trimToSize();

        final String query = sb.toString();

        Database db = localDbManager.getDaoSession().getLocalDb();
        cursor = db.rawQuery(query,
                new String[]{
                        String.valueOf(recordLimit),
                        String.valueOf(stationsLimit)
                }
        );

        try {
            while (cursor.moveToNext()) {
                try {
                    final int stationCode = cursor.getInt(0);
                    list.addFirst(stationCode);
                } catch (Exception e) {
                    Logger.error(TAG, e);
                }
            }
        } finally {
            cursor.close();
        }
    }

}