package ru.ppr.inpas.lib.command.custom;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.NetworkCommunicator;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.CustomMode;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Пользовательская команда 17 «Переслать данные внешней системе от терминала и обратно».
 */
public class SendDataCustomCommand implements ICustomCommand {
    private static final String TAG = InpasLogger.makeTag(SendDataCustomCommand.class);
    private static final int CUSTOM_OPERATION_CODE = CustomMode.SEND.getValue();
    private static final int SEND_TO_REMOTE_MODE_COMMAND = 0;
    private static final int RECEIVE_FROM_REMOTE_MODE_COMMAND = 1;
    private static final int SUCCESSFUL_RESULT = 0;
    private static final int UNSUCCESSFUL_RESULT = 1;

    private final NetworkCommunicator mCommunicator;
    private final SaPacket mRequest;

    private boolean mError;
    private SaPacket mResult;

    public SendDataCustomCommand(@NonNull final NetworkCommunicator communicator,
                                 @NonNull final SaPacket request) {
        mCommunicator = communicator;
        mRequest = request;

        mError = true;
    }

    @NonNull
    @Override
    public SaPacket getResult() {
        return mResult;
    }

    @Override
    public void execute() {
        try {
            if (!mCommunicator.isConnected()) {
                throw new Exception("No connection with remote server.");
            }

            if (isCorrectRequest(mRequest)) {
                if (isSendRequest()) {
                    send();
                } else if (isReceiveRequest()) {
                    receive();
                }
            }
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
        }
    }

    /**
     * Метод для отправки данных.
     */
    private void send() {
        try {
            final byte[] data = mRequest.getBytes(SaField.SAF_FILE_DATA);

            if (null == data) {
                throw new Exception("No data to sendWithoutAck.");
            }

            mCommunicator.send(data);
            mError = false;
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
        }

        mResult = getOperationResult(!mError);
    }

    /**
     * Метод для приема данных.
     */
    private void receive() {
        try {
            final byte[] data = mCommunicator.receive();
            mResult = getOperationResult(true, data);
            mError = false;
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
        }

        if (mError) {
            mResult = getOperationResult(false);
        }
    }

    /**
     * Метод для провекри корректности запроса.
     *
     * @param packet запрос для проверки.
     * @return результат проверки.
     */
    private boolean isCorrectRequest(@NonNull final SaPacket packet) {
        boolean result = false;

        if (!packet.isEmpty()) {
            final Integer mode1 = packet.getInteger(SaField.SAF_CMD_MODE1);
            final Integer mode2 = packet.getInteger(SaField.SAF_CMD_MODE2);

            result = (mode1 != null) && ((SEND_TO_REMOTE_MODE_COMMAND == mode1) || (RECEIVE_FROM_REMOTE_MODE_COMMAND == mode1))
                    && (mode2 != null) && (CUSTOM_OPERATION_CODE == mode2);
        }

        return result;
    }

    /**
     * Метод для проверки на запрос для отправки данных.
     *
     * @return результат проверки.
     */
    private boolean isSendRequest() {
        final Integer mode1 = mRequest.getInteger(SaField.SAF_CMD_MODE1);
        return (mode1 != null) && (SEND_TO_REMOTE_MODE_COMMAND == mode1);
    }

    /**
     * Метод для проверки на запрос для приема данных.
     *
     * @return результат проверки.
     */
    private boolean isReceiveRequest() {
        final Integer mode1 = mRequest.getInteger(SaField.SAF_CMD_MODE1);
        return (mode1 != null) && (RECEIVE_FROM_REMOTE_MODE_COMMAND == mode1);
    }

    /**
     * Метод для возвращения результата в зависимости от результата выполнения команды согласно протоколу SA.
     *
     * @param success признак успешного выполнения команды.
     * @return результат выполнения команды.
     * @see SaPacket
     */
    @NonNull
    private static SaPacket getOperationResult(final boolean success) {
        final SaPacket packet = new SaPacket();
        packet.putInteger(SaField.SAF_OPERATION_CODE, OperationCode.CUSTOM_COMMAND.getValue());
        packet.putInteger(SaField.SAF_CMD_MODE2, CUSTOM_OPERATION_CODE);

        if (success) {
            packet.putInteger(SaField.SAF_RESULT, SUCCESSFUL_RESULT);
        } else {
            packet.putInteger(SaField.SAF_RESULT, UNSUCCESSFUL_RESULT);
        }

        return packet;
    }

    /**
     * Метод для возвращения результата в зависимости от результата выполнения команды согласно протоколу SA.
     *
     * @param success признак успешного выполнения команды.
     * @param data    данные помещаемые в результат.
     * @return результат выполнения команды.
     * @see SaPacket
     */
    @NonNull
    private static SaPacket getOperationResult(final boolean success, final byte[] data) {
        final SaPacket packet = new SaPacket();
        packet.putInteger(SaField.SAF_OPERATION_CODE, OperationCode.CUSTOM_COMMAND.getValue());
        packet.putInteger(SaField.SAF_CMD_MODE2, CUSTOM_OPERATION_CODE);
        packet.putInteger(SaField.SAF_RESULT, UNSUCCESSFUL_RESULT);

        try {
            if (success) {
                packet.putInteger(SaField.SAF_RESULT, SUCCESSFUL_RESULT);
                packet.putBytes(SaField.SAF_FILE_DATA, data);
            }
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
        }

        return packet;
    }

}