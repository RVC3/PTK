package ru.ppr.inpas.lib.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.SaField;
import ru.ppr.inpas.lib.utils.DateFormatter;

/**
 * Базовый класс для команд выполняемых на POS терминале, согласно протоколу SA.
 */
public abstract class BaseCommand {
    protected int mOperationCode;
    protected final SaPacket mSaPacket = new SaPacket();
    protected int mTransactionNumber;
    protected String mTerminalId;

    /**
     * Метод для установки даты.
     */
    public void setCurrentDate() {
        mSaPacket.putString(SaField.SAF_TRX_ORG_DATE_TIME, DateFormatter.now()); // Поле 21 | Оригинальная дата и время совершения операции YYYYMMDDHHMMSS на внешнем устройстве.
    }

    /**
     * Метод возращает код операции.
     *
     * @return код операции.
     * @see ru.ppr.inpas.lib.protocol.model.OperationCode
     */
    public int getOperationCode() {
        return mOperationCode;
    }

    /**
     * Метод устанавливает кода операции.
     *
     * @param value код операции.
     * @see ru.ppr.inpas.lib.protocol.model.OperationCode
     */
    public void setOperationCode(final int value) {
        if (0 > value) {
            throw new IllegalArgumentException("Wrong operation code value.");
        }

        mOperationCode = value;
        mSaPacket.putInteger(SaField.SAF_OPERATION_CODE, mOperationCode); // Поле 25 | Код операции.
    }

    /**
     * Метод для получения {@link SaPacket} из текущей команды.
     *
     * @return пакет текущей команды.
     */
    @NonNull
    public SaPacket getPacket() {
        return mSaPacket;
    }

    /**
     * Метод для получения номера транзакции.
     *
     * @return номер транзакции.
     */
    public int getTransactionNumber() {
        return mTransactionNumber;
    }

    /**
     * Метод для установкления номера транзакции.
     *
     * @param value номер транзакции.
     */
    public void setTransactionNumber(final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Incorrect transaction number.");
        }

        mTransactionNumber = value;
        mSaPacket.putInteger(SaField.SAF_STAN, mTransactionNumber); // Поле 26 | Уникальный номер транзакции на стороне внешнего устройства.
    }

    /**
     * Метод для получения ID терминала.
     *
     * @return ID терминала.
     */
    @NonNull
    public String getTerminalId() {
        return mTerminalId;
    }

    /**
     * Метод для установки ID терминала.
     *
     * @param value ID терминала.
     */
    public void setTerminalId(@Nullable final String value) {
        mTerminalId = value;
        mSaPacket.putString(SaField.SAF_TERMINAL_ID, mTerminalId); // Поле 27 | Идентификатор внешнего устройства.
    }

}