package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.SaPacketPacker;
import ru.ppr.inpas.lib.protocol.SaPacket;

/**
 * Класс для отправки данных на терминал.
 */
public class SendState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(SendState.class);

    private final PosContext mContext;

    public SendState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "SendState.");

        final SaPacket request = mContext.getRequest();

        if (SaPacket.isValid(request)) {
            InpasLogger.info(TAG, "Sending.", request);
            final byte[] data = SaPacketPacker.pack(request);

            mContext.getPosCommunicator().send(data, true);
            mContext.getStateManager().setNextState(PosState.RECEIVE);
        } else {
            mContext.getStateManager().setNextState(PosState.ERROR);
        }
    }

}