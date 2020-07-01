package ru.ppr.inpas.lib.command.financial;

import ru.ppr.inpas.lib.protocol.model.OperationCode;

/**
 * Операция «Оплата».
 * Код операции: 1.
 */
public class SaleCommand extends FinancialCommand {

    public SaleCommand(final int amount, final int transactionNumber) {
        setAmount(amount);
        setCurrencyCode(DEFAULT_CURRENCY_CODE);
        setCardInputMode(CARD_INPUT_MODE);
        setCurrentDate();
        setOperationCode(OperationCode.SALE.getValue());
        setTransactionNumber(transactionNumber);
    }

}