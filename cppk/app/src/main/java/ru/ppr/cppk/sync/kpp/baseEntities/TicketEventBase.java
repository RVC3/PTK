package ru.ppr.cppk.sync.kpp.baseEntities;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.model.Station;
import ru.ppr.cppk.sync.kpp.model.Tariff;
import ru.ppr.cppk.sync.kpp.model.SmartCard;

/**
 * Базовый класс события по ПД
 *
 * @author Grigoriy Kashka
 */
public class TicketEventBase extends CashRegisterEvent {

    /**
     * Порядковый номер
     */
    public int TicketNumber;

    /**
     * Дата время оформления
     */
    public Date SaleDateTime;

    /**
     * Срок действия С (включительно)
     * Согласно ЧТЗ нужно только для событий Продажи ПД без места, Возврат ПД без места и для всех событий контроля
     */
    public Date ValidFromDateTime;

    /**
     * Срок действия ДО (невключительно)
     * Согласно ЧТЗ нужно только для событий Продажи ПД без места, Возврат ПД без места
     */
    public Date ValidTillDateTime;

    /**
     * Станция отправления
     */
    public Station DepartureStation;

    /**
     * Станция назначения
     */
    public Station DestinationStation;

    public Tariff Tariff;

    /**
     * Направление билета
     * TicketWayType
     * 0 - туда
     * 1 - туда обратно
     */
    public Integer WayType;

    /**
     * Тип АСО Экспресс билета (разовый, абонемент, количество поездок и т.д.)
     */
    @Nullable
    public String Type;

    /**
     * Наш код типа билета
     */
    public int TypeCode;

    /**
     * Смарткарта с которой был прочитан билет
     */
    public SmartCard SmartCard;

}
