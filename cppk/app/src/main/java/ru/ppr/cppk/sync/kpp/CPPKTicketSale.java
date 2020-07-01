package ru.ppr.cppk.sync.kpp;

import android.support.annotation.Nullable;

import ru.ppr.cppk.sync.kpp.baseEntities.TicketSaleReturnEventBase;
import ru.ppr.cppk.sync.kpp.model.PreTicket;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketSale extends TicketSaleReturnEventBase {

    /**
     * Признак того, что билет был записан на носитель
     */
    @Nullable
    public Boolean IsTicketWritten;

    /**
     * Количество поездок
     * Необязательное, указывается для абонементов «на количество поездок»
     */
    @Nullable
    public Integer TripsCount;

    /**
     * Код типа носителя ПД
     * Соответствует коду типа носителя в справочнике НСИ «Типы носителей ПД».
     */
    public int StorageTypeCode;

    /**
     * номер ключа ЭЦП
     * Параметр необходим для формирования Белого списка при отзыве ключа
     */
    public long EDSKeyNumber;

    /**
     * Признак связи с другим ПД - доплата, транзит, 2-й сегмент или трансфер
     */
    @Nullable
    public Integer ConnectionType;

    /**
     * Информация о талоне предварительного ПД
     */
    @Nullable
    public PreTicket PreTicket;
}
