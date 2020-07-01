package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Проверка "Разрешение на оформление определенного вида ПД по указанному коду льготы"
 *
 * @author Aleksandr Brazhkin
 */
public class TicketStorageTypeExemptionChecker {

    private final NsiDaoSession nsiDaoSession;

    public TicketStorageTypeExemptionChecker(NsiDaoSession nsiDaoSession) {
        this.nsiDaoSession = nsiDaoSession;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param ticketStorageType Тип носителя ПД
     * @param exemption         Льгота
     * @param nsiVersion        Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(TicketStorageType ticketStorageType, Exemption exemption, int nsiVersion) {
        Integer exemptionExpressCode = exemption.getExemptionExpressCode();
        String regionOkatoCode = exemption.getRegionOkatoCode();
        return !nsiDaoSession.getExemptionBannedForTicketStorageTypeDao()
                .isExemptionBannedForTicketStorageType(ticketStorageType, regionOkatoCode, exemptionExpressCode, nsiVersion);
    }
}
