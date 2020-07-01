package ru.ppr.inpas.lib.protocol.model;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.protocol.SaPacket;

/**
 * Код операции для работы с POS-терминалом.
 * Содержится в SAF_OPERATION_CODE.
 *
 * @see SaField
 * @see SaPacket
 */
public enum OperationCode {
    UNKNOWN(0),

    /**
     * Оплата товаров и услуг.
     */
    SALE(1),

    /**
     * Отмена операции, выполненной в текущем операционном дне.
     */
    CANCEL(4),

    /**
     * Команда «WAIT» служит для информирования ККМ о том, что операция проходит в штатном режиме,
     * но требуется время для завершения операции (например, по причине плохой связи с процессинговым центром Банка).
     */
    WAIT(21),

    /**
     * Проверка соединения.
     */
    TEST_HOST(26),

    /**
     * Возврат. Применяется для возврата средств по операции Оплаты, выполненной не в текущем операционном дне.
     */
    REFUND(29),

    /**
     * Аварийная отмена. Используется по отношению к некорректному завершению последней операции.
     */
    EMERGENCY_CANCELLATION(53),

    /**
     * Сверка итогов. Выполняется за время (промежуток времени) после начала операционного дня. После выполнения Сверки БД POS терминала (пин-пада) очищается.
     */
    CLOSE_SESSION(59),

    /**
     * Выполнение пользовательской команды.
     */
    CUSTOM_COMMAND(63);

    private final int mValue;

    OperationCode(final int code) {
        mValue = code;
    }

    public int getValue() {
        return mValue;
    }

    public static OperationCode from(final int value) {
        OperationCode operationCode = UNKNOWN;

        for (OperationCode code : OperationCode.values()) {
            if (value == code.mValue) {
                operationCode = code;
                break;
            }
        }

        return operationCode;
    }

    public static OperationCode from(@NonNull final SaPacket packet) {
        OperationCode operationCode = UNKNOWN;

        if (SaPacket.isValid(packet)) {
            final Integer value = packet.getInteger(SaField.SAF_OPERATION_CODE);

            if (value != null) {
                operationCode = OperationCode.from(value);
            }
        }

        return operationCode;
    }

}