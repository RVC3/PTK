package ru.ppr.cppk.sync.kpp;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.kpp.model.BankCardPayment;
import ru.ppr.cppk.sync.kpp.model.Check;

/**
 * Событие oформления (оплаты) штрафа
 *
 * @author Grigoriy Kashka
 */
public class FinePaidEvent extends CashRegisterEvent {
    /**
     * Код штрафа. Соответствует значению "Код" в справочнике НСИ "Штрафы"
     */
    public int fineCode;

    /**
     * Дата и время операции
     */
    public Date operationDateTime;

    /**
     * Сквозной номер документа (Нефискальный)
     */
    public int docNumber;

    /**
     * Информация о чеке
     */
    public Check check;

    /**
     * Сумма штрафа, руб
     */
    public BigDecimal amount;

    /**
     * Процент НДС с суммы штрафа
     */
    public int ndsPercent;

    /**
     * НДС с суммы штрафа, руб
     */
    public BigDecimal nds;

    /**
     * Способ оплаты
     */
    public int paymentType;

    /**
     * Параметры оплаты по банковской карте
     */
    public BankCardPayment bankCardPayment;
}
