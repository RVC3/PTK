package ru.ppr.inpas.lib.state;


import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.MessageConstructor;
import ru.ppr.inpas.lib.packer.SaPacketPacker;
import ru.ppr.inpas.lib.packer.model.Message;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.OperationCode;

/**
 * Класс пля обработки состояния разбора данных от терминала.
 */
public class ParseState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(ParseState.class);

    private final PosContext mContext;

    public ParseState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "ParseState.");
        final MessageConstructor messageConstructor = mContext.getMessageConstructor();

        if (messageConstructor.isComplete()) {
            final Message message = messageConstructor.poll();
            final SaPacket packet = SaPacketPacker.unpack(message);

            if (packet != null) {
                InpasLogger.info(TAG, packet.toString());

                if (OperationCode.WAIT == OperationCode.from(packet)) {
                    mContext.getStateManager().setNextState(PosState.WAIT);
                } else {
                    mContext.add(packet);
                    mContext.getStateManager().setNextState(PosState.EXECUTE);
                }
            } else {
                InpasLogger.error(TAG, "Packet parse error.");
                mContext.getStateManager().setNextState(PosState.RECEIVE);
            }
        } else {
            InpasLogger.error(TAG, "Message isn't complete.");
            mContext.getStateManager().setNextState(PosState.RECEIVE);
        }
    }

}