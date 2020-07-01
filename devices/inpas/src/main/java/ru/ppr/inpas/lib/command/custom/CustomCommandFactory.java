package ru.ppr.inpas.lib.command.custom;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.inpas.lib.internal.NetworkCommunicator;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.CustomMode;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

public class CustomCommandFactory {
    private static final String TAG = InpasLogger.makeTag(CustomCommandFactory.class);

    /**
     * Метод возвращает режим выполнения пользовательской команды для работы с POS-терминалом.
     *
     * @param packet пакет по которому определяется режим выполнения пользовательской команды.
     * @return режим выполнения пользовательской команды.
     * @see CustomMode
     * @see SaPacket
     */
    @NonNull
    private static CustomMode getCustomOperationCode(@NonNull final SaPacket packet) {
        CustomMode customMode = CustomMode.UNKNOWN;
        final Integer mode2 = packet.getInteger(SaField.SAF_CMD_MODE2);

        if (mode2 != null) {
            customMode = CustomMode.from(mode2);
        }

        return customMode;
    }

    /**
     * Метод формирует пользовательскую комаду на основе передаваемых данных.
     *
     * @param mode         режим выполнения пользовательской команды .
     * @param packet       SA пакет содержащий запрос согласно SA протоколу.
     * @param communicator коммуникатор для сетевой части.
     * @return пользовательская комада.
     * @see CustomMode
     * @see SaPacket
     * @see NetworkCommunicator
     */
    @Nullable
    private static ICustomCommand getCustomCommandByMode(@NonNull final CustomMode mode, @NonNull final SaPacket packet,
                                                         @NonNull final NetworkCommunicator communicator) {
        ICustomCommand cmd = null;

        switch (mode) {
            case CONNECT_DISCONNECT: {
                cmd = new ConnectDisconnectCustomCommand(communicator, packet);
            }
            break;

            case SEND: {
                cmd = new SendDataCustomCommand(communicator, packet);
            }
            break;

            case SSL:
                break;
        }

        return cmd;
    }

    /**
     * Метод формирует пользовательскую комаду на основе передаваемых данных.
     *
     * @param packet       SA пакет содержащий запрос согласно SA протоколу.
     * @param communicator коммуникатор для сетевой части.
     * @return пользовательская комада.
     * @see CustomMode
     * @see SaPacket
     * @see NetworkCommunicator
     */
    @Nullable
    public static ICustomCommand getCustomCommand(@NonNull final SaPacket packet,
                                                  @NonNull final NetworkCommunicator communicator) {
        ICustomCommand cmd = null;

        if (SaPacket.isValid(packet)) {
            final OperationCode code = OperationCode.from(packet);

            switch (code) {
                case CUSTOM_COMMAND: {
                    try {
                        final CustomMode customMode = getCustomOperationCode(packet);
                        cmd = getCustomCommandByMode(customMode, packet, communicator);
                    } catch (Exception ex) {
                        cmd = null;
                        InpasLogger.error(TAG, ex);
                    }
                }
                break;
            }
        }

        return cmd;
    }

}