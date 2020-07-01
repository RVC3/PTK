package ru.ppr.cppk.sync.kpp;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.TicketSaleReturnEventBase;
import ru.ppr.cppk.sync.kpp.model.BankCardPayment;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketReturn extends TicketSaleReturnEventBase {

    /**
     * Вид операции возврата ПД
     * Аннулирование - 0
     * Возврат - 1
     */
    @Nullable
    public Integer operation;

    public Date recallDateTime;

    public String recallReason;

    public Integer recallTicketNumber;

    /**
     * Код способа возврата.
     * Соответствует коду возврата оплаты в справочнике НСИ «Способы оплаты».
     */
    @Nullable
    public Integer returnPaymentMethod;

    public BankCardPayment returnBankCardPayment;

    /**
     * Полная стоимость билета без льгот
     */
    public BigDecimal fullTicketPrice;

    /**
     * Сумма к возврату (с учетом частичного использования билета)
     */
    public BigDecimal sumToReturn;

}
