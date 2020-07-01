package ru.ppr.inpas.lib.command.custom;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.NetworkCommunicator;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.CustomMode;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Пользовательская команда 16 «Установить или разорвать соединение с сервером».
 */
public class ConnectDisconnectCustomCommand implements ICustomCommand {
    private static final String TAG = InpasLogger.makeTag(ConnectDisconnectCustomCommand.class);
    private static final String DEFAULT_ENCODING = "Cp1251";
    private static final int CUSTOM_OPERATION_CODE = CustomMode.CONNECT_DISCONNECT.getValue();
    private static final int DISCONNECT_MODE_COMMAND = 0;
    private static final int CONNECT_MODE_COMMAND = 1;
    private static final int SUCCESSFUL_RESULT = 0;
    private static final int UNSUCCESSFUL_RESULT = 1;

    private final NetworkCommunicator mCommunicator;
    private final SaPacket mRequest;

    private boolean mError;
    private SaPacket mResult;

    public ConnectDisconnectCustomCommand(@NonNull final NetworkCommunicator communicator, @NonNull final SaPacket request) {
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
        if (isCorrectRequest(mRequest)) {
            try {
                setEndpoint(mRequest);

                if (isDisconnectRequest()) {
                    mCommunicator.disconnect();
                } else if (isConnectRequest()) {
                    if (!mCommunicator.isConnected()) {
                        mCommunicator.connect();
                    }
                }

                mError = false;
            } catch (Exception ex) {
                InpasLogger.error(TAG, ex);
            }
        }

        mResult = getOperationResult(!mError);
    }

    /**
     * Метод для проверки корректности запроса.
     *
     * @param packet зарос.
     * @return результат проверки.
     * @see SaPacket
     */
    private boolean isCorrectRequest(@NonNull final SaPacket packet) {
        boolean result = false;

        if (!packet.isEmpty()) {
            final Integer mode1 = packet.getInteger(SaField.SAF_CMD_MODE1);
            final Integer mode2 = packet.getInteger(SaField.SAF_CMD_MODE2);

            result = (mode1 != null) && ((DISCONNECT_MODE_COMMAND == mode1) || (CONNECT_MODE_COMMAND == mode1))
                    && (mode2 != null) && (CUSTOM_OPERATION_CODE == mode2);
        }

        return result;
    }

    /**
     * Метод для проверки на запрос для закрытия соединения.
     *
     * @return результат проверки.
     */
    private boolean isDisconnectRequest() {
        final Integer mode1 = mRequest.getInteger(SaField.SAF_CMD_MODE1);
        return (mode1 != null) && (DISCONNECT_MODE_COMMAND == mode1);
    }

    /**
     * Метод для проверки на запрос для открытия соединения.
     *
     * @return результат проверки.
     */
    private boolean isConnectRequest() {
        final Integer mode1 = mRequest.getInteger(SaField.SAF_CMD_MODE1);
        return (mode1 != null) && (CONNECT_MODE_COMMAND == mode1);
    }

    /**
     * Метод для получения значений удаленной точки доступа.
     *
     * @param packet пакет соедржащий необходимые данные.
     * @throws Exception если значения данных некорректны.
     */
    private void setEndpoint(@NonNull final SaPacket packet) throws Exception {
        final byte[] data = packet.getBytes(SaField.SAF_FILE_DATA);

        if (null == data) {
            throw new Exception(SaField.SAF_FILE_DATA + " no file data.");
        }

        final String[] parts = new String(data, DEFAULT_ENCODING).split(";");

        if (parts.length < 2) {
            throw new Exception("Invalid parameters.");
        }

        final String ip = parts[0];
        final int port = Integer.parseInt(parts[1]);

        mCommunicator.setEndPoint(ip, port);
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

}