package ru.ppr.cppk.db.migration;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.utils.DateTimeUtils;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV93 extends MigrationBase {
    public MigrationVV93(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {

        List<TicketSaleReturnEventBase> events = new ArrayList<>();
        Cursor cursor = null;

        //собираем список данных
        try {
            cursor = localDB.rawQuery(getQueryForLoadData(), null);
            while (cursor.moveToNext()) {
                TicketSaleReturnEventBase event = new TicketSaleReturnEventBase();
                event.ticketSaleReturnEventBaseId = cursor.getLong(0);
                event.issueDateTime = DateTimeUtils.getDateFromSQLite(cursor.getString(1));
                events.add(event);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        StringBuilder sql = new StringBuilder();

        //добавим данные
        for (TicketSaleReturnEventBase event : events) {
            //если тарифный план с доплатой, тогда добавим строчку
            sql.append("INSERT INTO AdditionalInfoForEtt (IssueDataTime) VALUES (").append(event.issueDateTime.getTime()).append("); ");
            sql.append("UPDATE TicketSaleReturnEventBase SET AdditionInfoForEttId = (SELECT last_insert_rowid()) WHERE _id = ").append(event.ticketSaleReturnEventBaseId).append("; ");
        }

        migrate(localDB, sql.toString());
    }

    @Override
    public int getVersionNumber() {
        return 93;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем дополнительные события AdditionalInfoForEtt для событий продажи со льготой введенной вручную";
    }

    private String getQueryForLoadData() {
        return "SELECT " +
                "TicketSaleReturnEventBase._id, " +
                "Exemption.IssueDate " +
                "FROM TicketSaleReturnEventBase " +
                "JOIN Exemption ON Exemption._id=TicketSaleReturnEventBase.ExemptionId " +
                "WHERE " +
                "TicketSaleReturnEventBase.AdditionInfoForEttId IS NULL " +
                "AND TicketSaleReturnEventBase.ExemptionId NOTNULL " +
                "AND Exemption.IssueDate NOTNULL";
    }

    private class TicketSaleReturnEventBase {
        /**
         * Локальный идентификатор события
         */
        long ticketSaleReturnEventBaseId;
        /**
         * Дата выпуска ЭТТ
         */
        Date issueDateTime;
    }

}