package ru.ppr.cppk.sync.kpp;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;

/**
 * @author Grigoriy Kashka
 */
public class TestTicketEvent extends CashRegisterEvent {
    /**
     * Номер ПД
     */
    public int number;

    /**
     * Дата и время печати ПД
     */
    public Date printDateTime;
}
