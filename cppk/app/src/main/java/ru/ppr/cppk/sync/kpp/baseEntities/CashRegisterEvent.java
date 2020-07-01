package ru.ppr.cppk.sync.kpp.baseEntities;

import ru.ppr.cppk.sync.kpp.model.CashRegister;
import ru.ppr.cppk.sync.kpp.model.Cashier;
import ru.ppr.cppk.sync.kpp.model.WorkingShift;

/**
 * Событие от фискального регистратора
 *
 * @author Grigoriy Kashka
 */
public class CashRegisterEvent extends Event {

    /**
     * Фискальный регистратор
     */
    public CashRegister CashRegister;

    /**
     * Кассир
     */
    public Cashier Cashier;

    /**
     * Номер рабочей смены
     */
    public WorkingShift WorkingShift;

}
