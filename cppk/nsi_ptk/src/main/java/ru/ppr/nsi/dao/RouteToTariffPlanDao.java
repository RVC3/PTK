package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.RouteToTariffPlan;

/**
 * DAO для таблицы НСИ <i>RoutesToTariffPlans</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class RouteToTariffPlanDao extends BaseEntityDao<RouteToTariffPlan, Integer> {

    public static final String TABLE_NAME = "RoutesToTariffPlans";

    public static class Properties {
        public static final String TariffPlanCode = "TariffPlanCode";
        public static final String RouteCode = "RouteCode";
    }

    public RouteToTariffPlanDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public RouteToTariffPlan fromCursor(Cursor cursor) {
        RouteToTariffPlan entity = new RouteToTariffPlan();

        entity.setTariffPlanCode(cursor.getInt(cursor.getColumnIndex(Properties.TariffPlanCode)));
        entity.setRouteCode(cursor.getString(cursor.getColumnIndex(Properties.RouteCode)));

        //добавим базовые поля
        addBaseNSIData(entity, Integer.class, cursor);

        return entity;
    }
}
