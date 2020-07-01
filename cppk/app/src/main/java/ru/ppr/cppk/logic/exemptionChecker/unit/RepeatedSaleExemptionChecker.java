package ru.ppr.cppk.logic.exemptionChecker.unit;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ExemptionsTo;

/**
 * Проверка "Задержка повторного оформления ПД на одну и ту же СКМ/СКМО/СКМ2/УЭК"
 *
 * @author Aleksandr Brazhkin
 */
public class RepeatedSaleExemptionChecker {

    private final LocalDaoSession localDaoSession;
    private final NsiDaoSession nsiDaoSession;

    public RepeatedSaleExemptionChecker(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemptionForEvent Льгота
     * @param exemptionCode     Код льготы
     * @param ticketTypeCode    Код типа ПД
     * @param trainCategoryCode Код категории поезда
     * @param nsiVersion        Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(ExemptionForEvent exemptionForEvent, int exemptionCode, int ticketTypeCode, int trainCategoryCode, int nsiVersion) {
        boolean result = true;

        Date lastPdSellTime = null;
        if (exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption() != null) {
            CPPKTicketSales lastSaleForSmartCard = localDaoSession.getCppkTicketSaleDao()
                    .findLastSellWithExemptionForSmartCard(
                            exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption(),
                            Arrays.asList(ProgressStatus.Completed, ProgressStatus.CheckPrinted));

            if (lastSaleForSmartCard != null) {

                CPPKTicketReturn ticketReturn = localDaoSession.getCppkTicketReturnDao()
                        .findLastPdRepealEventForPdSaleEvent(lastSaleForSmartCard.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));
                // проверим аннулирован билет или нет
                if (ticketReturn == null) {

                    // возьмем дату создания события, т.к. при проверке задержки используется время с птк,
                    // а в поле время "время продажи" записывается время с принтера, которое может отличаться
                    // от птк времени, установленном на птк.
                    Event event = localDaoSession.getEventDao().load(lastSaleForSmartCard.getEventId());
                    lastPdSellTime = event.getCreationTimestamp();
                }
            }
        }

        if (lastPdSellTime != null) {

            ExemptionsTo exemptionsTo = nsiDaoSession.getExemptionToDao().getExemptionsTo(exemptionCode, ticketTypeCode, trainCategoryCode, nsiVersion);

            Integer delay;
            if (exemptionsTo != null && (delay = exemptionsTo.getTicketProcessingDelay()) != null) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lastPdSellTime);
                calendar.add(Calendar.MINUTE, delay);

                if (new Date().before(calendar.getTime())) {
                    result = false;
                }
            }
        }
        return result;
    }
}
