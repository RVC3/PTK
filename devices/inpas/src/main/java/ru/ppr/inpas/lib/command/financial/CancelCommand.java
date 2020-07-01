package ru.ppr.inpas.lib.command.financial;

import android.support.annotation.Nullable;

import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Операция «Отмена».
 * Код операции: 4.
 */
public class CancelCommand extends FinancialCommand {
    private int mCancelOperationCode;

    public CancelCommand(final int amount,
                         final int transactionNumber,
                         final int cancelOperationCode,
                         @Nullable final String authorizationCode,
                         @Nullable final String rrn) {
        setAmount(amount);
        setCurrencyCode(DEFAULT_CURRENCY_CODE);
        setCardInputMode(CARD_INPUT_MODE);
        setAuthorizationCode(authorizationCode);
        setReferenceNumber(rrn);
        setCurrentDate();
        setOperationCode(OperationCode.CANCEL.getValue());
        setTransactionNumber(transactionNumber);
        setCanceledOperationCode(cancelOperationCode);
    }

    public int getCancelOperationCode() {
        return mCancelOperationCode;
    }

    /**
     * Поле 34 | Код оригинальной операции, которая отменяется.
     *
     * @param value
     */
    private void setCanceledOperationCode(final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Incorrect cancelled operation code.");
        }

        mCancelOperationCode = value;
        mSaPacket.putInteger(SaField.SAF_ORG_OPER_ID, mCancelOperationCode);
    }


}
