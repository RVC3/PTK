package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;

/**
 * Проверка "Запрет на ручной ввод кассиром-контролером льготных данных"
 *
 * @author Aleksandr Brazhkin
 */
public class ManualInputExemptionChecker {

    private final NsiDaoSession nsiDaoSession;

    public ManualInputExemptionChecker(NsiDaoSession nsiDaoSession) {
        this.nsiDaoSession = nsiDaoSession;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemptionForEvent Льгота
     * @param exemption         Льгота
     * @param regionCode        Код региона
     * @param nsiVersion        Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(ExemptionForEvent exemptionForEvent, Exemption exemption, int regionCode, int nsiVersion) {
        return !exemptionForEvent.isManualInput() || !nsiDaoSession.getProhibitedForManualEntryExemptionDao().isCantBeEnteredManually(exemption, regionCode, nsiVersion);
    }
}
