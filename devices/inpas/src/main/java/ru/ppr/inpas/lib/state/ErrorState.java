package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.PosCommunicator;
import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.SaPacketPacker;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.PosSignalType;

/**
 * Класс для обработки состояния ошибки.
 */
public class ErrorState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(ErrorState.class);

    private final PosContext mContext;

    public ErrorState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "ErrorState.");
        final SaPacket request = mContext.getRequest();
        final PosCommunicator communicator = mContext.getPosCommunicator();

        if (communicator.isConnected()) {
            if (SaPacket.isValid(request)) {
                InpasLogger.info(TAG, "Sending.", request);
                communicator.send(SaPacketPacker.pack(request), true);
            } else {
                InpasLogger.error(TAG, "Sending EOT.");
                communicator.send(new byte[]{PosSignalType.EOT.getAsByte()}, false);
            }
        } else {
            InpasLogger.error(TAG, "No active connection.");
        }

        mContext.getStateManager().setNextState(PosState.DONE);
    }

}