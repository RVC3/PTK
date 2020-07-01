package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.model.SentEvents;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>SentEvents</i>.
 *
 * @author Grigoriy Kashka
 */
public class SentEventsDao extends BaseDao {

    private static String TAG = Logger.makeLogTag(SentEventsDao.class);

    public static final String TABLE_NAME = "SentEvents";

    public static class Properties {
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    /**
     * Названия полей, синхронизированы с кассой
     */
    public final static class Entities {
        public static final String SENT_SHIFT_EVENTS = "SentShiftEvents";
        public static final String SENT_TICKET_CONTROLS = "SentTicketControls";
        public static final String SENT_TICKET_SALES = "SentTicketSales";
        public static final String SENT_TEST_TICKETS = "SentTestTickets";
        public static final String SENT_TICKET_RETURNS = "SentTicketReturns";
        public static final String SENT_MONTH_CLOSURES = "SentMonthClosures";
        public static final String SENT_TICKET_PAPER_ROLLS = "SentTicketPaperRolls";
        public static final String SENT_BANK_TRANSACTIONS = "SentBankTransactions";
        public static final String SENT_TICKET_RESIGNS = "SentTicketReSigns";
        public static final String SENT_SERVICE_SALES = "SentServiceSales";
        public static final String SENT_FINE_PAID_EVENTS = "SentFinePaidEvents";
    }


    public SentEventsDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    /**
     * Возвращает SentEvents
     */
    @NonNull
    public SentEvents load() {
        final String query = "select * from " + TABLE_NAME;
        Logger.info(TAG, "sql=" + query);

        Cursor cursor = db().rawQuery(query, null);

        try {
            final int nameIndex = cursor.getColumnIndex(Properties.NAME);
            final int valueIndex = cursor.getColumnIndex(Properties.VALUE);

            final Map<String, Long> map = new HashMap<>();

            while (cursor.moveToNext()) {
                final String name = cursor.getString(nameIndex);
                final Long value = cursor.getLong(valueIndex);

                map.put(name, value);
            }

            return fromMap(map);

        } finally {
            cursor.close();
        }
    }

    private SentEvents fromMap(Map<String, Long> map) {
        final SentEvents sentEvents = new SentEvents();
        sentEvents.setSentShiftEvents(map.get(Entities.SENT_SHIFT_EVENTS));
        sentEvents.setSentTicketControls(map.get(Entities.SENT_TICKET_CONTROLS));
        sentEvents.setSentTicketSales(map.get(Entities.SENT_TICKET_SALES));
        sentEvents.setSentTestTickets(map.get(Entities.SENT_TEST_TICKETS));
        sentEvents.setSentTicketReturns(map.get(Entities.SENT_TICKET_RETURNS));
        sentEvents.setSentMonthClosures(map.get(Entities.SENT_MONTH_CLOSURES));
        sentEvents.setSentTicketPaperRolls(map.get(Entities.SENT_TICKET_PAPER_ROLLS));
        sentEvents.setSentBankTransactions(map.get(Entities.SENT_BANK_TRANSACTIONS));
        sentEvents.setSentTicketReSigns(map.get(Entities.SENT_TICKET_RESIGNS));
        sentEvents.setSentServiceSales(map.get(Entities.SENT_SERVICE_SALES));
        sentEvents.setSentFinePaidEvents(map.get(Entities.SENT_FINE_PAID_EVENTS));
        return sentEvents;
    }

    private Map<String, Long> toMap(SentEvents sentEvents) {
        Map<String, Long> map = new HashMap<>();
        map.put(Entities.SENT_SHIFT_EVENTS, sentEvents.getSentShiftEvents());
        map.put(Entities.SENT_TICKET_CONTROLS, sentEvents.getSentTicketControls());
        map.put(Entities.SENT_TICKET_SALES, sentEvents.getSentTicketSales());
        map.put(Entities.SENT_TEST_TICKETS, sentEvents.getSentTestTickets());
        map.put(Entities.SENT_TICKET_RETURNS, sentEvents.getSentTicketReturns());
        map.put(Entities.SENT_MONTH_CLOSURES, sentEvents.getSentMonthClosures());
        map.put(Entities.SENT_TICKET_PAPER_ROLLS, sentEvents.getSentTicketPaperRolls());
        map.put(Entities.SENT_BANK_TRANSACTIONS, sentEvents.getSentBankTransactions());
        map.put(Entities.SENT_TICKET_RESIGNS, sentEvents.getSentTicketReSigns());
        map.put(Entities.SENT_SERVICE_SALES, sentEvents.getSentServiceSales());
        map.put(Entities.SENT_FINE_PAID_EVENTS, sentEvents.getSentFinePaidEvents());
        return map;
    }


    /**
     * Обновляет и/или устанавливает SentEvents
     */
    public void update(@NonNull final SentEvents sentEvents) {
        final ContentValues contentValues = new ContentValues();

        try {
            db().beginTransaction();

            Map<String, Long> map = toMap(sentEvents);

            for (Map.Entry<String, Long> entry : map.entrySet()) {
                contentValues.put(Properties.NAME, entry.getKey());
                contentValues.put(Properties.VALUE, entry.getValue());

                final String whereClause = Properties.NAME + " = ?";
                final String[] selectionArgs = new String[]{entry.getKey()};

                final int rows = db().update(TABLE_NAME, contentValues, whereClause, selectionArgs);

                if (rows <= 0) {
                    db().insert(TABLE_NAME, null, contentValues);
                }

                contentValues.clear();
            }

            db().setTransactionSuccessful();
        } finally {
            db().endTransaction();
        }
    }

}
