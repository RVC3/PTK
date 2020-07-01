package ru.ppr.cppk.logic.exemptionChecker.unit;

import javax.inject.Inject;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Проверка "Разрешение на оформление для категории поезда и типа билета"
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeTrainCategoryExemptionChecker {

    private final NsiDaoSession nsiDaoSession;

    @Inject
    public TicketTypeTrainCategoryExemptionChecker(NsiDaoSession nsiDaoSession) {
        this.nsiDaoSession = nsiDaoSession;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemptionCode     Код льготы
     * @param ticketTypeCode    Код типа ПД
     * @param trainCategoryCode Код категории поезда
     * @param nsiVersion        Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(int exemptionCode, int ticketTypeCode, int trainCategoryCode, int nsiVersion) {
        return nsiDaoSession.getExemptionToDao().checkExemptionToTicketTypeAndTrainCategory(exemptionCode, ticketTypeCode, trainCategoryCode, nsiVersion);
    }
}
