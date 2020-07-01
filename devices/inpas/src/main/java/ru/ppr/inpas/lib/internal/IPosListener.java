package ru.ppr.inpas.lib.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.inpas.lib.protocol.SaPacket;

/**
 * Интерфейс содержащий поддерживаемые состояния и ошибки.
 */
public interface IPosListener {

    enum State {
        POS_CONNECTING,
        POS_CONNECTED,
        POS_DISCONNECTING,
        POS_DISCONNECTED,
        POS_SENDING,
        POS_RECEIVING,

        NETWORK_CONNECTING,
        NETWORK_CONNECTED,
        NETWORK_DISCONNECTING,
        NETWORK_DISCONNECTED,
        NETWORK_SENDING,
        NETWORK_RECEIVING
    }

    enum Error {
        ADAPTER_NOT_FOUND_ERROR,
        ADAPTER_DISABLED_ERROR,
        NO_PAIRED_DEVICES_ERROR,
        DEVICE_NOT_FOUND_ERROR,
        READ_ERROR,
        SEND_ERROR,
        NAK_ERROR,
        CRC_ERROR,
        PACKET_ERROR,
        DELAY_ERROR,
        ERROR,
        RESULT_ERROR,
        EXCHANGE_ERROR,
        PROCESS_ERROR,
        SA_PACKET_ERROR,
        POS_CONNECTION_ERROR,
        POS_CONNECTION_TIMEOUT,
        POS_RECEIVE_TIMEOUT_ERROR,
        NETWORK_CONNECTION_ERROR,
        NETWORK_CONNECTION_TIMEOUT,
        NETWORK_RECEIVE_TIMEOUT_ERROR
    }

    /**
     * Метод вызываемый при успешном поведении.
     *
     * @param packet SA пакет содержащий необходимые данные.
     * @see SaPacket
     */
    void onComplete(@NonNull final SaPacket packet);

    /**
     * Метод вызываемый при изменении состояния.
     *
     * @param state текущее состояние.
     * @see State
     */
    void onChanged(@NonNull final State state);

    /**
     * Метод вызываемый в случае ошибки.
     *
     * @param error  возникшая ошибка.
     * @param packet SA пакет содержащий необходимые данные. Наличие необязательно.
     * @see SaPacket
     */
    void onError(@NonNull final Error error, @Nullable final SaPacket packet);
}