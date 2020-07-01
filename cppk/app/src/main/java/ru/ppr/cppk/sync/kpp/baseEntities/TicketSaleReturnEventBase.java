package ru.ppr.cppk.sync.kpp.baseEntities;

import ru.ppr.cppk.sync.kpp.model.AdditionalInfoForEtt;
import ru.ppr.cppk.sync.kpp.model.BankCardPayment;
import ru.ppr.cppk.sync.kpp.model.Check;
import ru.ppr.cppk.sync.kpp.model.Exemption;
import ru.ppr.cppk.sync.kpp.model.Fee;
import ru.ppr.cppk.sync.kpp.model.LegalEntity;
import ru.ppr.cppk.sync.kpp.model.ParentTicketInfo;
import ru.ppr.cppk.sync.kpp.model.Price;
import ru.ppr.cppk.sync.kpp.model.SeasonTicket;
import ru.ppr.cppk.sync.kpp.model.TrainInfo;

/**
 * Базовый класс для проданных и возвращенных (погашенных) билетов
 * @author Grigoriy Kashka
 */
public class TicketSaleReturnEventBase extends TicketEventBase {

    public ParentTicketInfo ParentTicket;

    /**
     * Перевозчик
     */
    public LegalEntity Carrier;

    /**
     * Льгота
     */
    public Exemption Exemption;


    /**
     * Чек
     */
    public Check Check;

    /**
     * Тип билета (полный, льготный, детский)
     * сущность TicketKind
     */
    public Integer Kind;

    /**
     * Информация о поезде
     */
    public TrainInfo TrainInfo;

    public SeasonTicket SeasonTicket;

    public boolean IsOneTimeTicket;

    public AdditionalInfoForEtt AdditionalInfoForETT;

    /**
     * Полная стоимость ПД
     */
    public Price FullPrice;

    /**
     * Информация о сборе в случае его взимания
     */
    public Fee Fee;

    /**
     * Код способа оплаты
     * Соответствует коду способа оплаты в справочнике НСИ «Способы оплаты».
     * Сущность PaymentType
     */
    public Integer PaymentMethod;

    public BankCardPayment BankCardPayment;
    
}
