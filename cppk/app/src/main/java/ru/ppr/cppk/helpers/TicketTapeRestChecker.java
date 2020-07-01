package ru.ppr.cppk.helpers;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.logger.Logger;

/**
 * Класс-помошник, следит за остатком билетной ленты.
 *
 * @author Grigoriy Kashka
 */
public class TicketTapeRestChecker {

    private static final String TAG = Logger.makeLogTag(TicketTapeRestChecker.class);

    private final LocalDaoSession localDaoSession;
    private final PaperUsageCounter paperUsageCounter;
    private final CommonSettingsStorage commonSettingsStorage;

    @Inject
    public TicketTapeRestChecker(LocalDaoSession localDaoSession,
                                 PaperUsageCounter paperUsageCounter,
                                 CommonSettingsStorage commonSettingsStorage) {
        this.localDaoSession = localDaoSession;
        this.paperUsageCounter = paperUsageCounter;
        this.commonSettingsStorage = commonSettingsStorage;
    }

    /**
     * Проверит не кончается ли билетная лента, в случае если ленты осталось мало вернет false
     *
     * @return
     */
    public boolean check() {
        TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();

        if (ticketTapeEvent == null) {
            Logger.warning(TAG, "Билетная лента не установлена!");
            return true;
        }

        String serial = ticketTapeEvent.getSeries();
        int meters = Integer.parseInt(serial.substring(0, 3));

        PaperUsage paperUsage = paperUsageCounter.getPaperUsage(PaperUsage.ID_TAPE);
        int paperConsumptionMillimeters = (int) paperUsage.getPaperLength();

        int ticketRestMillimeters = meters * 1000 - paperConsumptionMillimeters;

        return ticketRestMillimeters > commonSettingsStorage.get().getTicketTapeAttentionLength() * 10;
    }
}
