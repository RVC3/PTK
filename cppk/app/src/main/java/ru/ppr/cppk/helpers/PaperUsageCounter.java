package ru.ppr.cppk.helpers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.logger.Logger;

/**
 * Счетчик расхода билетной ленты.
 * Отвечает за изменение текущих показаний
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class PaperUsageCounter {

    private static final String TAG = Logger.makeLogTag(PaperUsageCounter.class);

    private final LocalDbManager localDbManager;

    /**
     * Конструктор
     */
    @Inject
    PaperUsageCounter(LocalDbManager localDbManager) {
        this.localDbManager = localDbManager;
    }

    /**
     * Возвращает DaoSession
     *
     * @return DaoSession
     */
    private LocalDaoSession localDaoSession() {
        return localDbManager.getDaoSession();
    }

    /**
     * Сбрасывает информацию о расходе билетной ленты для указанного среза
     *
     * @param id ID среза
     */
    public void resetPaperUsage(long id) {
        PaperUsage paperUsage = localDaoSession().getPaperUsageDao().load(id);
        paperUsage.setPaperLength(0);
        paperUsage.setRestarted(false);
        localDaoSession().getPaperUsageDao().update(paperUsage);
    }

    /**
     * Возвращает информацию о расходе билетной ленты для указанного среза
     *
     * @param id ID среза
     * @return
     */
    public PaperUsage getPaperUsage(long id) {
        PaperUsage paperUsage = localDaoSession().getPaperUsageDao().load(id);
        return paperUsage;
    }

    /**
     * Выполяет обновление информации о расходе билетной ленты по всем срезам
     *
     * @param odometerValue - Текущие показания одометра
     */
    public void setCurrentOdometerValueBeforePrinting(long odometerValue) {
        Logger.trace(TAG, "setCurrentOdometerValueBeforePrinting(" + odometerValue + ")");
        List<PaperUsage> paperUsages = localDaoSession().getPaperUsageDao().loadAll();
        for (PaperUsage paperUsage : paperUsages) {
            String objectBefore = paperUsage.toString();
            long diff = odometerValue - paperUsage.getPrevOdometerValue();
            if (diff > 0) {
                paperUsage.setPrevOdometerValue(odometerValue);
                paperUsage.setPaperLength(paperUsage.getPaperLength() + diff);
            } else if (diff < 0) {
                paperUsage.setPrevOdometerValue(odometerValue);
                paperUsage.setRestarted(true);
            }
            //если длина ленты пришла отрицательной или разница больше метра - то пора насторожиться
            if (odometerValue < 0 || diff > 1000) {
                Logger.error(TAG, "setCurrentOdometerValueBeforePrinting(" + odometerValue + ") paperUsageBefore=" + objectBefore + " - ЧТО-ТО ТУТ НЕ ТАК!");
            }
            localDaoSession().getPaperUsageDao().update(paperUsage);
        }
    }

}
