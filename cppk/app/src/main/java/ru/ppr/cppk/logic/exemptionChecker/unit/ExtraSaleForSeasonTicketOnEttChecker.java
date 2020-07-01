package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Проверка "Запрет на использование льготы при оформлении доплаты к абонементам, записанным на ЭТТ".
 *
 * @author Aleksandr Brazhkin
 */
public class ExtraSaleForSeasonTicketOnEttChecker {

    private final TicketCategoryChecker ticketCategoryChecker;

    public ExtraSaleForSeasonTicketOnEttChecker(TicketCategoryChecker ticketCategoryChecker) {
        this.ticketCategoryChecker = ticketCategoryChecker;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param ticketStorageType        Тип носителя ПД
     * @param parentTicketCategoryCode Код категории родительсеого ПД, {@code null} - если родительского ПД нет
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(TicketStorageType ticketStorageType, Integer parentTicketCategoryCode) {
        return !(parentTicketCategoryCode != null
                && ticketStorageType == TicketStorageType.ETT
                && ticketCategoryChecker.isTrainSeasonTicket(parentTicketCategoryCode));
    }
}
