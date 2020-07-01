package ru.ppr.cppk.sync.kpp.model;

import java.util.Date;

/**
 * Чек
 *
 * @author Grigoriy Kashka
 */
public class Check {

    public int Number;

    /**
     * Номер, символ # и значение контрольного проверочного кода
     */
    public String AdditionalInfo;

    public Date PrintDateTime;
}
