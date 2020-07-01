package ru.ppr.cppk.sync.kpp.model.local;

/**
 * Локальная сущность, в KPP ее нет, заведена чтобы можно было достать дополнительные поля для {@link ru.ppr.cppk.sync.kpp.FinePaidEvent}
 *
 * @author Grigoriy Kashka
 */
public class Fine {

    public int code;
    /**
     * НДС (проценты)
     */
    public int ndsPercent;
}
