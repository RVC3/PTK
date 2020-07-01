package ru.ppr.cppk.sync.kpp.model.local;

import java.math.BigDecimal;

/**
 * Локальная сущность, в KPP ее нет, заведена чтобы можно было достать поле Tax для сущности {@link ru.ppr.cppk.sync.kpp.model.Price}
 *
 * @author Grigoriy Kashka
 */
public class TicketType {
    public int code;
    public BigDecimal tax;
}
