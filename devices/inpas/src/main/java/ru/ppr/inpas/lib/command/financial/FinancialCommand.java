package ru.ppr.inpas.lib.command.financial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ru.ppr.inpas.lib.command.BaseCommand;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.model.CardInputMode;
import ru.ppr.inpas.lib.protocol.model.CurrencyCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Базовый класс для финансовых команд.
 */
public abstract class FinancialCommand extends BaseCommand {
    private static final String TAG = InpasLogger.makeTag(FinancialCommand.class);
    protected static final int DEFAULT_CURRENCY_CODE = CurrencyCode.RUB.getDigitalCode();
    protected static final int CARD_INPUT_MODE = CardInputMode.POS_TERMINAL_OR_PINPAD.getValue();

    protected int mAmount;
    protected int mCurrencyCode;
    protected String mAuthorizationCode;
    protected String mReferenceNumber; // RRN.

    /**
     * Метод возвращает сумму операции, выраженную в минимальных единицах валюты.
     *
     * @return сумма операции, выраженная в минимальных единицах валюты.
     */
    public int getAmount() {
        return mAmount;
    }

    /**
     * Метод для установления суммы операции, выраженной в минимальных единицах валюты.
     *
     * @param value сумма операции, выраженная в минимальных единицах валюты.
     */
    public void setAmount(final int value) {
        if (0 >= value) {
            final String msg = "Amount of sale transaction must be greater than 0.";
            InpasLogger.error(TAG, msg);

            throw new IllegalArgumentException(msg);
        }

        mAmount = value;
        mSaPacket.putString(SaField.SAF_TRX_AMOUNT, String.valueOf(mAmount)); // Поле 0 | Сумма операции, выраженная в минимальных единицах валюты.
    }

    /**
     * Метод для получения текущего кода валюты.
     *
     * @return код валюты
     */
    public int getCurrencyCode() {
        return mCurrencyCode;
    }

    /**
     * Метод для установления кода валюты.
     *
     * @param code код валюты.
     */
    public void setCurrencyCode(final int code) {
        if (0 >= code) {
            final String msg = "Currency code must be greater than 0.";
            InpasLogger.error(TAG, msg);

            throw new IllegalArgumentException(msg);
        }

        if ((code < 100) || (code >= 1000)) {
            final String msg = "Currency code can contain only 3 digits.";
            InpasLogger.error(TAG, msg);

            throw new IllegalArgumentException(msg);
        }

        mCurrencyCode = code;
        mSaPacket.putString(SaField.SAF_TRX_CURRENCY_CODE, String.valueOf(mCurrencyCode)); // Поле 4 | Код валюты операции.
    }

    /**
     * Метод для установления способа ввода карты.
     *
     * @param mode способ ввода карты.
     */
    public void setCardInputMode(final int mode) {
        mSaPacket.putInteger(SaField.SAF_CARD_ENTRY_MODE, mode); // Поле 8 | Способ ввода карты.
    }

    /**
     * Метод для получения кода авторизации.
     *
     * @return код авторизации.
     */
    @NonNull
    public String getAuthorizationCode() {
        return mAuthorizationCode;
    }

    /**
     * Метод для установления кода авторизации.
     * Если значение Кода авторизации было получено при выполнении транзакции,
     * то его наличие в запросе на Отмену обязательно.
     */
    public void setAuthorizationCode(@Nullable final String code) {
        if (!TextUtils.isEmpty(code)) {
            mAuthorizationCode = code;
            mSaPacket.putString(SaField.SAF_AUTH_CODE, mAuthorizationCode); // Поле 13 | Код авторизации. (Optional)
        }
    }

    /**
     * Метод для получения RRN.
     *
     * @return RRN.
     */
    @NonNull
    public String getReferenceNumber() {
        return mReferenceNumber;
    }

    /**
     * Метод для установления RRN.
     *
     * @param referenceNumber значение RRN.
     */
    public void setReferenceNumber(@Nullable final String referenceNumber) {
        if (!TextUtils.isEmpty(referenceNumber)) {
            mReferenceNumber = referenceNumber;
            mSaPacket.putString(SaField.SAF_RRN, mReferenceNumber); // Поле 14 | Номер ссылки (RRN). (Optional)
        }
    }

}
