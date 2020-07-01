package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TicketType;

/**
 * Проверка "Запрет на оформление льготных детских ПД"
 *
 * @author Aleksandr Brazhkin
 */
public class ForChildExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param exemption      Льгота
     * @param ticketTypeCode Код типа ПД
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(Exemption exemption, int ticketTypeCode) {
        return ticketTypeCode != TicketType.Code.SINGLE_CHILD || exemption.isChildTicketAvailable();
    }
}
