package ru.ppr.inpas.lib.protocol.model;

/**
 * Статус выполнения транзакции на POS-терминале.
 * Содержится в SAF_TRX_STATUS.
 *
 * @see SaField
 * @see ru.ppr.inpas.lib.protocol.SaPacket
 */
public enum TransactionStatus {
    /**
     * Неопределенный статус. Транзакция не выполнена.
     */
    UNKNOWN(0),

    /**
     * Одобрено. Положительное завершение транзакции.
     * Является единственным статусом одобрения транзакции.
     * Любое другое значение статуса должно расцениваться как не успех.
     */
    APPROVED(1),

    /**
     * Отказано. Транзакция проведена, но ее одобрение не получено.
     */
    DENIED(16),

    /**
     * Выполнено в OFFLINE.
     */
    COMPLETE_IN_OFFLINE(17),

    /**
     * Нет соединения.
     */
    NO_CONNECTION(34),

    /**
     * Операция прервана.
     */
    OPERATION_ABORTED(53);

    private final int value;

    TransactionStatus(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransactionStatus from(final int value) {
        TransactionStatus status = UNKNOWN;

        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            if (transactionStatus.value == value) {
                status = transactionStatus;
                break;
            }
        }

        return status;
    }
}
