package ru.ppr.cppk.logic.exemptionChecker.unit;

import android.support.annotation.Nullable;

import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.repository.ProhibitedTicketTypeForExemptionCategoryRepository;

/**
 * Проверка "Запрет на оформления вида ПД для определенной категории льготника"
 *
 * @author Aleksandr Brazhkin
 */
public class BeneficiaryCategoryExemptionChecker {

    private final ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository;

    public BeneficiaryCategoryExemptionChecker(ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository) {
        this.prohibitedTicketTypeForExemptionCategoryRepository = prohibitedTicketTypeForExemptionCategoryRepository;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param ticketStorageType Тип носителя ПД
     * @param ticketTypeCode    Тип  ПД
     * @param passengerCategory Категория пассажира
     * @param nsiVersion        Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(TicketStorageType ticketStorageType, int ticketTypeCode, @Nullable String passengerCategory, int nsiVersion) {
        if (passengerCategory == null) {
            return true;
        }

        return prohibitedTicketTypeForExemptionCategoryRepository.loadByParams(ticketStorageType.getDBCode(), ticketTypeCode, passengerCategory, nsiVersion) == null;
    }
}
