package ru.ppr.cppk.db.migration;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.di.Di;
import ru.ppr.database.Database;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV94 extends MigrationBase {
    public MigrationVV94(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        NsiDaoSession nsiDaoSession = Di.INSTANCE.nsiDbManager().getDaoSession();

        List<SaleEvent> events = new ArrayList<>();
        Cursor cursorForLocal = null;

        //собираем список событий продажи
        try {
            cursorForLocal = localDB.rawQuery(getQueryForLoadTariffCodesString(), null);
            while (cursorForLocal.moveToNext()) {
                SaleEvent event = new SaleEvent();
                event.cppkTicketSaleEventId = cursorForLocal.getLong(0);
                event.tariffCode = cursorForLocal.getLong(1);
                event.versionId = cursorForLocal.getInt(2);
                events.add(event);
            }
        } finally {
            if (cursorForLocal != null) {
                cursorForLocal.close();
            }
        }

        StringBuilder sql = new StringBuilder("ALTER TABLE CPPKTicketSales ADD COLUMN ConnectionType INTEGER DEFAULT NULL;");

        //поищем флаг доплаты в НСИ
        for (SaleEvent event : events) {
            Cursor cursor = null;
            try {
                cursor = nsiDaoSession.getNsiDb().rawQuery(getQueryForLoadTariffsString(event.tariffCode, event.versionId), null);
                if (cursor.moveToFirst()) {
                    if (cursor.getInt(0) == 1) {
                        //если тарифный план с доплатой, тогда добавим строчку
                        sql.append("UPDATE CPPKTicketSales set ConnectionType = 2 WHERE _id=").append(event.cppkTicketSaleEventId).append(";");
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        migrate(localDB, sql.toString());
    }

    @Override
    public int getVersionNumber() {
        return 94;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем в CPPKTicketSale поле ConnectionType";
    }

    private String getQueryForLoadTariffCodesString() {
        return "select CPPKTicketSales._id, " +
                "TicketEventBase.TariffCode, " +
                "Event.VersionId " +
                " from CPPKTicketSales " +
                "JOIN TicketSaleReturnEventBase ON TicketSaleReturnEventBase._id=CPPKTicketSales.TicketSaleReturnEventBaseId " +
                "JOIN TicketEventBase ON TicketSaleReturnEventBase.TicketEventBaseId=TicketEventBase._id " +
                "JOIN Event ON Event._id=CPPKTicketSales.EventId ";
    }

    private String getQueryForLoadTariffsString(long tariffCode, int versionId) {
        return "SELECT " +
                "IsSurcharge " +
                "from Tariffs " +
                "JOIN TariffPlans ON TariffPlanCode=TariffPlans.Code AND TariffPlans.VersionId <= Tariffs.VersionId AND (TariffPlans.DeleteInVersionId>Tariffs.VersionId OR TariffPlans.DeleteInVersionId IS NULL) " +
                "WHERE Tariffs.Code = " + tariffCode +
                " AND Tariffs.VersionId<=" + versionId +
                " ORDER BY Tariffs.VersionId DESC LIMIT 1";
    }


    private class SaleEvent {
        /**
         * Локальный идентификатор события продажи
         */
        long cppkTicketSaleEventId;
        /**
         * Код тарифа
         */
        long tariffCode;
        /**
         * версия НСИ на которой создано событие продажи
         */
        int versionId;
    }

}